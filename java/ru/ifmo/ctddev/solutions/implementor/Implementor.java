package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Class for generating a new class code that implements the specified interface or inherited from the class
 */
public class Implementor implements JarImpler {

    /**
     * Store the information about which methods of class already have been processed.
     * Class represented by canonical name and all parameter types.
     */
    Set<String> methodId = new HashSet<>();

    /**
     * Entry point
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("args.length < 2");
            return;
        }

        try {
            new Implementor().implementJar(Class.forName(args[0]), Paths.get(args[1]));
        } catch (ClassNotFoundException | ImplerException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param aClass token of the class to implement.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException if the aClass can not be generated in the jar-file
     */
    @Override
    public void implementJar(Class<?> aClass, Path jarFile) throws ImplerException {
        if (!jarFile.toString().toLowerCase().endsWith(".jar")) {
            throw new ImplerException("Incorrect name for jarFile");
        }

        Path root;
        if (jarFile.toString().lastIndexOf(File.separator) != -1) {
            root = Paths.get(jarFile.toString().substring(0, jarFile.toString().lastIndexOf(File.separator)));
        } else {
            root = Paths.get("").toAbsolutePath();
        }

        implement(aClass, root);

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        List<String> args = new ArrayList<>();
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));

        String canonicalName = aClass.getCanonicalName();
        String packagePath = canonicalName.substring(0, canonicalName.lastIndexOf('.')).replace(".", File.separator);
        args.add(root.toString() + File.separator + packagePath + File.separator + aClass.getSimpleName() + "Impl.java");

        if (javaCompiler == null) {
            throw new ImplerException("Not found java compiler");
        }

        javaCompiler.run(null, null, null, args.toArray(new String[args.size()]));

        String pathToJar = packagePath + File.separator + aClass.getSimpleName() + "Impl.class";

        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {

            jarOutputStream.putNextEntry(new JarEntry(pathToJar.replace(File.separator, "/")));
            Files.copy(root.resolve(pathToJar), jarOutputStream);
        } catch (IOException ex) {
            throw new ImplerException(ex.getMessage());
        }
    }

    /**
     * Responsible for genetating the implementation of specified class that represented by passed {@link Class}
     *
     * @param aClass token of the class to implement
     * @param path the destination directory where implementation should be located
     * @throws ImplerException if class cannot be implemented by some reasons
     */
    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        StringBuilder result = new StringBuilder();

        if (Modifier.isFinal(aClass.getModifiers())) {
            throw new ImplerException("Class is final");
        }
        if (aClass.isEnum() || aClass == Enum.class) {
            throw new ImplerException("Class is Enum");
        }
        if (aClass.isPrimitive()) {
            throw new ImplerException("Class is Primitive");
        }
        setPackage(aClass, result);
        setClass(aClass, result);

        try {
            writeResultFile(aClass, path, result);
            methodId = new HashSet<>();
        } catch (IOException e) {
            throw new ImplerException("Error while writing the result file.", e);
        }
    }

    /**
     * Responsible for generation of package
     *
     * <p>Append to the {@link
    StringBuilder} result the package
     *
     * @param aClass token of the class to implement
     * @param result {@link StringBuilder} represents the result string in where all generated information
     * will be placed
     */
    private void setPackage(Class<?> aClass, StringBuilder result) {
        result.append("package ")
                .append(aClass.getPackage().getName())
                .append(";\n");
    }

    /**
     * Responsible for generation of the body of the target class
     *
     * <p>Append to the {@link StringBuilder} result the class definition including the class modifier, name,
     * inheritance information, constructors and methods information.
     *
     * <p>Retrieves the constructors information using {@link #setConstructors} method
     *
     * <p>Retrieves the methods information using {@link #setMethods} method
     *
     * @param aClass token of the class to implement
     * @param result {@link StringBuilder} represents the result string in where all generated information
     * will be placed
     * @throws ImplerException if class cannot be implemented by some reasons
     */
    private void setClass(Class<?> aClass, StringBuilder result) throws ImplerException {
        result.append("public class ")
                .append(aClass.getSimpleName())
                .append("Impl ")
                .append(aClass.isInterface() ? "implements " : "extends ")
                .append(aClass.getName())
                .append(" {\n");

        setConstructors(aClass, result);
        setMethods(aClass, result);

        result.append("}");
    }

    /**
     * Responsible for generation of class's constructors
     *
     * <p>Append to the {@link StringBuilder} result constructors information.
     *
     * @param aClass token of the class to implement
     * @param result {@link StringBuilder} represents the result string in where all generated information
     * will be placed
     * @throws ImplerException if not public constructors
     */
    private void setConstructors(Class<?> aClass, StringBuilder result) throws ImplerException {
        boolean hasConstructors = false;

        for (Constructor constructor : aClass.getDeclaredConstructors()) {
            if (Modifier.isPrivate(constructor.getModifiers())) {
                continue;
            }
            hasConstructors = true;

            result.append("public ")
                    .append(aClass.getSimpleName())
                    .append("Impl (");

            int countParam
                    = constructor.getParameterCount();
            int counter = 1;

            StringBuilder params = new StringBuilder();

            for (Parameter parameter : constructor.getParameters()) {
                result.append(parameter.getParameterizedType().getTypeName())
                        .append(" ")
                        .append(parameter.getName());
                params.append(parameter.getName());

                if (countParam > counter++) {
                    result.append(", ");
                    params.append(", ");
                }
            }
            result.append(") ");

            int countExc = constructor.getExceptionTypes().length;
            int counterExc = 1;
            if (countExc > 0) result.append("throws ");
            for (Class<?> exception : constructor.getExceptionTypes()) {
                result.append(exception.getCanonicalName());
                if (countExc > counterExc++) result.append(", ");
            }

            result.append(" {\n")
                    .append("super(")
                    .append(params)
                    .append(");\n")
                    .append("}\n");
        }

        if (!hasConstructors && !aClass.isInterface()) {
            throw new ImplerException("Has not public constructors");
        }
    }


    /**
     * Responsible for generation of class's methods
     *
     * <p>Append to the {@link StringBuilder} result methods information.
     *
     * <p>Retrieves the methods information using {@link #setMethods} method
     *
     * @param aClass token of the class to implement
     * @param result {@link StringBuilder} represents the result string in where all generated information
     * will be placed
     */
    private void setMethods(Class<?> aClass, StringBuilder result) {

        if (aClass.isInterface()) {
            setMethods(aClass.getMethods(), result);
            return;
        }

        setMethods(aClass.getDeclaredMethods(), result);

        if (aClass.getSuperclass() != null) {
            setMethods(aClass.getSuperclass(), result);
        }
    }

    /**
     * Responsible for generation of method's id
     *
     * @param method of the class to implement
     * @return string method's id
     */
    private String getMethodId(Method method) {
        StringBuilder methodId = new
                StringBuilder(method.getName());
        for (Class<?> type : method.getParameterTypes()) {
            methodId.append(type.toString());
        }
        return methodId.toString();
    }

    /**
     * Responsible for generation of class's methods
     *
     * <p>Append to the {@link StringBuilder} result methods information.
     *
     * <p>Get default parameter for type using {@link #getDefaultParameterForType} method
     *
     * <p>Get method's id using {@link #getMethodId} method
     *
     * @param methods of the class to implement
     * @param result {@link StringBuilder} represents the result string in where all generated information
     * will be placed
     */
    private void setMethods(Method[] methods, StringBuilder result) {
        for (Method method : methods) {

            int modifiers = method.getModifiers();

            if (!Modifier.isAbstract(modifiers) || Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers)
                    || Modifier.isStatic(modifiers) || methodId.contains(getMethodId(method))) {
                continue;
            }

            methodId.add(getMethodId(method));

            result.append("public ")
                    .append(method.getReturnType().getCanonicalName())
                    .append(" ")
                    .append(method.getName())
                    .append(" (");

            int countParam = method.getParameterCount();
            int counter = 1;
            for (Class<?> parameterType : method.getParameterTypes()) {
                result.append(parameterType.getCanonicalName())
                        .append(" ")
                        .append("arg")
                        .append(counter);

                if (countParam > counter++)
                    result.append(", ");
            }
            result.append(") ");

            int countExc = method.getExceptionTypes().length;
            int counterExc = 1;
            if (countExc > 0) result.append("throws ");
            for (Class<?> exception : method.getExceptionTypes()) {
                result.append(exception.getCanonicalName());
                if (countExc > counterExc++) result.append(", ");
            }

            result.append(" {\n")
                    .append("return")
                    .append(" ")
                    .append(getDefaultParameterForType(method.getReturnType()))
                    .append(";\n")
                    .append("}\n");
        }
    }

    /**
     * Responsible for return of default parameter for type
     *
     * @param returnType type for return of method
     * @return string default parameter for type
     */
    private String getDefaultParameterForType(Class<?> returnType) {
        String returnValue = "";
        if (returnType != void.class) {
            returnValue = "null";
            if (returnType == boolean.class) {
                returnValue = Boolean.FALSE.toString();
            } else if (returnType.isPrimitive()) {
                returnValue = "0";
            }
        }
        return returnValue;
    }

    /**
     * Responsible for write result in file
     *
     * @param aClass token of the class to implement
     * @param path to file
     * @param result {@link StringBuilder} represents the result string in where all generated information
     * will be placed
     * @throws IOException if result cannot be wrote in file
     */
    private void writeResultFile(Class<?> aClass, Path path, StringBuilder result) throws IOException {
        String destDirName = path + File.separator + aClass.getPackage().getName().replace(".", File.separator);

        Files.createDirectories(Paths.get(destDirName));
        Files.write(Paths.get(destDirName + File.separator + aClass.getSimpleName() + "Impl.java"),
                result.toString().getBytes());
    }
}