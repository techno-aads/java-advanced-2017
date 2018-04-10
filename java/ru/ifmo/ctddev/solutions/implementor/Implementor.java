package ru.ifmo.ctddev.solutions.implementor;

import java.nio.file.Path;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.nio.file.*;
import java.util.*;
import java.io.*;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;


/**
 * Implenting classs for interfaces {@link Impler} and {@link JarImpler}
 *  @see info.kgeorgiy.java.advanced.implementor.JarImpler
 *
 * @author Sergey Egorov
 * @version 1.0
 * @since 1.0
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Line Separator for your OS
     */
    final private String LINE_SEPARATOR = System.lineSeparator();
    /**
     * Generated class Name.
     */
    private String ClassName;

    /**
     * Input class or interface to be implemented.
     */
    private Class<?> ImplementedClass;

    /**
     * Default Constructor without any params - doing nothing
     */
    public Implementor(){}

    /**
     * Runs Class-Implementor (implement) or Creates Jar(jarImplement).
     *
     * @param args The array of arguments for Implementor.
     *             2 types of using :
     *             <ul>
     *               <li> -jar fullClassName generatedFilesLocation.  </li>
     *               <li> fullClassName generatedFilesLocation.  </li>
     *             </ul>
     *
     */
    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Not Enough args");
            System.exit(1);
        }

        if (args.length > 3) {
            System.out.println("Too much args");
            System.exit(1);
        }


        if ((args.length == 3)&&(!args[0].equals("-jar"))) {
            System.out.println("Not Correct file extension");
            System.exit(1);
        }

        try {
            if (args[0].equals("-jar")) {
                new Implementor().implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else {
                new Implementor().implement(Class.forName(args[0]), Paths.get(args[1]));
            }
        } catch (ImplerException e) {
            System.out.println("ImplerException: " + e.getMessage());
        } catch (InvalidPathException e) {
            System.out.println("InvalidPathException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("No such class: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Not enough main() arguments");
        }
    }

    /**
     * Implements from input Class or Interface.
     * The generated class will have suffix "Impl".
     *
     * @param _Class Class or Interface to implement.
     * @param _Path   The location for generated class.
     * @throws ImplerException {@link ImplerException} if the given class cannot be generated.
     */
    @Override
    public void implement(Class<?> _Class, Path _Path) throws ImplerException {
        if (_Class.isPrimitive() || _Class.isArray() || _Class == Enum.class) {
            throw new ImplerException("Input Class<> is not  class or interface");
        }

        if (Modifier.isFinal(_Class.getModifiers())) {
            throw new ImplerException("Can't implement final class");
        }

        if(! checkImplementable(_Class)) throw new ImplerException("Этот класс невозможно реализовать");

        String fileName = _Path.toString() + "/";
        fileName += _Class.getPackage().getName().replaceAll("\\.", "/");
        fileName += (_Class.getPackage().getName().isEmpty()) ? "" : "/";

        generateClassSource(_Class, fileName);
    }


    /**
     * Implements the given class and creates Jar file.
     * @param _Class the given class.
     * @param _Path destination of Jar-Archive.
     * @throws ImplerException Exceptions thrown by {@link Implementor#implement(Class, Path)}
     * @see Implementor#implement(Class, Path)
     */
    @Override
    public void implementJar(Class<?> _Class, Path _Path) throws ImplerException {

        if (_Class.isPrimitive() || _Class.isArray() || _Class == Enum.class) {
            throw new ImplerException("Input Class<> is not  class or interface");
        }

        if (Modifier.isFinal(_Class.getModifiers())) {
            throw new ImplerException("Can't implement final class");
        }

        ImplementedClass = _Class;
        ClassName = "tmp/" + _Class.getPackage().getName().replaceAll("\\.", "/");
        ClassName += (_Class.getPackage().getName().isEmpty()) ? "" : "/";

        new File(ClassName).mkdirs();
        String sourceFile = generateClassSource(_Class, ClassName);
        compileFile(sourceFile);

        try {
            createJarFile(sourceFile.replace(".java", ".class"),  _Path);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Produces code implementing class or interface specified by provided <tt>clazz</tt> in specified file.
     * Generated source code implementing <tt>clazz</tt> should be placed in the file specified by <tt>out</tt>.
     *
     * @param clazz type token to create implementation for.
     * @param root   {@link String} where the implementation will be placed.
     * @return filepath of generated file
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException - if an error occurs
     */
    public String generateClassSource(Class<?> clazz, String root) throws ImplerException {

        new File(root).mkdirs();
        String filename = root.toString() + clazz.getSimpleName() + "Impl.java";


        try(
                Writer out = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8);
        ) {

            ImplementedClass = clazz;
            generatePackage(out);
            generateImport(out);
            generateHeader(out);
            generateMethods(out);
            generateConstructors(out);

            out.append("}");

        } catch (IOException e) {
            e.printStackTrace();
        }


        return filename;
    }


    /**
     * If the class ImplementedClass isn't located in default package, prints concatanation of string "package " and given class package name.
     * @param out writer for generating code to file.
     * @throws IOException if an error occured while writing to the destination file via {@link Writer}.
     */
    private void generatePackage(Writer out) throws IOException {
        final String packageName = ImplementedClass.getPackage().getName();
        out.append("package ").append(packageName).append(";").append(LINE_SEPARATOR);
        out.append(LINE_SEPARATOR);
    }

    private void generateImport(Writer out) throws IOException {

        for(Class importClass : findImports(ImplementedClass)){
            if(importClass.isArray()){
                importClass = clearFromArray(importClass);
            }
            out.append("import ").append(importClass.getCanonicalName()).append(";").append(LINE_SEPARATOR);
        }
        out.append(LINE_SEPARATOR);

    }

    /**
     * Prints header of generated ImplementedClass.
     * @param out writer for generating code to file.
     * @throws IOException if an error occured while writing to the destination file via {@link Writer}.
     */
    private void generateHeader(Writer out) throws IOException {
        out.append(printModifier(ImplementedClass.getModifiers()));
        out.append("class ");
        out.append(ImplementedClass.getSimpleName() + "Impl" + " ");
        out.append(ImplementedClass.isInterface() ? "implements " : "extends ");
        out.append(ImplementedClass.getSimpleName() + " {\n");
    }

    /**
     * Implements  constructors of the class/interface ImplementedClass.

     * @param out writer for generating code to file.
     * @throws ImplerException If ImplementedClass is class with no public constructors.
     * @throws IOException if an error occured while writing to the destination file via {@link Writer}.
     */
    private void generateConstructors(Writer out) throws ImplerException, IOException {

        Constructor[] constructors = ImplementedClass.getDeclaredConstructors();
        boolean defaultConstuctor = false;
        if (constructors.length == 0)
            defaultConstuctor = true;
        for (Constructor constructor : constructors) {
            if (Modifier.isPrivate(constructor.getModifiers()))
                continue;
            if (constructor.getParameterTypes().length == 0)
                defaultConstuctor = true;
        }

        if (!defaultConstuctor) {
            int k = 0;
            while ((Modifier.isPrivate(constructors[k].getModifiers())))
                ++k;
            Class[] params = constructors[k].getParameterTypes();
            out.append(LINE_SEPARATOR + "\t" + "public " + ImplementedClass.getSimpleName() + "Impl" + "()");

            if (constructors[k].getExceptionTypes().length != 0) {
                out.append(" throws ");
                Class[] es = constructors[k].getExceptionTypes();
                for (int i = 0; i < es.length; ++i) {
                    out.append(es[i].getSimpleName());
                    if (i < es.length - 1)
                        out.append(", ");
                }
            }
            out.append("{" + LINE_SEPARATOR + "\t" + "\t" + "super(");

            for (int i = 0; i < params.length; ++i) {
                out.append("(" + params[i].getSimpleName() + ")");
                out.append(getDefaultValue(params[i]));
                if (i < params.length - 1)
                    out.append(", ");
            }
            out.append(");" + LINE_SEPARATOR + "\t" + "}" + LINE_SEPARATOR);
        }
    }

    /**
     * Implements all methods from class or interface ImplementedClass and public and protected methods of its superclasses.
     * @param out writer for generating code to file.
     * @throws IOException if an error occured while writing to the destination file via. {@link Writer}
     */
    private void generateMethods(Writer out) throws IOException {

        for (Method m : getMethods(ImplementedClass, false)) {
            int mod = m.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isNative(mod) || Modifier.isPrivate(mod) || !Modifier.isAbstract(mod)) {
                continue;
            }
            mod ^= Modifier.ABSTRACT;
            if (Modifier.isTransient(mod)) {
                mod ^= Modifier.TRANSIENT;
            }
            out.append(LINE_SEPARATOR);
            if (m.isAnnotationPresent(Override.class)) {
                out.append( "\t" + "@Override").append(LINE_SEPARATOR);
            }
            out.append("    ");
            out.append("\t" + Modifier.toString(mod));

            out.append(" " + m.getReturnType().getSimpleName() + " ");
            out.append(m.getName() + "(");
            Class[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; ++i) {
                out.append(params[i].getSimpleName() + " " + "arg" + i);
                if (i < params.length - 1)
                    out.append(", ");
            }
            out.append(")");
            Class[] exceptions = m.getExceptionTypes();

            if (exceptions.length != 0) {
                out.append(" throws ");
                for (int i = 0; i < exceptions.length; ++i) {
                    out.append(exceptions[i].getSimpleName());
                    if (i < exceptions.length - 1) {
                        out.append(", ");
                    }
                }
            }

            out.append("{" + LINE_SEPARATOR + "\t" + "\t" + "return ");
            out.append(getDefaultValue(m.getReturnType()) + ";" + LINE_SEPARATOR + "\t}" + LINE_SEPARATOR);

        }
    }

    /**
     * Checks, is it possible to generate implementation fo given <tt>clazz</tt>.
     *
     * @param clazz type token to check the possibility of generating implementation for.
     * @return true if it is possible to generate implementation
     */
    public boolean checkImplementable(Class<?> clazz){

        boolean result = false;

        for (Constructor c : clazz.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                result = true;
            }
        }
        if (clazz.getDeclaredConstructors().length == 0) {
            result = true;
        }
        if (Modifier.isFinal(clazz.getModifiers())) {
            result = false;
        }
        if (clazz == Enum.class) {
            result = false;
        }
        return result;
    }

    /**
     * Generate string containig modifiers from the given int value {@link Modifier#ABSTRACT},
     * {@link Modifier#TRANSIENT}, {@link Modifier#INTERFACE}.
     *
     * @param modifiers the byte mask of the modifiers.
     * @return The string generated from given int.
     */
    private String printModifier(int modifiers) {
        return Modifier.toString(modifiers & ~(Modifier.ABSTRACT | Modifier.TRANSIENT |
                Modifier.INTERFACE)) + " ";
    }


    /**
     * Provides a type of element by given array(array of arrays)
     *
     * @param clazz the type of array
     * @return a type of array's element
     */
    private Class clearFromArray(Class clazz) {
        if (clazz.getComponentType().isArray()) {
            return clearFromArray(clazz.getComponentType());
        } else {
            return clazz.getComponentType();
        }
    }

    /**
     * Verifies the need for adding class to import list
     *
     * @param importClass class to verify
     * @param  clientClass  implementable class
     * @return true, if import must be added
     */
    private boolean validateImport(Class<?> importClass, Class<?> clientClass){

        if(importClass.isArray()){
            importClass = clearFromArray(importClass);
        }
        if (!importClass.isPrimitive()
                && !importClass.getPackage().getName().startsWith("java.lang")
                && !importClass.getPackage().getName().equals(clientClass.getPackage().getName())){

            return true;
        }
        return false;

    }

    /**
     * Provide a {@link Set} of type token representing classes used in methods an constructors.
     * Note: primitives, classes from the same package(the same to the class ot interface to be implemented)
     * and classes from java.lang are excluded from the result set.
     *
     * @param clazz type token where from all the classes will be retrieved.
     * @return a set of classes
     */
    private Set<Class> findImports(Class<?> clazz) {
        Set<Class> imports = new HashSet<>();

        for (Method method : getMethods(clazz, false)) {

            for (Class paramType : method.getParameterTypes()) {
                if(validateImport(paramType, clazz)){
                    imports.add(paramType);
                }
            }
            if(validateImport(method.getReturnType(), clazz)){
                imports.add(method.getReturnType());
            }
            for (Class exceptionType : Arrays.asList(method.getExceptionTypes())) {
                if(validateImport(exceptionType, clazz)){
                    imports.add(exceptionType);
                }
            }
        }

        for (Constructor ctr : Arrays.asList(clazz.getConstructors())) {
            for (Class paramType : ctr.getParameterTypes()) {
                if(validateImport(paramType, clazz)){
                    imports.add(paramType);
                }
            }
            for (Class exceptionType : Arrays.asList(ctr.getExceptionTypes())) {
                if(validateImport(exceptionType, clazz)){
                    imports.add(exceptionType);
                }
            }
        }
        return imports;

    }

    /**
     * Provides all methods from class, it's super classes und interfaces to be overridden
     *
     * @param clazz Class object to retrieve all methods from it
     * @param inRecursion must be false for first call and true for recursive call
     * @return {@link List} with Method object's, representing methods to be overridden
     *         in requested implementation
     */
    private List<Method> getMethods(Class<?> clazz, boolean inRecursion) {

        List<Method> methods = new ArrayList<>();
        if (clazz == null)
            return methods;

        methods.addAll(getMethods(clazz.getSuperclass(), true));

        for (Class inter : clazz.getInterfaces()) {
            methods.addAll(getMethods(inter, true));
        }

        for (Method m : clazz.getDeclaredMethods()) {

            if (Modifier.isNative(m.getModifiers()) || Modifier.isStatic(m.getModifiers()) || m.isSynthetic() || m.isDefault())
                continue;

            if (Modifier.isPublic(m.getModifiers())
                    || Modifier.isProtected(m.getModifiers())
                    || (!Modifier.isProtected(m.getModifiers()) && !Modifier.isPublic(m.getModifiers())
                    && !Modifier.isPrivate(m.getModifiers()) && ! inRecursion)) {

                if(! checkMethods(m, methods)){
                    methods.add(m);
                }

            }
        }
        return methods;

    }

    /**
     * Replace the superclass method with overrided method
     *
     * @param newMethod method from subclass
     * @param  methods {@link List} list of methods to implement
     * @return true, if the method's signatures are equal, false - otherwise
     */
    private boolean checkMethods(Method newMethod, List<Method> methods){

        m1:for(int i = 0; i< methods.size(); i++){

            if (newMethod.getName().equals(methods.get(i).getName())) {
                Class[] args1 = newMethod.getParameterTypes();
                Class[] args2 = methods.get(i).getParameterTypes();
                if (args1.length == args2.length) {
                    for (int j = 0; j < args1.length; ++j) {
                        if (!args1[j].getCanonicalName().equals(args2[j].getCanonicalName())) {
                            continue m1;
                        }
                    }
                    methods.set(i, newMethod);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Provides a default value corresponding the given type.
     *
     * @param type type token to become default value
     * @return the default value of given type token
     */
    private static String getDefaultValue(Class type) {
        if (type.isPrimitive()) {
            if (Boolean.TYPE.equals(type))
                return "false";
            else if (Void.TYPE.equals(type))
                return "";
            else
                return "0";
        } else
            return "null";
    }

    /**
     * Compiles given java file
     *
     * @param filePath - a {@link String} representing a path to file to be compiled
     */
    public void compileFile(String filePath) {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        String[] args = { filePath, "-cp", filePath + File.pathSeparator + System.getProperty("java.class.path") };

        int exitCode = -1;
        try {
            exitCode = javaCompiler.run(null, null, null, args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (exitCode != 0) {
            System.out.println("Compilation failed, error code: " + exitCode);
        }
    }

    /**
     * Creates Jar file in the given directory and stores the given file in it.
     * @param sourceFile The location of package of java-class that need to be stored in archive.
     * @param jarPath The absolute _Path to java-class.
     * @throws IOException If error occured while using {@link JarOutputStream}
     */
    private void createJarFile(String sourceFile, Path jarPath) throws IOException {

        FileOutputStream fos = null;
        JarOutputStream jarOutputStream = null;
        try {
            File f = new File(jarPath.toUri());

            if(f.getParentFile()!= null)
                f.getParentFile().mkdirs();

            if(f!= null)
                f.createNewFile();
            else
            {
                System.out.println("invalid path to jar file to be created");
                System.exit(1);
            }

            fos = new FileOutputStream(jarPath.toFile());

            String newClassPath = sourceFile.substring(sourceFile.indexOf("tmp/") + 4);

            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            jarOutputStream = new JarOutputStream(fos, manifest);

            jarOutputStream.putNextEntry(new ZipEntry(newClassPath));

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));

            int bytesRead;
            byte[] buffer = new byte[8 * 1024];
            while ((bytesRead = bis.read(buffer)) != -1) {
                jarOutputStream.write(buffer, 0, bytesRead);
            }
            jarOutputStream.closeEntry();
            jarOutputStream.close();
            fos.close();

        } catch (IOException e) {

            jarOutputStream.closeEntry();
            jarOutputStream.close();
            fos.close();
            throw new IOException(e.getMessage());
        }

    }
}
