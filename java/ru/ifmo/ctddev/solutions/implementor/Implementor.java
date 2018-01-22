package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Nikita Sokeran
 */
public class Implementor implements Impler {

  /**
   * Map keeps default values for primitive types;
   */
  protected static final Map<Class, String> DEFAULT_VALUE_MAP = new HashMap<>();

  /**
   * {@link Collector} for {@link String} joining. like "(str1, str2)";
   */
  protected static final Collector<CharSequence, ?, String> JOINING_COLLECTOR = Collectors
      .joining(", ", "(", ")");
  /**
   * {@link String} Constant for "Impl"
   */
  protected static final String CLASS_EXT = "Impl";
  /**
   * {@link String} Constant for "extends"
   */
  protected static final String EXTEND = "extends";
  /**
   * {@link String} Constant for "implement"
   */
  protected static final String IMPLEMENT = "implements";
  /**
   * {@link String} Constant empty String
   */
  protected static final String EMPTY_STRING = "";
  /**
   * {@link String} Constant for new line (\n)
   */
  protected static final String NEW_LINE = "\n";
  /**
   * {@link String} Constant for space symbol
   */
  protected static final String SPACE = " ";

  /**
   * Class constructor
   */
  public Implementor() {
    DEFAULT_VALUE_MAP.put(int.class, "0");
    DEFAULT_VALUE_MAP.put(char.class, "0");
    DEFAULT_VALUE_MAP.put(byte.class, "0");
    DEFAULT_VALUE_MAP.put(long.class, "0L");
    DEFAULT_VALUE_MAP.put(short.class, "0");
    DEFAULT_VALUE_MAP.put(double.class, "0.0");
    DEFAULT_VALUE_MAP.put(float.class, "0.0f");
    DEFAULT_VALUE_MAP.put(boolean.class, "false");
    DEFAULT_VALUE_MAP.put(void.class, "");
  }

  /**
   * @param clazz {@link Class} that needs to implement
   * @param root {@link Path} root directory.
   */
  @Override
  public void implement(Class<?> clazz, Path root) throws ImplerException {
    checkExtendable(clazz);

    Set<Method> overridableMethods = getAllMethods(clazz).stream()
        .filter(m -> checkModifiers(m.getModifiers()))
        .collect(Collectors.toSet());

    List<Constructor> constructors = Arrays
        .stream(clazz.getDeclaredConstructors())
        .filter(c -> checkModifiers(c.getModifiers()))
        .collect(Collectors.toList());

    writeImplementation(root, clazz, constructors, overridableMethods);
  }

  /**
   * This method check that clazz may be extendable.
   *
   * @param clazz {@link Class} that needs to check extendable
   */
  private void checkExtendable(final Class clazz) throws ImplerException {
    if (clazz == Enum.class || clazz.isEnum()) {
      throw new ImplerException("Class mustn't be an Enum!");
    } else if (clazz.isArray()) {
      throw new ImplerException("Class mustn't be an Array!");
    } else if (Modifier.isFinal(clazz.getModifiers())) {
      throw new ImplerException("Class mustn't be final!");
    } else if (!checkAvailableConstructors(clazz) && !clazz.isInterface()) {
      throw new ImplerException("No available default constructors!");
    }
  }

  /**
   * @param str - {@link String} that needs to replace keywords: abstract,
   * transient, volatile
   * @return java.lang.String
   */
  private String fixModifiersString(final String str) {
    return str.replace("abstract", EMPTY_STRING)
        .replace("transient", EMPTY_STRING)
        .replace("volatile", EMPTY_STRING)
        .trim();
  }

  /**
   * @param token - {@link Class}
   * @return {@link List<String>} that contains all methods for token
   */

