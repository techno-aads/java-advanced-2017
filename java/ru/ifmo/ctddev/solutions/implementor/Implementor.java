package ru.ifmo.ctddev.solutions.implementor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * The <code> Implementor </code> class is used for generating implementation of interfaces or extending classes.
 */
public class Implementor implements JarImpler {

    /**
     * A line separator for the current platform
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * A file separator for the current platform
     */
    private static final String FILE_SEPARATOR = File.separator;

    /**
     * A {@link String} constant for the jar extension
     */
    private static final String JAR_EXTENSION = ".jar";

    /**
     * A {@link String} constant for the java extension of the implemented class
     */
    private static final String JAVA_EXTENSION = "Impl.java";

    /**
     * A {@link String} constant for the class extension of the implemented class
     */
    private static final String CLASS_EXTENSION = "Impl.class";

    /**
     * Program entry
     *
     * @param args are program arguments
     * @throws ImplerException in case the given class does not meet the requirements
     */
    public static void main (String [] args) throws ImplerException {
        if (args == null || args.length != 2) {
            System.out.println("Not enough arguments");
            return;
        }
        try {
            new Implementor().implementJar(Class.forName(args[0]), Paths.get(args[1]));
        }
        catch (ClassNotFoundException e) {
            System.out.println("An error occurred while implementing class: " + e.getMessage());
        }
    }

    /**
     * This method implements the given interface or class and compiles it
     *
     * @param token is the class for implementation
     * @param jarFile target <tt>.jar </tt> file
     * @throws ImplerException in case the given class is not valid for implementation
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (!jarFile.toString().toLowerCase().endsWith(JAR_EXTENSION)) {
            throw new ImplerException("Name for jar file must end with '.jar'");
        }

        Path root = jarFile.toString().lastIndexOf(FILE_SEPARATOR) != -1
                ? Paths.get(jarFile.toString().substring(0, jarFile.toString().lastIndexOf(FILE_SEPARATOR)))
                : Paths.get("").toAbsolutePath();

        implement(token, root);

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        String canonicalName = token.getCanonicalName();
        String packagePath = canonicalName.substring(0, canonicalName.lastIndexOf('.')).replace(".", FILE_SEPARATOR);
        String javaFilePath = root.toString() + FILE_SEPARATOR + packagePath + FILE_SEPARATOR + token.getSimpleName() + JAVA_EXTENSION;

        javaCompiler.run(null, null, null, "-cp", System.getProperty("java.class.path"), javaFilePath);

        String pathToJar = packagePath + FILE_SEPARATOR + token.getSimpleName() + CLASS_EXTENSION;

        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {
            jarOutputStream.putNextEntry(new JarEntry(pathToJar.replace(File.separator, "/")));
            Files.copy(root.resolve(pathToJar), jarOutputStream);

        } catch (IOException ex) {
            throw new ImplerException("An error occurred while writing a jar file: " + ex.getMessage());
        }
    }

    /**
     * Implements the given class and writes it to a ".java" file in the specified directory
     *
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException in case the given class is not valid for implementation
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!isValidClass(token))
            throw new ImplerException("Unable to implement the given class as it is of invalid type or has final modifier");

        String classSource = buildClassSource(token, root);

        try {
            writeJavaFile(token, root, classSource);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Builds implementation for the given class
     *
     * @param token is the input class for implementation
     * @param root is the path to the class in the File System
     * @return a {@link String} source code of the implemented class
     * @throws ImplerException in case the given class is not valid for implementation
     * @see Implementor#isValidClass(Class)
     */
    private String buildClassSource (Class<?> token, Path root) throws ImplerException{
        StringBuilder classSource = new StringBuilder();

        appendPackage(token, root, classSource);
        appendClassDefinition(token, classSource);
        appendConstructors(token, classSource);
        appendMethods(token, classSource);

        return classSource.toString();
    }

    /**
     * Determines if the given class could be implemented or not based on its type and modifiers
     *
     * @param token is the input class
     * @return true if class could be implemented
     */
    private boolean isValidClass (Class<?> token) {
        return (!Modifier.isFinal(token.getModifiers())
                && !token.isPrimitive()
                && !token.isEnum()
                && token != Enum.class
                && !token.isArray());
    }

    /**
     * Appends package information to the resulting source code of the implemented class
     *
     * @param token is the class for implementation
     * @param root is the path to the class in File System
     * @param classSource {@link StringBuilder} is the source code of the implemented interface or class}
     */
    private void appendPackage(Class token, Path root, StringBuilder classSource) {
        String destinationDirName = root + FILE_SEPARATOR + token.getPackage().getName().replace(".", FILE_SEPARATOR);

        if (token.getPackage().getName().equals(destinationDirName))
            return;

        classSource.append("package ")
                .append(token.getPackage().getName())
                .append(";")
                .append(LINE_SEPARATOR)
                .append(LINE_SEPARATOR);
    }

