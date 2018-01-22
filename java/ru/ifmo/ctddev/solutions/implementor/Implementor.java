package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
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

/**
 * Class which implements given interfaces or extends given classes. Also creates jar files with this files.
 */
public class Implementor implements JarImpler {

    /**
     * Class for which the implementation is created
     */
    private Class<?> clazz;
    /**
     * Name of class for which the implementation is created
     */
    private String className;
    /**
     * Array of methods of class or interface
     */
    private Method[] methods;
    /**
     * Package of interface or class
     */
    private String packageName;
    /**
     * Class comparator
     */
    private Comparator<Class<?>> CLASS_COMPARATOR = Comparator.comparing(Class::getSimpleName);
    /**
     * Set of imports in implemented class
     */
    private Set<Class<?>> imports;
    /**
     * Field indicates if implementation extends class
     */
    private boolean isClass;
    /**
     * Output jar file
     */
    private File outputJavaFile;

    /**
     * Main method. Used to run created jar file. 2 params creates only implementation, more params creates Jar files
     * for implementations.
     * @param args array of arguments.
     */
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                throw new IllegalAccessException("Wrong number of arguments");
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
            System.out.println("Implementation error.");
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Generates class or interface implementation and writes it in java file.
     *
     * @param clazz {@link Class} class or interface to create implementation for.
     * @param path {@link Path} output directory.
     * @throws ImplerException if appeared exceptions during implementation.
     */
    @Override
    public void implement(Class<?> clazz, Path path) throws ImplerException {
        try {
            outputJavaFile = getOutputFile(clazz, path);
            PrintWriter writer = new PrintWriter(outputJavaFile);
            String implementation;
            if (clazz.isInterface()) {
                isClass = false;
                implementation = getImplementedClass(clazz);
            } else {
                isClass = true;
                validate(clazz);
                implementation = getExtendedClass(clazz);
            }
            writer.write(implementation);
            writer.close();
//            PrintWriter secondWriter = new PrintWriter(outputJavaFile.getName());
//            secondWriter.write(implementation);
//            secondWriter.close();
        } catch (Exception e) {
            throw new ImplerException(e);
        }
    }

