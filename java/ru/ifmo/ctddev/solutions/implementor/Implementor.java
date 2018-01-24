package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Generate code which implements specified interface or extends class
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Creates implementation of specified interface
     *
     * @param token type token to create implementation for
     * @param root  root directory
     * @throws ImplerException When impossible to create Implementation for token
     * @see java.lang.Class
     * @see java.nio.file.Path
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

        if (token == void.class || token == Enum.class || token.isEnum() || token.isArray()) {
            throw new ImplerException("Impossible to implement");
        }

        try {
            clazz = Implementor.class.getClassLoader().loadClass(token.getCanonicalName());
        } catch (Exception e) {
            throw new ImplerException(e);
        }

        result = new StringBuilder();

        Path path = Paths.get(root.toString(), clazz.getCanonicalName().replace(".", "/") + "Impl");
        String className = path.getFileName().toString();

        implementedCode = Paths.get(path.toString() + ".java");

        Package clazzPackage = clazz.getPackage();
        if (clazzPackage != null) {
            result.append("package ").append(clazz.getPackage().getName()).append(";\n");
        }

        result.append("public class ").append(className).append(" ");
        List<Executable> executableList = new ArrayList<>();
        if (clazz.isInterface()) {
            result.append("implements");
            Collections.addAll(executableList, clazz.getMethods());
        } else {
            result.append("extends");
            Class superClazz = clazz.getSuperclass();
            if (superClazz != null) {
                Collections.addAll(executableList, superClazz.getDeclaredMethods());
            }
            List<Method> methods = Arrays.stream(clazz.getDeclaredMethods()).filter(
                    method -> method.getDeclaringClass().equals(clazz)
            ).collect(Collectors.toList());

            executableList.addAll(methods);

            List<Constructor> constructors = Arrays.stream(clazz.getDeclaredConstructors()).filter(
                    constructor ->
                            !Modifier.isPrivate(constructor.getModifiers()) & !Modifier.isFinal(constructor.getModifiers())
            ).collect(Collectors.toList());
            if (constructors.size() == 0) {
                throw new ImplerException("no available constructors");
            }
            executableList.addAll(constructors);
        }
        result.append(" ").append(clazz.getName()).append(" {\n");
        Set<String> uniqueMethods = new LinkedHashSet();
        for (Executable executable : executableList) {
            if (executable instanceof Method) {
                String executableStringRepr = getExecutableStringRepr(executable);
                if (uniqueMethods.contains(executableStringRepr) || !clazz.isInterface() && !Modifier.isAbstract(executable.getModifiers())) {
                    continue;
                }
                uniqueMethods.add(executableStringRepr);
            }
            result.append(printMethodsRealisation(className, executable));
        }
        result.append("}\n");
        writeFile(implementedCode);
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
        String fullPathToClassFile = implementedCode.toString().replace(".java", JAVA_CLASS_EXTENSION);
        String classFileName = Paths.get(fullPathToClassFile).getFileName().toString();
        String packagePath = token.getPackage().getName().replace(".", File.separator);
        String classPath = Paths.get(packagePath, classFileName).toString();

        fullPathToClassFile = fullPathToClassFile.replace('\\', '/');
        makeJar(jarFile, fullPathToClassFile, classPath);
    }

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
     * @param executable Implemented executable
     * @return thrown exceptions source code definition
     * @see java.lang.reflect.Method
     * @see java.lang.StringBuilder
     */
    private StringBuilder printMethodsThrowsExceptions(Executable executable) {
        Type[] exceptions = executable.getExceptionTypes();
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
     * Adds implemented methods body to generated source code
     *
     * @param constructor Implemented constructor
     * @return constructor body source code
     * @see java.lang.reflect.Constructor
     * @see java.lang.StringBuilder
     */
    private StringBuilder printConstructorBody(Constructor constructor) {

        List<String> parameterList = Arrays.stream(constructor.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.toList());
        return new StringBuilder("\t\tsuper(" + String.join(", ", parameterList) + ");");

    }

    /**
     * Returns string representation of executable parameters
     *
     * @param executable Implemented executable
     * @return string representation of executable parameters
     * @see java.lang.reflect.Executable
     * @see java.lang.StringBuilder
     */
    public StringBuilder getMethodParametersRepr(Executable executable) {
        StringBuilder rv = new StringBuilder();
        int i = 0;
        for (Parameter t : executable.getParameters()) {
            if (i != 0) {
                rv.append(", ");
            }
            rv.append(t.getType().getCanonicalName()).append(" ").append(t.getName());
            i++;
        }
        return rv;
    }

    /**
     * Adds implemented methods source code(including thrown exceptions and body
     *
     * @param className  name of the implemented class
     * @param executable Implemented executable
     * @return Source code of implemented executable
     * @see java.lang.reflect.Method
     * @see java.lang.StringBuilder
     */
    private StringBuilder printMethodsRealisation(String className, Executable executable) {
        StringBuilder rv = new StringBuilder();
        String modifiersString = Modifier.toString(executable.getModifiers() & ((Modifier.ABSTRACT | Modifier.TRANSIENT | Modifier.STATIC) ^ Integer.MAX_VALUE));
        if (rv.length() > 0) {
            modifiersString += " ";
        }

        rv.append("\t").append(modifiersString).append(" ");
        if (executable instanceof Method) {
            rv.append(((Method) executable).getReturnType().getTypeName());
        }
        rv.append(" ");
        if (executable instanceof Method) {
            rv.append(executable.getName());
        } else {
            rv.append(className);
        }
        rv.append("(");
        rv.append(getMethodParametersRepr(executable));
        rv.append(")");
        rv.append(printMethodsThrowsExceptions(executable));
        rv.append(" {\n");
        if (executable instanceof Method) {
            rv.append(printMethodsBody((Method) executable));
        } else {
            rv.append(printConstructorBody((Constructor) executable));
        }
        rv.append("\t}\n");
        return rv;
    }
    
    /**
     * Return executable identification string representation
     *
     * @param executable executable to generate string representation
     * @see java.lang.reflect.Executable
     */
    public String getExecutableStringRepr(Executable executable) {
        StringBuilder reprStringBuilder = new StringBuilder(executable.getName());
        reprStringBuilder.append(" ").append(getMethodParametersRepr(executable));
        return reprStringBuilder.toString();
    }

    /**
     * Compiles source code located in directory
     *
     * @param path directory where source code to compile located
     * @return compiler return code (0 if succeed)
     * @see java.nio.file.Path
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

}}