    /**
     * Appends class definition information
     *
     * @param token is the input class for implementation
     * @param classSource {@link StringBuilder} is the source code of the implemented interface or class}
     */
    private void appendClassDefinition(Class token, StringBuilder classSource) {
        classSource.append("public class ")
                .append(token.getSimpleName())
                .append("Impl")
                .append(token.isInterface() ? " implements " : " extends ")
                .append(token.getSimpleName())
                .append(" {")
                .append(LINE_SEPARATOR)
                .append(LINE_SEPARATOR);
    }

    /**
     * Appends such method information as:
     * <ul>
     *     <li> method modifier </li>
     *     <li> method name </li>
     *     <li> method arguments </li>
     *     <li> thrown exceptions </li>
     *     <li> method body </li>
     * </ul>
     *
     * @see Implementor#appendMethodArgs(java.lang.reflect.Parameter[], java.lang.StringBuilder)
     * @see Implementor#appendMethodArgs(Class[], StringBuilder)
     * @see Implementor#appendException(Executable, StringBuilder)
     * @see Implementor#appendMethodImpl(Method, StringBuilder)
     * @param token is the input class
     * @param classSource {@link StringBuilder} is the source code of the implemented interface or class}
     */
    private void appendMethods (Class token, StringBuilder classSource) {
        Map<String, Method> methods = getMethods(token);

        for (Map.Entry<String, Method> methodEntry: methods.entrySet()) {
            Method method = methodEntry.getValue();

            if (isValidMethod(method, token))
                continue;

            classSource.append(LINE_SEPARATOR)
                    .append("public ")
                    .append(method.getGenericReturnType() instanceof ParameterizedType
                            ? (method.getGenericReturnType()).getTypeName()
                            : method.getReturnType().getCanonicalName())
                    .append(" ")
                    .append(method.getName())
                    .append(" ");

            appendMethodArgs(method.getParameterTypes(), classSource);
            appendException(method, classSource);
            appendMethodImpl(method, classSource);
        }

        classSource.append("}");
    }

    /**
     * Determines if the given method could be implemented
     *
     * @param method {@link Method} is the method to validate
     * @param token is the source class
     * @return true if the given method is valid for implementation
     */
    private boolean isValidMethod (Method method, Class<?> token) {
        int modifier = method.getModifiers();

        return Modifier.isFinal(modifier)
                || Modifier.isPrivate(modifier)
                || Modifier.isStatic(modifier)
                || Modifier.isNative(modifier)
                || (
                (Modifier.isPublic(modifier)
                        || Modifier.isProtected(modifier)
                        || method.isDefault()
                )
                        && (!Modifier.isAbstract(modifier)
                        && !token.isInterface()));
    }

    /**
     * Appends information about method arguments specifying their return type and names
     *
     * @param params {@link Parameter} is the collection of parameters from the source {@link Executable}
     * @param classSource {@link StringBuilder} is the source code of the implemented interface or class}
     */
    private void appendMethodArgs (Parameter[] params, StringBuilder classSource) {
        classSource.append("(");

        for (int i = 0; i < params.length; i++) {
            classSource.append(params[i].getParameterizedType().getTypeName())
                    .append(" ")
                    .append(params[i].getName())
                    .append(i == params.length - 1 ? "" : ", ");
        }

        classSource.append(")");
    }

    /**
     * Appends information about method arguments specifying their return type and names
     *
     * @param returnTypes is the collection of parameter types used in the method
     * @param classSource {@link StringBuilder} is the source code of the implemented interface or class
     */
    private void appendMethodArgs (Class<?>[] returnTypes, StringBuilder classSource) {
        classSource.append("(");

        for (int i = 0; i < returnTypes.length; i++) {
            classSource.append(returnTypes[i].getCanonicalName())
                    .append(" ")
                    .append("args")
                    .append(i)
                    .append(i == returnTypes.length - 1 ? "" : ", ");
        }

        classSource.append(")");
    }

    /**
     * Appends information about exceptions thrown by the given <code> executable </code>
     *
     * @param executable {@link Executable} is a method or constructor for which exceptions are determined
     * @param classSource {@link StringBuilder} is the source code of the implemented interface or class
     */
    private void appendException (Executable executable, StringBuilder classSource) {
        Class<?> [] exceptions = executable.getExceptionTypes();

        if (exceptions.length > 0)
            classSource.append("throws ");

        for (int i = 0; i < exceptions.length; i ++) {
            classSource.append(exceptions[i].getCanonicalName());

            if (i != exceptions.length - 1) classSource.append(", ");
        }
    }