    /**
     * Check possibility to create implementation
     * @param clazz {@link Class} class for which need create implementation
     * @throws ImplerException if implementation impossible to create.
     */
    private void validate(Class<?> clazz) throws ImplerException {
        if (clazz.isPrimitive()) {
            throw new ImplerException("Type is a primitive.");
        }
        if (!clazz.isInterface()) {
            if (Modifier.isFinal(clazz.getModifiers())) {
                throw new ImplerException("Cannot extend final class: " + clazz.toString());
            }
        }
        if (Enum.class.isAssignableFrom(clazz)) {
            throw new ImplerException("Cannot extend enum type: " + clazz.toString());
        }
        boolean allConstructorsPrivate = true;
        Constructor[] constructors = clazz.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                allConstructorsPrivate = false;
            }
        }
        if (allConstructorsPrivate) {
            throw new ImplerException("Cannot extend class with only private constructors: " + clazz.toString());
        }
    }

    /**
     * Extract base class params
     * @param currentInterface {@link Class} class for which need create implementation with extracted params.
     */
    private void extractClassParams(Class<?> currentInterface) {
        this.clazz = currentInterface;
        this.className = currentInterface.getSimpleName();
        this.packageName = currentInterface.getPackage().getName();
        this.methods = currentInterface.getMethods();
        this.imports = new TreeSet<>(CLASS_COMPARATOR);
        this.imports.add(currentInterface);
        for (Method m : methods) {
            addImportsFrom(m);
        }
    }

    /**
     * Base method to create implementation for interface
     * @param currentInterface {@link Class} interface for which implementation created
     * @return {@link String} String with implementation.
     */
    private String getImplementedClass(Class<?> currentInterface) {
        extractClassParams(currentInterface);
        return generateHeader() + generateBody() + "}";
    }

    /**
     * Base method to create extended class for class
     * @param currentClass {@link Class} class which extended
     * @return {@link String} String with extended class.
     */
    private String getExtendedClass(Class<?> currentClass) {
        extractClassParams(currentClass);
        return generateHeader() + generateClassBody() + "}";
    }

    /**
     * Generates header for class
     * @return {@link String} String with implemented header
     */
    private String generateHeader() {
        StringBuilder headerStruct = new StringBuilder();
        headerStruct
                .append("package ").append(packageName).append(";\n");
        for (Class<?> s : imports) {
            headerStruct.append("import ").append(s.getName()).append(";\n");
        }
        headerStruct.append("\n");
        headerStruct.append("public class ").append(className);
        if (isClass) {
            headerStruct.append("Impl extends ").append(className).append(" {\n");
        } else {
            headerStruct.append("Impl implements ").append(className).append(" {\n");
        }
        return headerStruct.toString();
    }

    /**
     * Generates body for implemented class
     * @return {@link String} String with body of implemented class
     */
    private String generateBody() {
        StringBuilder body = new StringBuilder();
        for (Method m : methods) {
            body.append(implementMethod(m));
        }
        return body.toString();

    }

    /**
     * Generates body for extended class
     * @return {@link String} String with body of extended class
     */
    private String generateClassBody() {
        StringBuilder classBody = new StringBuilder();
        classBody.append(implementConstructors());
        for (Method m : methods) {
            int modifiers = m.getModifiers();
            if (Modifier.isAbstract(modifiers)) {
                classBody.append(implementMethod(m));
            }
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            int modifiers = m.getModifiers();
            if (Modifier.isAbstract(modifiers) && !Modifier.isPublic(modifiers)) {
                classBody.append(implementMethod(m));
            }
        }
        boolean abstractClass = true;
        Class superClass = clazz;
        while (abstractClass) {
            abstractClass = false;
            superClass = superClass.getSuperclass();
            methods = superClass.getDeclaredMethods();
            for (Method m : methods) {
                int modifiers = m.getModifiers();
                if (Modifier.isAbstract(modifiers) && !Modifier.isPublic(modifiers)) {
                    abstractClass = true;
                    classBody.append(implementMethod(m));
                }
            }
        }
        return classBody.toString();
    }

    /**
     * Generates realization for method
     * @param method {@link Method} method which need to generate
     * @return {@link String} String with method realization
     */
    private String implementMethod(Method method) {
        StringBuilder methodStruct = new StringBuilder();
        methodStruct
                .append("    public ")
                .append(method.getReturnType().getSimpleName())
                .append(" ")
                .append(method.getName()).append("(").append(methodParams(method)).append(") {\n");

        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE)) {
            methodStruct.append("        ");
            if (returnType.isPrimitive()) {
                if (returnType.equals(Boolean.TYPE)) {
                    methodStruct.append("return false;\n");
                }
                else if (returnType.equals(Character.TYPE)) {
                    methodStruct.append("return '\\0';\n");
                }
                else {
                    methodStruct.append("return 0;\n");
                }
            }
            else {
                methodStruct.append("return null;\n");
            }
        }
        methodStruct.append("    }\n\n");
        return methodStruct.toString();
    }

    /**
     * Generates constructors for extended class
     * @return {@link String} String with generated constructors
     */
    private String implementConstructors() {
        StringBuilder constructorStruct = new StringBuilder();
        Constructor[] constructors = clazz.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            int modifiers = constructor.getModifiers();
            if (Modifier.isTransient(modifiers)) {
                modifiers -= Modifier.TRANSIENT;
            }
            if (!Modifier.isPrivate(modifiers)) {
                constructorStruct.append("    ").append(Modifier.toString(modifiers))
                        .append(" ")
                        .append(className)
                        .append("Impl")
                        .append("(");
                Class[] params = constructor.getParameterTypes();
                Class[] except = constructor.getExceptionTypes();
                if (params.length > 0) {
                    for (int i = 0; i < params.length; i++) {
                        constructorStruct.append(params[i].getTypeName())
                                .append(" args")
                                .append(i);
                        if (i == params.length - 1) {
                            constructorStruct.append(")");
                        } else {
                            constructorStruct.append(", ");
                        }
                    }
                } else {
                    constructorStruct.append(")");
                }
                if (except.length > 0) {
                    constructorStruct.append(" throws ");
                    for (int i = 0; i < except.length; i++) {
                        constructorStruct.append(except[i].getTypeName());
                        if (i < except.length - 1) {
                            constructorStruct.append(", ");
                        }
                    }
                }
                constructorStruct.append(" {\n        super(");
                if (params.length > 0) {
                    for (int i = 0; i < params.length; i++) {
                        constructorStruct.append("args")
                                .append(i);
                        if (i < params.length - 1) {
                            constructorStruct.append(", ");
                        }
                    }
                }
                constructorStruct.append(");\n    }\n\n");
            }
        }
        return constructorStruct.toString();
    }

    /**
     * Extract method params
     * @param m {@link Method} method in which parameters are extracted
     * @return String with extracted method parameters
     */
    private String methodParams(Method m) {
        Class<?>[] args = m.getParameterTypes();

        return IntStream.range(0, args.length)
                .mapToObj(idx -> {
                    String s;
                    if (args[idx].isArray()) {
                        s = args[idx].getComponentType().getName() + "[]";
                    }
                    else {
                        s = args[idx].getName();
                    }
                    return s + " arg" + idx;
                }).collect(Collectors.joining(", "));
    }

    private void addImports(Class<?>... types) {
        this.imports.addAll(Arrays.stream(types)
                .map(this::extractType)
                .filter(this::validImport)
                .collect(Collectors.toList())
        );
    }

    /**
     * Extract method imports
     * @param method {@link Method} method for which need to extract imports
     */
    private void addImportsFrom(Method method) {
        addImports(method.getReturnType());
        addImportsFrom((Executable) method);
    }

    /**
     * Extract additional method imports
     * @param executable {@link Executable} for extraction
     */
    private void addImportsFrom(Executable executable) {
        addImports(executable.getParameterTypes());
        addImports(executable.getExceptionTypes());
    }

    /**
     * Validates if class need to be imported
     * @param aClass {@link Class} validated class
     * @return true if class need to import
     */
    private boolean validImport(Class<?> aClass) {
        if (aClass == null) {
            return false;
        }
        if (aClass.isLocalClass() || aClass.isMemberClass()) {
            return false;
        }

        Package p = aClass.getPackage();
        return !p.getName().equals("java.lang") && !p.equals(clazz.getPackage());
    }

    /**
     * Class to extract only necessary types
     * @param aClass {@link Class} type to extract data
     * @return {@link Class} extracted type or null
     */
    private Class<?> extractType(Class<?> aClass) {
        if (aClass.isArray()) {
            aClass = aClass.getComponentType();
        }
        return aClass.isPrimitive() ? null : aClass;
    }

    /**
     * Helper method to extract output file and additional data
     * @param clazz {@link Class} class for which need generate implementation
     * @param path {@link Path} path where generated file need save
     * @return {@link File} file for writer
     * @throws IOException if passed wrong parameters
     */
    private File getOutputFile(Class<?> clazz, Path path) throws IOException {
        String classFileName = clazz.getSimpleName() + "Impl.java";
        String[] packages = clazz.getPackage().getName().split("\\.");
        Path outputPath = Paths.get(path.toAbsolutePath().toString(), packages);
        Files.createDirectories(outputPath);
        outputPath = Paths.get(outputPath.toString(), classFileName);
        return outputPath.toFile();
    }

    /**
     * Crates jar files with implemented files
     * @param token {@link Class} type token to create implementation for
     * @param jarFile {@link Path} path where generated file need save
     * @throws ImplerException if appeared exceptions during implementation
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path directoryPath;
        if (jarFile.toString().lastIndexOf(File.separator) != -1) {
            directoryPath = Paths.get(jarFile.toString().substring(0, jarFile.toString().lastIndexOf(File.separator)));
        }
        else{
            directoryPath = Paths.get("").toAbsolutePath();
        }
        implement(token, directoryPath);
        compileAndPackToJar(token, directoryPath, jarFile);
    }

    /**
     * Method which compiles and packs created implementation to Jar file
     * @param clazz {@link Class} class for which implementation generated
     * @param fileDirectory {@link Path} path to directory with generated files
     * @param jarFile {@link Path} path to generated jar file
     * @throws ImplerException when exceptions during compiling or saving to archive
     */
    private void compileAndPackToJar(Class clazz, Path fileDirectory, Path jarFile) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));
        args.add(outputJavaFile.toString());
        int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        if (exitCode != 0) throw new ImplerException("Compilation error");

        String packagePath = clazz.getPackage().getName().replace(".", File.separator);
        String classPath = packagePath + File.separator + clazz.getSimpleName() + "Impl.class";
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {
            jarOutputStream.putNextEntry(new JarEntry(classPath));
            Files.copy(fileDirectory.resolve(classPath), jarOutputStream);
        } catch (IOException e) {
            throw new ImplerException("Packaging error");
        }
    }
}