  private Set<Method> getAllMethods(final Class token) {
    Set<Method> methods = new TreeSet<>(Comparator
        .comparing(m -> m.getName() + Arrays.stream(m.getParameterTypes())
            .map(Class::getSimpleName)
            .collect(Collectors.joining(EMPTY_STRING))));
    methods.addAll(Arrays.stream(token.getDeclaredMethods())
        .filter(m -> Modifier.isAbstract(m.getModifiers()))
        .collect(Collectors.toSet()));

    if (token.isInterface()) {
      LinkedList<Class> interfaces = new LinkedList<>(
          Arrays.asList(token.getInterfaces()));

      while (!interfaces.isEmpty()) {
        Class interfaceClass = interfaces.poll();
        interfaces.addAll(Arrays.asList(interfaceClass.getInterfaces()));
        methods.addAll(Arrays.stream(interfaceClass.getDeclaredMethods())
            .filter(m -> Modifier.isAbstract(m.getModifiers()))
            .collect(Collectors.toSet()));
      }
    }

    Set<Method> allMethods = new TreeSet<>(Comparator
        .comparing(m -> m.getName() + Arrays.stream(m.getParameterTypes())
            .map(Class::getSimpleName)
            .collect(Collectors.joining(EMPTY_STRING))));

    Class clazz = token.getSuperclass();
    while (clazz != null && Modifier.isAbstract(clazz.getModifiers())) {

      Arrays.stream(clazz.getDeclaredMethods())
          .filter(m -> Modifier.isAbstract(m.getModifiers()))
          .collect(Collectors.toCollection(() -> methods));

      Arrays.stream(clazz.getDeclaredMethods())
          .filter(m -> !Modifier.isAbstract(m.getModifiers()))
          .collect(Collectors.toCollection(() -> allMethods));
      clazz = clazz.getSuperclass();
    }

    methods.removeAll(allMethods);
    return methods;
  }

  /**
   * @param mod - modifiers for check
   * @return true if modifiers allow to extention/implemention class, false
   * otherwise
   */
  private boolean checkModifiers(final int mod) {
    return !(Modifier.isFinal(mod) ||
        Modifier.isNative(mod) ||
        Modifier.isStatic(mod) ||
        Modifier.isPrivate(mod));
  }

  /**
   * @param clazz {@link Class} class that need to check available constructors
   * @return true if clazz contains available constructors, false otherwise
   */
  private boolean checkAvailableConstructors(Class clazz) {
    List<Constructor> constructors = Arrays
        .asList(clazz.getDeclaredConstructors());
    return constructors.stream()
        .map(Constructor::getModifiers)
        .filter(this::checkModifiers)
        .count() > 0;
  }

  /**
   * @param root {@link Path} - root path of directory
   * @param clazz {@link Class} that needs to implement
   * @return {@link File} instance
   */
  private File resolveFile(final Path root, final Class clazz)
      throws IOException {
    String dir = root.toString() + File.separator + clazz.getPackage().getName()
        .replaceAll("\\.", "/") + File.separator;
    Files.createDirectories(Paths.get(dir));
    return new File(dir + clazz.getSimpleName() + CLASS_EXT + ".java");
  }


  protected String collectClassInfo(final Class clazz,
      final Collection<Constructor> constructors,
      final Collection<Method> methods) {
    return appendStrings(getPackage(clazz),
        NEW_LINE,
        getClassHeader(clazz),
        NEW_LINE,
        constructorsToString(clazz, constructors),
        NEW_LINE,
        methodsToString(methods),
        "}"
    );
  }

  /**
   * @param root {@link Path} - root path of directory
   * @param clazz {@link Class} that needs to implement
   * @param constructors {@link Collection<Constructor>} - collection of
   * constructors for implementation
   * @param methods {@link Collection<Method>} - collection of methods for
   * implementation
   */

