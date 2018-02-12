package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * The Implementor class provides methods to create basic implementation of specified class.
 */
public class Implementor implements JarImpler {
    
    /**
     * An indent used in code generation
     */
    private static final String INDENT = "    ";
    
    /**
     * Does nothing.
     */
    public Implementor() {
    
    }
    
    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     *
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path root = jarFile.getParent().toAbsolutePath();
        
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Java compiler is missing");
        }
        
        String[] args = new String[] { "-cp", System.getProperty("java.class.path"), generateSourceCode(token, jarFile.getParent()).toString() };
        
        compiler.run(null, null, null, args);
        
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        
        String packagePath = token.getPackage().getName().replace(".", "/");
        String jarEntryPath = packagePath + "/" + token.getSimpleName() + "Impl.class";
        
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            JarEntry jarEntry = new JarEntry(jarEntryPath);
            jos.putNextEntry(jarEntry);
            Files.copy(root.resolve(jarEntryPath), jos);
        } catch (IOException e) {
            throw new ImplerException(e.getMessage());
        }
    }
    
    /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <tt>$root/java/util/ListImpl.java</tt>
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     *
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        generateSourceCode(token, root);
    }
    
    /**
     * Generates implementation of a class specified by <tt>token</tt>.
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     *
     * @return path to created file
     *
     * @throws ImplerException when implementation cannot be generated.
     */
    private Path generateSourceCode(Class<?> token, Path root) throws ImplerException {
        if (token == Enum.class) {
            throw new ImplerException("Class " + token.getCanonicalName() + " is an enum");
        }
        
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Class " + token.getCanonicalName() + " is final");
        }
        
        Map<String, Constructor> constructors = getConstructors(token);
        if (!token.isInterface() && constructors.size() == 0) {
            throw new ImplerException("Class " + token.getCanonicalName() + " has no accessible constructors");
        }
        
        Map<String, Method> methods = getMethods(token);
        
        Set<Class<?>> imports = getImports(token);
        
        Path pathToFile = Paths.get(root.toAbsolutePath() + File.separator + token.getCanonicalName().replace(".", File.separator) + "Impl.java");
        try {
            Files.createDirectories(pathToFile.getParent());
        } catch (IOException e) {
            throw new ImplerException(e.getMessage());
        }
        
        try (BufferedWriter bw = Files.newBufferedWriter(pathToFile, StandardCharsets.UTF_8)) {
            bw.write("package ");
            bw.write(token.getPackage().getName());
            bw.write(";");
            
            bw.newLine();
            bw.newLine();
            
            writeImports(bw, imports);
            
            bw.newLine();
            
            bw.write("public class ");
            bw.write(token.getSimpleName());
            bw.write("Impl ");
            bw.write(token.isInterface() ? "implements " : "extends ");
            bw.write(token.getSimpleName());
            bw.write(" {");
            
            bw.newLine();
            
            writeConstructors(bw, constructors);
            
            writeMethods(bw, methods);
            
            bw.write("}");
        } catch (IOException e) {
            throw new ImplerException(e.getMessage());
        }
        
        return pathToFile;
    }
    
    /**
     * Writes imports to the specified BufferedWriter.
     *
     * @param bw      a BufferedWriter that writes to the new source file
     * @param imports a Set of imports
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeImports(BufferedWriter bw, Set<Class<?>> imports) throws IOException {
        for (Class<?> anImport : imports) {
            bw.write("import ");
            bw.write(anImport.getCanonicalName());
            bw.write(";");
            bw.newLine();
        }
    }
    
    /**
     * Writes implementation of constructors to the specified BufferedWriter.
     *
     * @param bw           a BufferedWriter that writes to the new source file
     * @param constructors a map that contains constructors mapped by its signatures
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeConstructors(BufferedWriter bw, Map<String, Constructor> constructors) throws IOException {
        for (Map.Entry<String, Constructor> entry : constructors.entrySet()) {
            Constructor constructor = entry.getValue();
            String header = entry.getKey();
            
            bw.newLine();
            bw.write(INDENT);
            bw.write(Modifier.toString(constructor.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT));
            bw.write(" ");
            bw.write(header);
            bw.write(concatExceptions(constructor.getExceptionTypes()));
            bw.write(" { ");
            bw.newLine();
            bw.write(INDENT);
            bw.write(INDENT);
            bw.write("super(");
            
            bw.write(Arrays.stream(constructor.getParameters())
                           .map(Parameter::getName)
                           .collect(Collectors.joining(", ")));
            
            bw.write(");");
            bw.newLine();
            bw.write(INDENT);
            bw.write("}");
            bw.newLine();
        }
    }
    
    /**
     * Writes implementation of constructors to the specified BufferedWriter.
     *
     * @param bw      a BufferedWriter that writes to the new source file
     * @param methods a map that contains methods mapped by its signatures
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeMethods(BufferedWriter bw, Map<String, Method> methods) throws IOException {
        for (Map.Entry<String, Method> entry : methods.entrySet()) {
            Method method = entry.getValue();
            String signature = entry.getKey();
            int modifiers = method.getModifiers();
            Class<?> returnType = method.getReturnType();
            
            bw.newLine();
            bw.write(INDENT);
            bw.write("@Override");
            bw.newLine();
            bw.write(INDENT);
            bw.write(Modifier.toString(modifiers & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT & ~Modifier.NATIVE));
            bw.write(" ");
            bw.write(returnType.getSimpleName());
            bw.write(" ");
            bw.write(signature);
            bw.write(concatExceptions(method.getExceptionTypes()));
            bw.write(" {");
            bw.newLine();
            bw.write(INDENT);
            bw.write(INDENT);
            bw.write(getDefaultReturnValue(entry.getValue()));
            bw.newLine();
            bw.write(INDENT);
            bw.write("}");
            bw.newLine();
        }
    }
    
    /**
     * Retrieves all imports from a class.
     *
     * @param token a class to retrieve imports from
     *
     * @return a new set of imports
     */
    private Set<Class<?>> getImports(Class<?> token) {
        Package classPackage = token.getPackage();
        
        Set<Class<?>> imports = new HashSet<>();
        
        for (Map.Entry<String, Constructor> entry : getConstructors(token).entrySet()) {
            Constructor constructor = entry.getValue();
            for (Class<?> clazz : constructor.getParameterTypes()) {
                tryAddImport(imports, classPackage, clazz);
            }
            
            for (Class<?> clazz : constructor.getExceptionTypes()) {
                tryAddImport(imports, classPackage, clazz);
            }
        }
        
        for (Map.Entry<String, Method> entry : getMethods(token).entrySet()) {
            Method method = entry.getValue();
            for (Class<?> clazz : method.getParameterTypes()) {
                tryAddImport(imports, classPackage, clazz);
            }
            
            tryAddImport(imports, classPackage, method.getReturnType());
            
            for (Class<?> clazz : method.getExceptionTypes()) {
                tryAddImport(imports, classPackage, clazz);
            }
        }
        
        return imports;
    }
    
    /**
     * Attempts to add an import to the specified set of imports.
     *
     * @param imports      the Set of imports
     * @param classPackage the package of the class being implemented
     * @param anImport     import to add
     */
    private void tryAddImport(Set<Class<?>> imports, Package classPackage, Class<?> anImport) {
        if (anImport.isArray()) {
            anImport = extractArrayType(anImport);
        }
        
        if (anImport.getPackage() != classPackage && !anImport.isPrimitive() && !anImport.getPackage().getName().startsWith("java.lang")) {
            imports.add(anImport);
        }
    }
    
    /**
     * Extracts type from an array.
     *
     * @param token an array class
     *
     * @return the Class representing the component type of this class if this class is an array; <tt>token</tt> otherwise.
     */
    private Class<?> extractArrayType(Class<?> token) {
        if (token.isArray()) {
            if (token.getComponentType().isArray()) {
                return extractArrayType(token.getComponentType());
            } else {
                return token.getComponentType();
            }
        } else {
            return token;
        }
    }
    
    /**
     * Retrieves all constructors from a class.
     *
     * @param token a class to retrieve constructors from
     *
     * @return new Map that contains methods mapped by its signatures
     */
    private Map<String, Constructor> getConstructors(Class<?> token) {
        Map<String, Constructor> constructors = new HashMap<>();
        
        for (Constructor constructor : token.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                constructors.put(token.getSimpleName() + "Impl(" + concatParameters(constructor.getParameters()) + ")", constructor);
            }
        }
        
        return constructors;
    }
    
    /**
     * Retrieves all methods from a class.
     *
     * @param token a class to retrieve methods from
     *
     * @return new Map that contains methods mapped by its signatures
     */
    private Map<String, Method> getMethods(Class<?> token) {
        Map<String, Method> filteredMethods = new HashMap<>();
        
        Method[] methods;
        if (token.isInterface()) {
            methods = token.getMethods();
        } else {
            methods = token.getDeclaredMethods();
            
            Class<?> superclass = token.getSuperclass();
            if (superclass != null) {
                filteredMethods.putAll(getMethods(superclass));
            }
        }
        
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (Modifier.isAbstract(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isPrivate(modifiers)) {
                filteredMethods.put(method.getName() + "(" + concatParameters(method.getParameters()) + ")", method);
            }
        }
        
        return filteredMethods;
    }
    
    /**
     * Concatenates the pairs of names and types of each parameter.
     *
     * @param parameters a Parameter array
     *
     * @return concatenation result
     */
    private String concatParameters(Parameter[] parameters) {
        return Arrays.stream(parameters)
                     .map(p -> p.getType().getSimpleName() + " " + p.getName())
                     .collect(Collectors.joining(", "));
    }
    
    /**
     * Constructs a String that contains concatenated types of exceptions.
     *
     * @param exceptions an Exception array
     *
     * @return concatenated types of exceptions; an empty String otherwise
     */
    private String concatExceptions(Class<?>[] exceptions) {
        if (exceptions.length == 0) {
            return "";
        }
        
        return " throws " + Arrays.stream(exceptions)
                                  .map(Class::getSimpleName)
                                  .collect(Collectors.joining(", "));
    }
    
    /**
     * Returns the default return value of the <tt>method</tt>
     *
     * @param method the method
     *
     * @return default value
     */
    private String getDefaultReturnValue(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            if (Void.TYPE == returnType) {
                return "";
            } else if (Boolean.TYPE == returnType) {
                return "return false;";
            } else {
                return "return 0;";
            }
        } else {
            return "return null;";
        }
    }
    
    /**
     * The main entry point for the application.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 3) {
            System.out.println("Usage: Implementor <full class name> <root path>");
            System.out.println("\t\t(to implement a class)");
            System.out.println("\tor Implementor -jar <full class name> <root path>");
            System.out.println("\t\t(to implement a jar)");
            return;
        }
        
        try {
            if (args.length == 3) {
                if (args[0].equals("-jar")) {
                    new Implementor().implementJar(Class.forName(args[1]), Paths.get(args[2]));
                } else {
                    System.out.println("Unrecognized option: " + args[0]);
                }
            } else {
                new Implementor().implement(Class.forName(args[0]), Paths.get(args[1]));
            }
        } catch (ImplerException | InvalidPathException | ClassNotFoundException e) {
            System.out.println(e.toString());
        }
    }
}