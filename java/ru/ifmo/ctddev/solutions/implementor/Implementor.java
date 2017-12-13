package ru.ifmo.ctddev.solutions.implementor;


import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * The implementor for classes and interfaces
 *
 * @author Nikiforovykh Danil
 */
public class Implementor implements JarImpler, Impler {

    /**
     * A String value representing line separator of current OS
     */
    private static final String NEW_LINE = System.lineSeparator();

    /**
     * Used for process arguments
     * Produces code implementing class or interface specified by provided
     * full class name
     *
     * @param args - the full class or interface name to be implemented,
     *             and the path where  source code should be placed in
     */
    public static void main(String[] args) {

        if (args.length < 2 || args.length > 3) {
            System.out.println("Неверное число аргументов");
            System.exit(1);
        }

        if ((args.length == 3)&&(!"-jar".equals(args[0]))) {
            System.out.println("Некорректные аргументы");
            System.exit(1);
        }

        String className = "";

        try {
            Implementor  i = new Implementor();

            if (args.length == 3) {
                className = args[1];
                i.implementJar(Class.forName(className), Paths.get(args[2]));
            } else {
                className = args[0];
                i.implement(Class.forName(className), Paths.get(args[1]));
            }
        } catch (ImplerException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.out.println("Класс не найден: " + className);
            System.exit(1);
        }
    }


    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {


        if(! checkImplementable(token)) throw new ImplerException("Этот класс невозможно реализовать");

        String fileName = "tmp/";
        fileName += token.getPackage().getName().replaceAll("\\.", "/");
        fileName += (token.getPackage().getName().isEmpty()) ? "" : "/";

        String sourceFile = generateSource(token, fileName);
        compileFile(sourceFile);

        try {
            packToJar(sourceFile.replace(".java", ".class"),  jarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {

        if(! checkImplementable(token)) throw new ImplerException("Этот класс невозможно реализовать");

        String fileName = root.toString() + "/";
        fileName += token.getPackage().getName().replaceAll("\\.", "/");
        fileName += (token.getPackage().getName().isEmpty()) ? "" : "/";

        generateSource(token, fileName);
    }

    /**
     * Produces code implementing class or interface specified by provided <tt>clazz</tt> in specified file.
     * Generated source code implementing <tt>clazz</tt> should be placed in the file specified by <tt>out</tt>.
     *
     * @param clazz type token to create implementation for.
     * @param root   {@link java.lang.String} where the implementation will be placed.
     * @return filepath of generated file
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException - if an error occurs
     */
    public String generateSource(Class<?> clazz, String root) throws ImplerException {

        new File(root).mkdirs();
        String filename = root.toString() + clazz.getSimpleName() + "Impl.java";
        try(
                //        FileWriter out = new FileWriter(filename, true);
                Writer out = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8);
                //PrintWriter out = new PrintWriter(filename);
        ) {

            final String packageName = clazz.getPackage().getName();
            out.append("package ").append(packageName).append(";").append(NEW_LINE);
            out.append(NEW_LINE);

            for(Class importClass : findImports(clazz)){
                if(importClass.isArray()){
                    importClass = clearFromArray(importClass);
                }
                out.append("import ").append(importClass.getCanonicalName()).append(";").append(NEW_LINE);
            }
            out.append(NEW_LINE);

            out.append("public class " + clazz.getSimpleName() + "Impl");
            if (clazz.isInterface())
                out.append(" implements " + clazz.getSimpleName()).append(" {");
            else
                out.append(" extends " + clazz.getSimpleName()).append(" {");
            out.append(NEW_LINE);

            for (Method m : getMethods(clazz, false)) {
                int mod = m.getModifiers();
                if (Modifier.isFinal(mod) || Modifier.isNative(mod) || Modifier.isPrivate(mod) || !Modifier.isAbstract(mod)) {
                    continue;
                }
                mod ^= Modifier.ABSTRACT;
                if (Modifier.isTransient(mod)) {
                    mod ^= Modifier.TRANSIENT;
                }
                out.append(NEW_LINE);
                if (m.isAnnotationPresent(Override.class)) {
                    out.append("    @Override").append(NEW_LINE);
                }
                out.append("    ");
                out.append(Modifier.toString(mod));

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

                out.append("{").append(NEW_LINE).append("        return ");
                out.append(getDefaultValue(m.getReturnType())).append(";").append(NEW_LINE);
                out.append("    }").append(NEW_LINE);
            }

            Constructor[] c = clazz.getDeclaredConstructors();
            boolean defaultConstuctor = false;
            if (c.length == 0)
                defaultConstuctor = true;
            for (Constructor ctor : c) {
                if (Modifier.isPrivate(ctor.getModifiers()))
                    continue;
                if (ctor.getParameterTypes().length == 0)
                    defaultConstuctor = true;
            }

            if (!defaultConstuctor) {
                int k = 0;
                while ((Modifier.isPrivate(c[k].getModifiers())))
                    ++k;
                Class[] params = c[k].getParameterTypes();
                out.append(NEW_LINE);
                out.append("    public " + clazz.getSimpleName() + "Impl" + "()");
                if (c[k].getExceptionTypes().length != 0) {
                    out.append(" throws ");
                    Class[] es = c[k].getExceptionTypes();
                    for (int i = 0; i < es.length; ++i) {
                        out.append(es[i].getSimpleName());
                        if (i < es.length - 1)
                            out.append(", ");
                    }
                }
                out.append("{").append(NEW_LINE);
                out.append("        super(");
                for (int i = 0; i < params.length; ++i) {
                    out.append("(" + params[i].getSimpleName() + ")");
                    out.append(getDefaultValue(params[i]));
                    if (i < params.length - 1)
                        out.append(", ");
                }
                out.append(");").append(NEW_LINE);
                out.append("    }");
                out.append(NEW_LINE);
            }


            out.append("}");

        } catch (IOException e) {
            e.printStackTrace();
        }


        return filename;
    }

    /**
     * Provide a {@link java.util.Set} of type token representing classes used in methods an constructors.
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
     * @return {@link java.util.List} with Method object's, representing methods to be overridden
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

                if(! replaceDuplicateMethod(m, methods)){
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
     * @param  methods {@link java.util.List} list of methods to implement
     * @return true, if the method's signatures are equal, false - otherwise
     */
    private boolean replaceDuplicateMethod(Method newMethod, List<Method> methods){

        m1:for(int i = 0; i< methods.size(); i++){

            if (newMethod.getName().equals(methods.get(i).getName())) {
                Class[] args1 = newMethod.getParameterTypes();
                Class[] args2 = methods.get(i).getParameterTypes();
                if (args1.length == args2.length) {
                    for (int j = 0; j < args1.length; ++j) {
                        if (!args1[j].getCanonicalName().equals(args2[j].getCanonicalName())) {
                            //return false;
                            continue m1;
                        }
                    }
                    methods.set(i, newMethod);
                    return true;
                }
            }
            //return false;

        }

        return false;

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
     * Provides a default value corresponding the given type.
     * For boolean provides false, for other primitives - 0,
     * and null - otherwise
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
     * Compiles a specified file
     *
     * @param filePath - a {@link java.lang.String} representing a path to file to be compiled
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
            System.out.println("Ошибка компиляции, код: " + exitCode);
        }
    }

    private void packToJar(String sourceFile, Path jarPath) throws IOException {

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