  protected void writeImplementation(final Path root, final Class clazz,
      final Collection<Constructor> constructors,
      final Collection<Method> methods) {
    try (Writer writer = new FileWriter(resolveFile(root, clazz))) {
      writer.write(collectClassInfo(clazz, constructors, methods));
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param clazz {@link Class} class that needs to implement
   * @return {@link String} that contains information about packege for clazz
   */
  private String getPackage(final Class clazz) {
    return "package " + clazz.getPackage().getName() + ";";
  }

  /**
   * @param strings varargs of {@link String} that need to join
   * @return {@link String} joined string
   */
  private String appendStrings(final String... strings) {
    return String.join(EMPTY_STRING, strings);
  }

  /**
   * @param clazz {@link Class} class that needs to implement
   * @param constructors {@link Collections<Constructor>} - collection of
   * Constructors that need to implement
   * @return {@link List<String>} list of constructors string
   */
  private String constructorsToString(final Class clazz,
      final Collection<Constructor> constructors) {
    return constructors.stream()
        .map(constructor -> {
          Iterator<Integer> iterator = IntStream
              .range(0, constructor.getParameterCount()).iterator();

          List<Pair<String, String>> pairs = Arrays
              .stream(constructor.getParameterTypes())
              .map(c -> new Pair<>(c.getCanonicalName(),
                  "arg" + iterator.next()))
              .collect(Collectors.toList());

          String throwsExceptions = Arrays
              .stream(constructor.getExceptionTypes())
              .map(Class::getCanonicalName).collect(Collectors.joining(", "));
          throwsExceptions =
              (EMPTY_STRING.equals(throwsExceptions)) ? EMPTY_STRING
                  : " throws " + throwsExceptions;

          String args = pairs.stream()
              .map(p -> p.getKey() + SPACE + p.getValue())
              .collect(JOINING_COLLECTOR);

          String body = "super" + pairs.stream()
              .map(Pair::getValue)
              .collect(JOINING_COLLECTOR) + ";";

          return appendStrings("\t",
              fixModifiersString(Modifier.toString(constructor.getModifiers())),
              SPACE,
              clazz.getSimpleName(),
              CLASS_EXT,
              args,
              throwsExceptions,
              " {\n\t\t",
              body,
              "\n\t}\n");
        })
        .collect(Collectors.joining());
  }

  /**
   * @param clazz {@link Class} class that needs to implement
   * @return {@link String} that contains full name of class with modifiers
   */
  private String getClassHeader(final Class clazz) {
    return appendStrings("public class ",
        clazz.getSimpleName(),
        CLASS_EXT,
        SPACE,
        (clazz.isInterface() ? IMPLEMENT : EXTEND),
        SPACE,
        clazz.getCanonicalName(),
        " {"
    );
  }

  /**
   * @param methods {@link Collection<Method>} - collection of methods that
   * needs to transform to list of Strings
   * @return {@link List<String>}
   */
  private String methodsToString(final Collection<Method> methods) {
    return methods.stream()
        .map(m -> {
          Iterator<Integer> iterator = IntStream.range(0, m.getParameterCount())
              .iterator();
          Class returnedType = m.getReturnType();

          String args = Arrays.stream(m.getParameterTypes())
              .map(c -> c.getCanonicalName() + " arg" + iterator.next())
              .collect(JOINING_COLLECTOR);

          String body =
              EMPTY_STRING.equals(DEFAULT_VALUE_MAP.get(returnedType)) ? ""
                  : "return " + DEFAULT_VALUE_MAP.get(returnedType) + ";";

          return appendStrings("\t@Override\n\t",
              fixModifiersString(Modifier.toString(m.getModifiers())),
              SPACE,
              returnedType.getCanonicalName(),
              SPACE,
              m.getName(),
              args,
              " {\n\t\t ",
              body,
              "\n\t}\n\n"
          );
        })
        .collect(Collectors.joining());
  }

  public static void main(String[] args)
      throws ClassNotFoundException, ImplerException {
    Implementor implementor = new Implementor();
    implementor.implement(ClassLoader.getSystemClassLoader().loadClass(args[0]), Paths.get("."));
  }
}
