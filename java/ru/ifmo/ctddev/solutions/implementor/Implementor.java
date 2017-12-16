package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Implementor implements Impler, JarImpler {

    public static final String PACKAGE_SEPARATOR = ".";
    public static final String JAVA_CLASS_EXTENSION = ".class";
    public static final String JAVA_SOURCE_EXTENSION = ".java";
    public static final String RESULT_CLASS_NAME_SUFFIX = "Impl";

    /**
     * Generated interface implementation source code
     *
     * @see java.lang.StringBuilder
     */
    private StringBuilder result;
    /**
     * Where implemented source code *.java file is located
     *
     * @see java.io.File
     */
    private Path implementedCode;

    /**
     * Empty constructor
     */
    public Implementor() {

    }

    /**
     * Generates arguments name string based on number in method arguments list
     *
     * @param i number in arguments list
     * @return argument's name
     * @see java.lang.String
     */
    private String argName(int i) {
        String b = Integer.toString(i);
        String rv = "";
        for (int j = 0; j < b.length(); j++) {
            rv += (char) (b.charAt(j) - '0' + 'a');
        }
        return rv;
    }

    /**
     * Adds implemented methods exceptions to generated source code
     *
     * @param method Implemented method
     * @return thrown exceptions source code definition
     * @see java.lang.reflect.Method
     * @see java.lang.StringBuilder
     */
    private StringBuilder printMethodsThrowsExceptions(Method method) {
        Type[] exceptions = method.getExceptionTypes();
        if (exceptions.length == 0) {
            return new StringBuilder("");
        }
        StringBuilder rv = new StringBuilder(" throws");
        boolean printComma = false;
        for (Type t : exceptions) {
            rv.append(" ");
            if (!printComma) {
                printComma = true;
            } else {
                rv.append(",");
            }
            rv.append(t.getTypeName());
        }
        return rv;
    }

    /**
     * Adds implemented methods body to generated source code
     *
     * @param method Implemented method
     * @return methods body source code
     * @see java.lang.reflect.Method
     * @see java.lang.StringBuilder
     */
    private StringBuilder printMethodsBody(Method method) {
        StringBuilder rv = new StringBuilder("\t\treturn ");
        Class c = method.getReturnType();
        if (c.isPrimitive()) {
            if (c.equals(boolean.class)) {
                rv.append("false");
            } else if (c.equals(void.class)) {
                rv.append("");
            } else {
                rv.append("0");
            }
        } else {
            rv.append("null");
        }
        rv.append(";\n");
        return rv;
    }

    /**
     * Adds implemented methods source code(including thrown exceptions and body
     *
     * @param method Implemented method
     * @return Source code of implemented method
     * @see java.lang.reflect.Method
     * @see java.lang.StringBuilder
     */

    private StringBuilder printMethodsRealisation(Method method) {
        StringBuilder rv = new StringBuilder();
        method.getDefaultValue();
        String modifiersString = Modifier.toString(method.getModifiers() & ((Modifier.ABSTRACT | Modifier.TRANSIENT | Modifier.STATIC) ^ Integer.MAX_VALUE));
        if (rv.length() > 0) {
            modifiersString += " ";
        }
        rv.append("\t").append(modifiersString).append(" ");
        rv.append(method.getReturnType().getTypeName());
        rv.append(" ").append(method.getName()).append("(");
        int i = 0;
        for (Type t : method.getParameterTypes()) {
            if (i != 0) {
                rv.append(", ");
            }
            rv.append(t.getTypeName()).append(" ").append(argName(i));
            i++;
        }
        rv.append(")");
        rv.append(printMethodsThrowsExceptions(method));
        rv.append(" {\n");
        rv.append(printMethodsBody(method));
        rv.append("\t}\n");
        return rv;
    }

    /**
     * Implements interface and then make jarFile with its implementation.
     *
     * @param token   type token to create implementation for.
     * @param jarFile file path where we need to write our jarFile file
     * @see java.io.File
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {

        compileFile(implementedCode);
        String fullPathToClassFile = implementedCode.toString().replace(JAVA_SOURCE_EXTENSION, JAVA_CLASS_EXTENSION);
        String classFileName = Paths.get(fullPathToClassFile).getFileName().toString();
        String packagePath = token.getPackage().getName().replace(PACKAGE_SEPARATOR, File.separator);
        String classPath = Paths.get(packagePath, classFileName).toString();

        fullPathToClassFile = fullPathToClassFile.replace('\\', '/');
        makeJar(jarFile, fullPathToClassFile, classPath);
    }

    /**
     * Creates implementation of specified interface
     *
     * @param token type token to create implementation for
     * @param root  root directory
     * @throws ImplerException When impossible to create Implementation for token
     * @see java.lang.Class
     * @see java.io.File
     */

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        Class clazz;
        if (token.isPrimitive()) {
            throw new ImplerException("class is primitive");
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("class is final");
        }
        try {
            clazz = Implementor.class.getClassLoader().loadClass(token.getCanonicalName());
        } catch (Exception e) {
            throw new ImplerException(e);
        }

        result = new StringBuilder();

        Path path = Paths.get(root.toString(), clazz.getCanonicalName().replace(".", "/") + RESULT_CLASS_NAME_SUFFIX);
        String className = path.getFileName().toString();

        implementedCode = Paths.get(path.toString() + ".java");

        Package clazzPackage = clazz.getPackage();
        if (clazzPackage != null) {
            result.append("package ").append(clazz.getPackage().getName()).append(";\n");
        }

        result.append("public class ").append(className).append(" ");
        result.append(clazz.isInterface() ? "implements" : "extends");
        result.append(" ").append(clazz.getName()).append(" {\n");
        for (Method method : clazz.getDeclaredMethods()) {
            result.append(printMethodsRealisation(method));
        }
        result.append("}\n");
        writeFile(implementedCode);
    }

    /**
     * Compiles source code located in directory
     *
     * @param path directory where source code to compile located
     * @return compiler return code (0 if succeed)
     * @see java.io.File
     * @see java.lang.String
     */
    private int compileFile(final Path path) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.add(path.toString());
        args.add("-cp");
        args.add(path.getParent() + File.pathSeparator + System.getProperty("java.class.path"));
        return compiler.run(null, null, null, args.toArray(new String[args.size()]));
    }

    /**
     * Makes jar from compiled class file
     *
     * @param jarFile             Path to place where jar will be placed
     * @param fullPathToClassFile path to compiled file
     * @param classPath           class location
     * @throws ImplerException when can't save created file
     * @see java.io.File
     * @see java.lang.String
     */

    private void makeJar(Path jarFile, String fullPathToClassFile, String classPath) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream jarOutput = new JarOutputStream(new FileOutputStream(jarFile.toFile()), manifest)) {
            jarOutput.putNextEntry(new ZipEntry(classPath));
            Files.copy(Paths.get(fullPathToClassFile), jarOutput);
        } catch (IOException ex) {
            throw new ImplerException("Error while writing jar", ex);
        }
    }

    /**
     * Writes implemented source code on disk
     *
     * @param path .java file where source code should be write in
     * @throws ImplerException when can't save created file
     * @see java.io.File
     */

    private void writeFile(Path path) throws ImplerException {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, result.toString().getBytes());
        } catch (IOException e) {
            throw new ImplerException("Can't write result file");

        }
    }

}