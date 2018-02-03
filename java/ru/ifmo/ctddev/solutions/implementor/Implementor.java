package ru.ifmo.ctddev.solutions.implementor;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Realization for interfaces {@link Impler} and {@link JarImpler}
 * @author Dyomin Alexander
 * @version 1.0
 * @since 1.0
 */
public class Implementor implements JarImpler, Impler {

    /**
     * Line separator for current platform
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();
    /**
     * Interface to get realization
     */
    private Class<?> clazz;
    /**
     * Array of methods of interface
     */
    private Method[] methods;
    /**
     * Set of imported classes that we need for our class realization
     */
    private Set<Class<?>> imports;

    /**
     *
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException
     */
    public void implement(Class<?> token, Path root) throws ImplerException {
        this.clazz = token;
        this.methods = this.getMethods(this.clazz);
        this.imports = this.getClassImports(this.clazz);

        try {
            String classRealization = generateClassRealization(token);
            File out = getFilename(token, root);
            Files.createDirectories(out.getParentFile().toPath());
            Writer writer = new OutputStreamWriter(new FileOutputStream(out));
            writer.write(classRealization);
            writer.close();
            compileClass(out);
        } catch(Exception e) {
            throw new ImplerException(e);
        }
    }

    /**
     *
     * @param token type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        this.clazz = token;
        this.methods = this.getMethods(this.clazz);
        this.imports = this.getClassImports(this.clazz);

        try {
            Path root = new File(".").toPath();
            String classRealization = generateClassRealization(token);
            File out = getFilename(token, root);
            Files.createDirectories(out.getParentFile().toPath());
//            Files.createDirectories(jarFile.getParent());
            Writer writer = new OutputStreamWriter(new FileOutputStream(out));
            writer.write(classRealization);
            writer.close();
            compileClass(out);
            String outString = out.toString().replaceAll("\\\\", "/");
            String classNameString = outString.substring(0, outString.length() - 5) + ".class";
            System.out.println(classNameString);
            File sourceFile = new File(classNameString);
            Files.createDirectories(jarFile.getParent());
            File jarOutput = new File(jarFile.toString());
            jarOutput.createNewFile();
            createJar(jarOutput, sourceFile);
        } catch(Exception e) {
            throw new ImplerException(e);
        }
    }

    /**
     *
     * @param token class or interface to get imports
     * @return set of classes/interfaces that should be imported
     */
    private Set<Class<?>> getClassImports(Class<?> token) {
        Set<Class<?>> importSet = new HashSet<>();
        Class<?> _token;
        for(Method m : methods) {
            for (Class paramType : m.getParameterTypes()) {
                _token = extractType(paramType);
                if(checkClassType(_token)){
                    importSet.add(_token);
                }
            }
            _token = extractType(m.getReturnType());
            if(checkClassType(_token)){
                importSet.add(_token);
            }
            for (Class exceptionType : Arrays.asList(m.getExceptionTypes())) {
                _token = extractType(m.getReturnType());
                if(checkClassType(_token)){
                    importSet.add(_token);
                }
            }
        }
    return importSet;
    }

    /**
     * This function generates header for given interface
     * @param token class to generate header
     * @return {@link java.lang.String} with generated header
     */
    private String getHeader(Class<?> token) {
        StringBuilder header = new StringBuilder();
        String className = token.getSimpleName();
        header.append("public class ")
                .append(className)
                .append("Impl implements ")
                .append(className)
                .append("{")
                .append(LINE_SEPARATOR);
        return header.toString();
    }
    /**
     * This function generates package for given interface
     * @param token class to generate package
     * @return {@link java.lang.String} representation of package
     */
    private String getPackage(Class<?> token) {
        return "package " + token.getPackage().getName() + ";" + LINE_SEPARATOR;
    }

    /**
     * This function is used to check, should we add this class to imports
     * @param token class to check
     * @return true if this class is needed, false otherwise
     */
    private boolean checkClassType(Class<?> token) {
        return (!token.isPrimitive()
                && (!token.getName().startsWith("java.lang")))
                && (!token.getPackage().getName().equals(clazz.getPackage().getName()));
    }

    /**
     * Gets methods of given interface
     * @param token class to get methods
     * @return array of {@link java.lang.reflect.Method}
     */
    private Method[] getMethods(Class<?> token) {
        if (token == null)
            return new Method[0];

        return token.getMethods();
    }

    /**
     * Gets correct string representation of class type
     * @param token class to check
     * @return {@link java.lang.String} representation of class type
     */
    protected String componentTypeToString(Class<?> token) {
        return token.isArray() ? componentTypeToString(token.getComponentType()) + "[]" : token.getName();
    }

