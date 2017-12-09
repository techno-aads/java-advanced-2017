package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.newInputStream;

public class Implementor implements JarImpler {
    /**
     * type token to create implementation for.
     */
    private Class<?> token;

    /**
     * Implementation name will be saved
     */
    private String fileName;

    /**
     * File path where class implementation had to be saved
     */
    private Path filePath;

    /**
     * Default values for primitive types
     */
    private final Map<String, String> valuesForPrimitiveTypes = new HashMap<>();

    /**
     * Implementation of the type token
     */
    private StringBuilder fileText;

    /**
     * Initial operations for Implementor.
     * Validates user input, and other stuff
     *
     * @param token {@link Class} type token to create implementation for.
     * @param root  {@link Path} root directory.
     * @throws ImplerException if it is impossible to generate realization for type token.
     */
    private void init(Class<?> token, Path root) throws ImplerException {
        if (token == Enum.class || token.isEnum() || token.isArray() || Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Not implementable class!");
        }

        List<Constructor<?>> constructorsToImplement = Arrays.stream(token.getDeclaredConstructors())
                .filter(c -> !Modifier.isPrivate(c.getModifiers()))
                .collect(Collectors.toList());


        if (constructorsToImplement.size() == 0 && !token.isInterface()) {
            throw new ImplerException("No available default constructors!");
        }

        this.token = token;
        fileText = new StringBuilder();
        fileName = token.getName().replace(".", "/");
        filePath = Paths.get(root + "/" + fileName + "Impl.java");

        valuesForPrimitiveTypes.put("int", "0");
        valuesForPrimitiveTypes.put("char", "0");
        valuesForPrimitiveTypes.put("byte", "0");
        valuesForPrimitiveTypes.put("long", "0L");
        valuesForPrimitiveTypes.put("short", "0");
        valuesForPrimitiveTypes.put("double", "0.0");
        valuesForPrimitiveTypes.put("float", "0.0f");
        valuesForPrimitiveTypes.put("boolean", "false");
        valuesForPrimitiveTypes.put("void", "");

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            System.out.println("IOException " + e.getMessage());
        }
    }

    /**
     * Generates set of methods to create implementation for
     *
     * @return {@link Set} of methods to implement
     */
    private Set<Method> getMethodsToImplement() {
        Set<Method> methods = new TreeSet<>(Comparator
                .comparing(m -> m.getName() + Arrays.stream(m.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(""))));

        Set<Method> implementedMethods = new TreeSet<>(Comparator
                .comparing(m -> m.getName() + Arrays.stream(m.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(""))));

        appendOwnMethods(methods, implementedMethods);
        appendInterfaces(methods);
        appendSuperclassesMethods(methods, implementedMethods);

        methods.removeAll(implementedMethods);
        return methods;
    }

    /**
     * Generates set of methods to implement and set of methods which are already implemented in class itself
     * And fills it in corresponding input sets
     *
     * @param methods            {@link Set} of methods to implement.
     * @param implementedMethods {@link Set} of methods which are already implemented
     */
    private void appendOwnMethods(Set<Method> methods, Set<Method> implementedMethods) {
        for (Method method : token.getDeclaredMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                methods.add(method);
            } else {
                implementedMethods.add(method);
            }
        }
    }

    /**
     * Generates set of methods to implement and set of methods which are already implemented in superclasses of token
     * And fills it in corresponding input sets
     *
     * @param methods            {@link Set} of methods to implement.
     * @param implementedMethods {@link Set} of methods which are already implemented
     */
    private void appendSuperclassesMethods(Set<Method> methods, Set<Method> implementedMethods) {
        Class superClass = token.getSuperclass();
        while (superClass != null) {
            Method[] superMethodsArr = superClass.getDeclaredMethods();
            for (Method method : superMethodsArr) {
                if (Modifier.isAbstract(method.getModifiers())) {
                    methods.add(method);
                } else {
                    implementedMethods.add(method);
                }
            }
            superClass = superClass.getSuperclass();
        }
    }

    /**
     * Generates set of methods to implement in all interfaces of the token
     * And fills it in corresponding input sets
     *
     * @param methods {@link Set} of methods to implement.
     */
    private void appendInterfaces(Set<Method> methods) {
        LinkedList<Class> interfaces = new LinkedList<>(Arrays.asList(token.getInterfaces()));
        while (!interfaces.isEmpty()) {
            Class superInterface = interfaces.poll();
            interfaces.addAll(Arrays.asList(superInterface.getInterfaces()));
            methods.addAll(Arrays.stream(superInterface.getDeclaredMethods())
                    .filter(m -> Modifier.isAbstract(m.getModifiers()))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Appends first line of class - package of this class
     */
    private void appendPackage() {
        fileText.append("package ")
                .append(token.getPackage().getName())
                .append(";\n");
    }

    /**
     * Appends implemented class declaration
     */
    private void appendClassDeclaration() {
        fileText.append("\npublic class ").append(token.getSimpleName()).append("Impl ")
                .append(token.isInterface() ? "implements " : "extends ")
                .append(token.getCanonicalName())
                .append(" {\n\n");
    }

    /**
     * Appends implemented class constructor inplementations
     */
    private void appendClassConstructors() {
        for (Constructor<?> constr : token.getDeclaredConstructors()) {
            fileText.append("\t").append(getAccess(constr.getModifiers()))
                    .append(" ").append(token.getSimpleName()).append("Impl");

            Class<?>[] parameterTypes = constr.getParameterTypes();
            fileText.append("(")
                    .append(String.join(", ", getInputParametersList(parameterTypes)))
                    .append(")");

            Class<?>[] exceptions = constr.getExceptionTypes();
            if (exceptions.length > 0) {
                fileText.append(" throws ")
                        .append(String.join(", ",
                                Arrays.stream(exceptions)
                                        .map(Class::getCanonicalName)
                                        .collect(Collectors.toList())));

            }

            fileText.append(" {\n\t\tsuper(")
                    .append(String.join(", ",
                            IntStream.range(0, parameterTypes.length)
                                    .mapToObj(i -> " argument" + i)
                                    .collect(Collectors.toList())))
                    .append(");\n\t};\n\n");
        }

    }

    /**
     * Appemds methods implementations for implemented class
     *
     * @param methods {@link Set} of methods to implement
     */
    private void appendClassMethods(Set<Method> methods) {
        for (Method method : methods) {
            if (!token.isInterface()) {
                fileText.append("\t@Override\n");
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            fileText.append("\t ").append(getAccess(method.getModifiers()))
                    .append(" ")
                    .append(method.getReturnType().getCanonicalName()).append(" ")
                    .append(method.getName()).append("(")
                    .append(String.join(", ", getInputParametersList(parameterTypes)))
                    .append(") {\n\t\treturn ")
                    .append(valuesForPrimitiveTypes.getOrDefault(method.getReturnType().getCanonicalName(), "null"))
                    .append(";\n\t}\n\n");
        }
    }

    /**
     * Appends class finish lines
     */
    private void appendClassEnding() {
        fileText.append("}\n");
    }

    /**
     * Generates access modifier from modifiers of {@link Class} or {@link Method}
     *
     * @param modifiers modifiers of {@link Class} or {@link Method}
     * @return {@link String} access modifier
     */
    private String getAccess(int modifiers) {
        return Modifier.toString(modifiers).split(" ")[0];
    }

    /**
     * Generates {@link List} of input parameters in pattern "argugment1, argument2"
     *
     * @param parameterTypes {@link Class}[] array of input parameter types
     * @return {@link List} of arguments
     */
    private List getInputParametersList(Class<?>[] parameterTypes) {
        return IntStream.range(0, parameterTypes.length)
                .mapToObj(i -> parameterTypes[i].getCanonicalName() + " argument" + i)
                .collect(Collectors.toList());
    }

    /**
     * Generates class or interface realization and writes this realization in java file
     *
     * @param token {@link Class} type token to create implementation for.
     * @param root  {@link Path} root directory.
     * @throws ImplerException if it is impossible to generate realization for type token.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        init(token, root);
        Set<Method> methods = getMethodsToImplement();

        appendPackage();
        appendClassDeclaration();
        appendClassConstructors();
        appendClassMethods(methods);
        appendClassEnding();

        try (FileWriter fWriter = new FileWriter(filePath.toString(), false);
             BufferedWriter out = new BufferedWriter(fWriter)) {
            out.write(fileText.toString());
        } catch (IOException e) {
            System.err.println("Error while writing in output file, " + e.getMessage());
        }
    }

    /**
     * Main method to run from console
     *
     * @param args param arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                throw new IllegalAccessException("Count of arguments must be equals to 2 or 3");
            }

            if (args.length == 2) {
                new Implementor().implement(Class.forName(args[0]), Paths.get(args[1]));
            } else {
                Class<?> clazz = Class.forName(args[0]);
                Path path = Paths.get(args[1]);
                Implementor impler = new Implementor();
                impler.implement(clazz, path);
                impler.implementJar(clazz, path);
            }

        } catch (ClassNotFoundException | ImplerException e) {
            System.out.println("Error while implementing, exit...");
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Generates jar file for generated .java file
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException on IO exceptions
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        // get path of the generated java file
        List<String> javaPathSplitted = Arrays.asList(jarFile.toString().split("\\."));
        javaPathSplitted = javaPathSplitted.subList(0, javaPathSplitted.size() - 1);

        String generatedClassesRootPath = String.join("\\", javaPathSplitted);
        Path javaPath = Paths.get(generatedClassesRootPath + "Impl.java");
        Path jarPath = Paths.get(String.join("\\", javaPathSplitted) + "Impl.class");

        // compile file to class
        ToolProvider.getSystemJavaCompiler().run(null, null, null, javaPath.toString());

        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {
            generateJar(jarOutputStream, jarPath);
        } catch (IOException e) {
            throw new ImplerException("Error while writing in jar", e);
        }
    }

    /**
     * Generates jar file in specified path from compiled files
     *
     * @param jarOutputStream      {@link JarOutputStream} which contains creating jar
     * @param pathToGeneratedClass path for JAR
     * @throws IOException on IO issues
     */
    private void generateJar(JarOutputStream jarOutputStream, Path pathToGeneratedClass) throws IOException {
        Iterator<Path> iter = pathToGeneratedClass.iterator();
        String rootName = iter.next().getFileName().toString();
        String relativePath = "";

        while (iter.hasNext()) {
            relativePath += iter.next().getFileName();
            if (iter.hasNext()) {
                relativePath += "/";
            }

            addJarEntryToJarStream(relativePath, pathToGeneratedClass, jarOutputStream);

            if (!iter.hasNext()) {
                try (InputStream in = newInputStream(Paths.get(rootName + "/" + relativePath))) {
                    byte[] bytes = new byte[4096];
                    int count = in.read(bytes);
                    while (count != -1) {
                        jarOutputStream.write(bytes, 0, count);
                        count = in.read(bytes);
                    }
                } catch (IOException e) {
                    throw e;
                }
            }
        }

        jarOutputStream.close();
    }

    /**
     * Adds jar entry to current jarOutputStream
     *
     * @param entryPath       path of entry to be added
     * @param path            JAR path
     * @param jarOutputStream {@link JarOutputStream} used output stream
     * @throws IOException on IO exceptions
     */
    private void addJarEntryToJarStream(String entryPath, Path path, JarOutputStream jarOutputStream) throws IOException {
        JarEntry jarEntry = new JarEntry(entryPath);
        jarEntry.setLastModifiedTime(getLastModifiedTime(path));
        jarOutputStream.putNextEntry(jarEntry);
    }
}
