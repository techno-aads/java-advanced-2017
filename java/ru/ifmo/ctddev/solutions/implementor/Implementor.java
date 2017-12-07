package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

/**
 * Implementation of {@link info.kgeorgiy.java.advanced.implementor.JarImpler}.
 * Used to generate implementation of specified interface or class, compile it and put to .jar file.
 *
 * @author Denis Semenov (sinedsem@gmail.com)
 */
public class Implementor implements JarImpler {

    /**
     * Used to keep provided type in order to shorten methods definitions.
     */
    private Class<?> clazz;

    /**
     * Used to keep writer in order to shorten methods definitions.
     */
    private PrintWriter writer;


    /**
     * Application entry point.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        if (args == null) {
            throw new NullPointerException();
        }

        if (args.length != 3 || !args[0].equals("-jar")) {
            System.out.println("Usage: Implementor -jar <class> <output>");
            return;
        }

        try {
            new Implementor().implementJar(Class.forName(args[1]), new File(args[2]).toPath());
        } catch (ImplerException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        this.clazz = aClass;

        if (!canImplement()) {
            throw new ImplerException();
        }

        try {
            try {
                String pckg = path.toString();
                if (aClass.getPackage() != null) {
                    pckg += File.separator + aClass.getPackage().getName().replace(".", "/");
                }
                //noinspection ResultOfMethodCallIgnored
                new File(pckg).mkdirs();
                writer = new PrintWriter(pckg + "/" + getImplName() + ".java");
            } catch (FileNotFoundException e) {
                throw new ImplerException(e);
            }

            printPackage();

            writer.println("public class " + getImplName() + (clazz.isInterface() ? " implements " : " extends ") + clazz.getSimpleName() + " {");
            writer.println();

            if (clazz.getDeclaredConstructors().length > 0 && !hasDefaultConstructor()) {
                for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                    if (!Modifier.isPrivate(constructor.getModifiers())) {
                        printConstructor(constructor);
                        break;
                    }
                }
            }

            for (Method method : getAccessibleMethods()) {
                if (method.isDefault() || Modifier.isStatic(method.getModifiers()) || !Modifier.isAbstract(method.getModifiers()) || Modifier.isFinal(method.getModifiers())) {
                    continue;
                }
                printMethod(method);
            }

            writer.println("}");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void implementJar(Class<?> aClass, Path file) throws ImplerException {
        this.clazz = aClass;

        Path path;
        try {
            path = Files.createTempDirectory("Impl");
        } catch (IOException e) {
            throw new ImplerException(e);
        }
        implement(aClass, path);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        List<String> args = new ArrayList<>();
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));

        String pckg = aClass.getPackage() == null ? "" : (aClass.getPackage().getName().replace(".", "/"));
        args.add(path.toString() + File.separator + pckg + File.separator + getImplName() + ".java");

        if (compiler == null) {
            System.err.println("Compiler not found, use JDK instead of JRE");
            System.exit(1);
        }

        compiler.run(null, null, null, args.toArray(new String[args.size()]));

        try (JarOutputStream os = new JarOutputStream(new FileOutputStream(file.toFile()))) {
            os.putNextEntry(new JarEntry(pckg + File.separator + getImplName() + ".class"));
            Files.copy(path.resolve(pckg + File.separator + getImplName() + ".class"), os);
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }


    /**
     * @return Implementation class simple name.
     */
    private String getImplName() {
        return clazz.getSimpleName() + "Impl";
    }

    /**
     * Prints 4 * level spaces to the output.
     *
     * @param level indent level.
     */
    private void printIndent(int level) {
        for (int i = 0; i < level; i++) {
            writer.print("    ");
        }
    }

    /**
     * Prints constructor implementation which matches super constructor to the output.
     *
     * @param constructor Constructor of the super class to match.
     */
    private void printConstructor(Constructor<?> constructor) {
        printIndent(1);
        writer.print("public");

        writer.print(" ");
        writer.print(getImplName());

        writer.print("() throws Exception {");

        printIndent(1);
        writer.print("super(");

        int i = 0;
        for (Parameter parameter : constructor.getParameters()) {
            if (i++ > 0) {
                writer.print(", ");
            }
            writer.print("(");
            printParameterType(parameter);
            writer.print(") ");
            writer.print(getDefaultValueForType(parameter.getType()));
        }

        writer.println(");");

        printIndent(1);
        writer.println("}");
        writer.println();
    }