    protected Class<?> extractType(Class<?> token) {
        return token.isArray() ? extractType(token.getComponentType()) : token;
    }

    /**
     * Generates code for given {@link java.lang.reflect.Method}
     * @param method method to generate code
     * @return {@link java.lang.String} with generates method's codes
     */
    private String getMethodRealization(Method method) {
        StringBuilder methodText = new StringBuilder();
        int counter = 0;
        methodText.append("    public ").append( componentTypeToString(method.getReturnType()))
                .append(" ").append(method.getName()).append(" (");

        Class<?>[] params = method.getParameterTypes();
        String arg = "arg";
        for (int i = 0; i < params.length; ++i) {
            methodText.append(componentTypeToString(params[i])). append(" ")
                    .append(arg).append(i);
            if(i != params.length - 1)
                methodText.append(",");
        }
        methodText.append(") {" + LINE_SEPARATOR);

        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE)) {
            methodText.append("        ");
            if (returnType.isPrimitive()) {
                if (Boolean.TYPE.equals(returnType)) {
                    methodText.append("return false;");
                }
                else if (Character.TYPE.equals(returnType)) {
                    methodText.append("return '\\0';");
                }
                else {
                    methodText.append("return 0;");
                }
            }
            else {
                methodText.append("return null;");
            }
        }
        methodText.append(LINE_SEPARATOR + "}" + LINE_SEPARATOR);
        return methodText.toString();
    }

    /**
     * Generates class realization
     * @param token class to be realized
     * @return {@link java.lang.String} with generated class' code
     */
    public String generateClassRealization(Class<?> token) {
//        this.clazz = token;
//        this.methods = this.getMethods(this.clazz);
//        this.imports = this.getClassImports(this.clazz);

        StringBuilder classRealization = new StringBuilder();
        classRealization.append(getPackage(token));
        for(Class<?> i : imports) {
            classRealization.append("import ").append(i.getName())
                    .append(";").append(LINE_SEPARATOR);
        }
        classRealization.append(getHeader(token));
        for(Method m : methods) {
            classRealization.append(getMethodRealization(m));
        }
        classRealization.append("}");

        return classRealization.toString();
    }

    /**
     * Generates file to write class' realization. Generated name have suffix "Impl"
     * @param token class to get realization
     * @param path given root path
     * @return {@link java.io.File} to write class realization
     */
    private File getFilename(Class<?> token, Path path) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(path.toString()).append("/")
                .append(token.getPackage().getName().replaceAll("\\.", "/"));
        if (!token.getPackage().getName().isEmpty()) {
            fileName.append("/");
        }
        fileName.append(token.getSimpleName()).append("Impl.java");
        System.out.println(fileName);
        return new File(fileName.toString());
    }

    /**
     * Compiles given .java file
     * @param path path to .java file
     */
    protected void compileClass(File path) {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        String[] args = {"-classpath", System.getProperty("java.class.path")
                , path.toString()};

        try {
            javaCompiler.run(null, null, null, args);
        } catch(Exception e) {
            System.err.println("From compileClass");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Creates .jar file for given .class file
     * @param outputFile file to write .jar
     * @param sourceFile .class file
     */
    protected void createJar(File outputFile, File sourceFile) {

        // dirty hack
        String sourceString = sourceFile.getPath().substring(2); // because i don't need "./"
        System.out.println("sourceString = " + sourceString);
        File newSourceFile = new File(sourceString);
        Manifest manifest = new Manifest();
        OutputStream out = null;
        JarOutputStream jar = null;
        BufferedInputStream in = null;
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try {
            out = new FileOutputStream(outputFile);
        } catch(Exception e) {
            System.err.println("From createJar");
            System.err.println(e.getMessage());
        }
        try {
            jar = new JarOutputStream(out, manifest);
            JarEntry entry = new JarEntry(newSourceFile.getPath().replace("\\", "/"));
            jar.putNextEntry(entry);
            byte[] buffer = new byte[8 * 1024];
            in = new BufferedInputStream(new FileInputStream(newSourceFile));
            while (true)
            {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                jar.write(buffer, 0, count);
            }
            jar.closeEntry();
            in.close();
            jar.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static final void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Bad arguments");
            System.exit(-1);
        } else if((args.length == 3)&&(!args[0].equals("-jar"))) {
            System.out.println("Wrong first argument");
            System.exit(-2);
        }

        Implementor impl = new Implementor();
        try {
            if("-jar".equals(args[0])) {
                impl.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
            else {
                impl.implement(Class.forName(args[0]), Paths.get(args[1]));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

}