    /**
     * Appends a method implementation for the given <code> method </code> to the {@link StringBuilder} source code
     *
     * @param method is the target method for which implementation will be added
     * @param classSource {@link StringBuilder} is the source code of the implemented interface or class
     */
    private void appendMethodImpl (Method method, StringBuilder classSource) {
        classSource.append("{")
                .append(LINE_SEPARATOR);

        if (void.class.equals(method.getReturnType())) {
            classSource.append("}")
                    .append(LINE_SEPARATOR)
                    .append(LINE_SEPARATOR);
        }
        else {
            classSource.append("return ")
                    .append(getDefaultReturn(method))
                    .append(";")
                    .append(LINE_SEPARATOR)
                    .append("}")
                    .append(LINE_SEPARATOR)
                    .append(LINE_SEPARATOR);
        }
    }

    /**
     * This method collects a {@link Map} of {@link String} identifiers and {@link Method} methods for the specified class
     * The identifier is calculated as {@link Method#getName()} + all {@link Method#getReturnType()} names
     *
     * @param token is the input class from which the methods are collected
     * @return a unique collection of methods
     */
    private Map<String, Method> getMethods (Class<?> token) {
        List<Method> methods = token.isInterface()
                ? new ArrayList<>(Arrays.asList(token.getMethods()))
                : new ArrayList<>(Arrays.asList(token.getDeclaredMethods()));

        if (token.getSuperclass() != null) {
            methods.addAll(Arrays.asList(token.getSuperclass().getDeclaredMethods()));
        }

        Map<String, Method> targetMethods = new HashMap<>();
        for (Method method: methods) {
            StringBuilder identifier = new StringBuilder(method.getName());
            for (Class<?> type : method.getParameterTypes()) {
                identifier.append(type.toString());
            }

            if (!targetMethods.containsKey(identifier.toString())) {
                targetMethods.put(identifier.toString(), method);
            }
        }

        return targetMethods;
    }

    /**
     * Returns a default value for the given method depending on its return type
     *
     * @param method is the target method for which to determine return value
     * @return a {@link String} representation of return value
     */
    private String getDefaultReturn (Method method) {
        Class<?> returnType = method.getReturnType();
        if (boolean.class.equals(returnType))
            return Boolean.FALSE.toString();

        if (returnType.isPrimitive())
            return "0";

        else return "null";
    }

    /**
     * This method appends information about constructors to the <code> classSource </code> for the implemented class
     *
     * @param token is the input class to be implemented
     * @param classSource is the {@link StringBuilder} containing source code information
     *                    of the implemented class
     * @throws ImplerException if no constructors could be declared
     */
    private void appendConstructors (Class<?> token, StringBuilder classSource) throws ImplerException {
        Constructor<?>[] constructors = token.getDeclaredConstructors();
        int countAppended = 0;

        for (Constructor<?> constructor: constructors) {
            int modifier = constructor.getModifiers();

            if (Modifier.isPrivate(modifier) || Modifier.isFinal(modifier))
                continue;

            classSource.append("public ")
                    .append(token.getSimpleName())
                    .append("Impl ");

            Parameter[] paramTypes = constructor.getParameters();

            appendMethodArgs(paramTypes, classSource);
            appendException(constructor, classSource);

            classSource.append(" {");

            if (paramTypes.length > 0 ) {
                classSource.append(LINE_SEPARATOR)
                        .append("super(");

                for (int i = 0; i < paramTypes.length; i++) {
                    classSource.append("arg")
                            .append(i)
                            .append(i == paramTypes.length - 1 ? "" : ", ");
                }

                classSource.append(");");
            }
            classSource.append(LINE_SEPARATOR)
                    .append("}")
                    .append(LINE_SEPARATOR)
                    .append(LINE_SEPARATOR);

            countAppended ++;
        }

        if (token.isInterface()) {
            classSource.append("public ")
                    .append(token.getSimpleName())
                    .append("Impl () {}")
                    .append(LINE_SEPARATOR);

            countAppended++;
        }

        if (countAppended == 0)
            throw new ImplerException("There are no available constructors in " + token.getSimpleName());
    }

    /**
     * Writes a resulting  java file in the specified directory
     *
     * @param token is the input class for implementation
     * @param path is the path in File System leading to the generated file
     * @param classSource {@link StringBuilder} is the source code of the implemented interface or class
     * @throws IOException in case of errors during writing to a file
     */
    private void writeJavaFile(Class<?> token, Path path, String classSource) throws IOException {
        String dirName = path + FILE_SEPARATOR + token.getPackage().getName().replace(".", FILE_SEPARATOR);

        Files.createDirectories(Paths.get(dirName));
        Files.write(Paths.get(dirName + FILE_SEPARATOR + token.getSimpleName() + JAVA_EXTENSION),
                classSource.getBytes());
    }

}