    /**
     * Prints single method implementation to the output.
     *
     * @param method Method to implement.
     */
    private void printMethod(Method method) {
        printIndent(1);
        writer.println("@Override");

        printIndent(1);
        writer.print("public");

        if (method.getTypeParameters().length != 0) {
            writer.print(" ");
        }
        printTypeParameters(method.getTypeParameters());

        writer.print(method.getAnnotatedReturnType().getType().getTypeName());
        writer.print(" ");
        writer.print(method.getName());
        writer.print("(");

        int i = 0;
        for (Parameter parameter : method.getParameters()) {
            if (i++ > 0) {
                writer.print(", ");
            }
            printParameterType(parameter);

            writer.print("arg");
            writer.print(i);
        }

        writer.println(") {");

        printReturnStatement(method);

        printIndent(1);
        writer.println("}");
        writer.println();
    }

    /**
     * Prints package definition to the output.
     */
    private void printPackage() {
        if (clazz.getPackage() != null) {
            writer.println("package " + clazz.getPackage().getName() + ";");
            writer.println();
        }
    }

    /**
     * Prints to the output parameter's class name and generic type information if applicable. Prints space in the end.
     *
     * @param parameter Parameter to print its type.
     */
    private void printParameterType(Parameter parameter) {
        writer.print(getClassName(parameter.getType()));
        if (parameter.getAnnotatedType().getType() instanceof ParameterizedType) {
            printTypeParameters(((ParameterizedType) parameter.getAnnotatedType().getType()).getActualTypeArguments());
        } else {
            writer.print(" ");
        }
    }

    /**
     * Prints method return statement to the output.
     *
     * @param method Method to print return statement.
     */
    private void printReturnStatement(Method method) {
        if (!method.getReturnType().equals(Void.TYPE)) {
            printIndent(2);
            writer.println("return " + getDefaultValueForType(method.getReturnType()) + ";");
        }
    }

    /**
     * Prints generic type parameters to the output. Prints space in the end.
     *
     * @param typeParameters Type parameters to print.
     */
    private void printTypeParameters(Type[] typeParameters) {
        if (typeParameters.length > 0) {
            writer.print("<");
            int i = 0;
            for (Type type : typeParameters) {
                if (i++ > 0) {
                    writer.print(", ");
                }
                writer.print(type.getTypeName());
            }
            writer.print("> ");
        } else {
            writer.print(" ");
        }
    }

    /**
     * @param clazz to get its name.
     * @return Class' canonical name. If class belongs to java.lang package, returns class's simple name.
     */
    private String getClassName(Class<?> clazz) {
        String name = clazz.getCanonicalName();
        if (name.startsWith("java.lang.")) {
            return name.substring(10);
        }
        return name;
    }

    /**
     * @return True if and only if current class has constructor with no arguments.
     */
    private boolean hasDefaultConstructor() {
        return Arrays.stream(clazz.getDeclaredConstructors()).anyMatch(constructor -> constructor.getParameters().length == 0);
    }

    /**
     * @return True if Implementor should be able to implement current class.
     */
    private boolean canImplement() {
        return !clazz.isPrimitive() && !Modifier.isFinal(clazz.getModifiers()) && !clazz.isEnum() && !clazz.equals(Enum.class)
                && (clazz.isInterface() || Arrays.stream(clazz.getDeclaredConstructors()).anyMatch(constructor -> !Modifier.isPrivate(constructor.getModifiers())));
    }

    /**
     * Calculates default value for specified type.
     *
     * @param clazz Calculated value will be assignable from this type.
     * @return 'null' if clazz is an Object type, 'false' if boolean and 0 for all other primitive types.
     */
    private String getDefaultValueForType(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(Boolean.TYPE)) {
                return "false";
            } else {
                return "0";
            }
        } else {
            return "null";
        }
    }

    /**
     * Determines which class methods should be implemented.
     *
     * @return Collection of methods.
     */
    private Collection<Method> getAccessibleMethods() {
        Map<MethodHash, Method> result = new HashMap<>();
        Queue<Class> interfaces = new LinkedList<>();
        Class<?> aClass = clazz;
        while (aClass != null) {
            for (Method method : aClass.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (!Modifier.isPrivate(modifiers)) {
                    result.putIfAbsent(new MethodHash(method), method);
                }
            }
            Collections.addAll(interfaces, aClass.getInterfaces());
            aClass = aClass.getSuperclass();
        }

        for (Class anInterface : interfaces) {
            for (Method method : anInterface.getMethods()) {
                result.putIfAbsent(new MethodHash(method), method);
            }
        }

        return result.values();
    }

    /**
     * Used to remove duplicated from the collection of methods to implement.
     */
    private static class MethodHash {

        /**
         * Name of the method.
         */
        private final String name;

        /**
         * List of types of method's parameters.
         */
        private final List<Class> parameters;

        /**
         * @param method Source method.
         */
        private MethodHash(Method method) {
            name = method.getName();
            parameters = Arrays.stream(method.getParameters()).map(Parameter::getType).collect(Collectors.toList());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodHash that = (MethodHash) o;

            return name.equals(that.name) && (parameters != null ? parameters.equals(that.parameters) : that.parameters == null);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
            return result;
        }
    }
}
