package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zakhar.razzhivin
 */
public class Implementor implements Impler, JarImpler {

    @Override
    public void implement(Class<?> clazz, Path root) throws ImplerException {
        checkImplementationPossibility(clazz);
        String fileName = root.toAbsolutePath() + "/";
        fileName += clazz.getPackage().getName().replaceAll("\\.", "/");
        fileName += (clazz.getPackage().getName().isEmpty()) ? "" : "/";
        (new File(fileName)).mkdirs();
        fileName += clazz.getSimpleName() + "Impl.java";

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileName, true))) {
            generateImplementation(clazz, writer);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        checkImplementationPossibility(token);
        String fileName;
        fileName = "tmp/";
        fileName += token.getPackage().getName().replaceAll("\\.", "/");
        fileName += (token.getPackage().getName().isEmpty()) ? "" : "/";
        (new File(fileName)).mkdirs();
        fileName += token.getSimpleName() + "Impl.java";

        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(fileName, true));
            Implementor.generateImplementation(token, writer);
            writer.close();
            JarHelper.compileFile(fileName);
            JarHelper.createJar(jarFile.toString(), fileName.substring(0, fileName.indexOf(".java")) + ".class");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Check possibility to generate class implementation
     * if generation is impossible throw {@link info.kgeorgiy.java.advanced.implementor.ImplerException}
     * otherwise does nothing
     * @param clazz class type for checking
     * @throws ImplerException if generating is impossible
     */
    private void checkImplementationPossibility(Class clazz) throws ImplerException {
        boolean flag = true;
        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                flag = false;
            }
        }

        if (clazz.getDeclaredConstructors().length == 0) {
            flag = false;
        }

        if (flag || Modifier.isFinal(clazz.getModifiers()) || clazz.getCanonicalName().equals("java.lang.Enum")) {
            throw new ImplerException();
        }
    }

    /**
     * Generate class implementation
     * @param clazz class type for generating
     * @param out output stream
     * @throws IOException
     */
    private static void generateImplementation(Class clazz, Appendable out) throws IOException {
        final String packageName = clazz.getPackage().getName();
        generatePackages(packageName, out);
        out.append(System.lineSeparator());
        generateImports(clazz, out);
        out.append(System.lineSeparator());
        generateClassDeclaration(clazz, out);
        generateConstructors(clazz, out);
        generateMethods(clazz, out);
        out.append("}").append(System.lineSeparator());
    }

    /**
     * Generate part for packages
     * @param packageName name of package
     * @param out output stream
     * @throws IOException
     */
    private static void generatePackages(String packageName, Appendable out) throws IOException {
        out.append("package ").append(packageName).append(";").append(System.lineSeparator());
    }

    /**
     * Generate part of imports
     * @param clazz class type for generating
     * @param out output stream
     * @throws IOException
     */
    private static void generateImports(Class clazz, Appendable out) throws IOException {
        for (Class c : findUsedClasses(clazz)) {
            out.append("import ").append(c.getCanonicalName()).append(";").append(System.lineSeparator());
        }
    }

    /**
     * Generate declaration of class
     * @param clazz class type for generating
     * @param out output stream
     * @throws IOException
     */
    private static void generateClassDeclaration(Class clazz, Appendable out) throws IOException {
        out.append("public class ").append(clazz.getSimpleName()).append("Impl");
        if (clazz.isInterface()) {
            out.append(" implements ").append(clazz.getSimpleName()).append(" {");
        } else {
            out.append(" extends ").append(clazz.getCanonicalName()).append(" {");
        }
    }

    /**
     * Generate constructors
     * @param clazz class type for generating
     * @param out output stream
     * @throws IOException
     */
    private static void generateConstructors(Class clazz, Appendable out) throws IOException {
        Constructor[] c = clazz.getDeclaredConstructors();
        boolean defaultConstructor = false;
        if (c.length == 0) {
            defaultConstructor = true;
        }
        for (Constructor ctor : c) {
            if (Modifier.isPrivate(ctor.getModifiers())) {
                continue;
            }
            if (ctor.getParameterTypes().length == 0) {
                defaultConstructor = true;
            }
        }

        if (!defaultConstructor) {
            int k = 0;
            while (Modifier.isPrivate(c[k].getModifiers())) {
                ++k;
            }
            Class[] params = c[k].getParameterTypes();
            out.append(System.lineSeparator());
            out.append("    public " + clazz.getSimpleName() + "Impl()");
            if (c[k].getExceptionTypes().length != 0) {
                out.append(" throws ");
                Class[] es = c[k].getExceptionTypes();
                for (int i = 0; i < es.length; ++i) {
                    out.append(es[i].getSimpleName());
                    if (i < es.length - 1)
                        out.append(", ");
                }
            }
            out.append("{").append(System.lineSeparator());
            out.append("        super(");
            for (int i = 0; i < params.length; ++i) {
                out.append("(").append(params[i].getSimpleName()).append(")");
                out.append(getDefault(params[i]));
                if (i < params.length - 1)
                    out.append(", ");
            }
            out.append(");").append(System.lineSeparator());
            out.append("    }");
            out.append(System.lineSeparator());
        }
    }

    /**
     * Generate methods
     * @param clazz class type for generating
     * @param out output stream
     * @throws IOException
     */
    private static void generateMethods(Class clazz, Appendable out) throws IOException {
        Set<String> methodsDescriptions = new HashSet<>();
        for (Method m : getInheritedMethods(clazz)) {
            StringBuilder methodDescription = new StringBuilder();
            int mod = m.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isNative(mod) || Modifier.isPrivate(mod) || !Modifier.isAbstract(mod)) {
                continue;
            }
            mod ^= Modifier.ABSTRACT;
            if (Modifier.isTransient(mod)) {
                mod ^= Modifier.TRANSIENT;
            }
            methodDescription.append(System.lineSeparator());
            methodDescription.append("    ");
            methodDescription.append(Modifier.toString(mod));

            methodDescription.append(" ").append(m.getReturnType().getSimpleName()).append(" ");
            methodDescription.append(m.getName()).append("(");
            Class[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; ++i) {
                methodDescription.append(params[i].getSimpleName()).append(" ").append("arg").append(String.valueOf(i));
                if (i < params.length - 1)
                    methodDescription.append(", ");
            }
            methodDescription.append(")");
            Class[] exceptions = m.getExceptionTypes();

            if (exceptions.length != 0) {
                methodDescription.append(" throws ");
                for (int i = 0; i < exceptions.length; ++i) {
                    methodDescription.append(exceptions[i].getSimpleName());
                    if (i < exceptions.length - 1) {
                        methodDescription.append(", ");
                    }
                }
            }

            methodDescription.append("{").append(System.lineSeparator()).append("        return ");
            methodDescription.append(getDefault(m.getReturnType())).append(";").append(System.lineSeparator());
            methodDescription.append("    }").append(System.lineSeparator());
            methodsDescriptions.add(methodDescription.toString());
        }
        for (String methodsDescription : methodsDescriptions) {
            out.append(methodsDescription);
        }
    }

    /**
     * Find classes which are used
     * @param clazz class type for generating
     * @return classes set
     */
    private static Set<Class> findUsedClasses(Class clazz) {
        Set<Class> classes = new HashSet<Class>();
        for (Method method : getMethods(clazz)) {
            for (Class paramType : method.getParameterTypes()) {
                if (paramType.isArray()) {
                    Class cls = getFromArray(paramType);
                    if (!cls.isPrimitive())
                        classes.add(cls);
                } else if (!paramType.isPrimitive()
                        && !paramType.getPackage().getName().startsWith("java.lang")
                        && !paramType.getPackage().getName().equals(clazz.getPackage().getName())
                        ) {
                    classes.add(paramType);
                }
            }

            if (method.getReturnType().isArray()) {
                Class cls = getFromArray(method.getReturnType());
                if (!cls.isPrimitive())
                    classes.add(cls);
            } else if (!method.getReturnType().isPrimitive()
                    && !method.getReturnType().getPackage().getName().startsWith("java.lang")
                    && !method.getReturnType().getPackage().getName().equals(clazz.getPackage().getName())) {
                classes.add(method.getReturnType());
            }

            for (Class e : Arrays.asList(method.getExceptionTypes())) {
                if (e.isArray()) {
                    Class cls = getFromArray(e);
                    if (!cls.isPrimitive())
                        classes.add(cls);
                } else if (!e.isPrimitive()
                        && !e.getPackage().getName().startsWith("java.lang")
                        && !e.getPackage().getName().equals(clazz.getPackage().getName())
                        ) {
                    classes.add(e);
                }
            }

        }

        for (Constructor ctr : Arrays.asList(clazz.getConstructors())) {
            for (Class paramType : ctr.getParameterTypes()) {
                if (paramType.isArray()) {
                    Class cls = getFromArray(paramType);
                    if (!cls.isPrimitive())
                        classes.add(cls);
                } else if (!paramType.isPrimitive()
                        && !paramType.getPackage().getName().startsWith("java.lang")
                        && !paramType.getPackage().getName().equals(clazz.getPackage().getName())
                        ) {
                    classes.add(paramType);
                }
            }

            for (Class e : Arrays.asList(ctr.getExceptionTypes())) {
                if (e.isArray()) {
                    Class cls = getFromArray(e);
                    if (!cls.isPrimitive())
                        classes.add(cls);
                } else if (!e.isPrimitive()
                        && !e.getPackage().getName().startsWith("java.lang")
                        && !e.getPackage().getName().equals(clazz.getPackage().getName())
                        ) {
                    classes.add(e);
                }
            }
        }
        return classes;
    }

    /**
     * Util method for determining class type
     * @param arrayType target type
     * @return class type
     */
    private static Class getFromArray(Class arrayType) {
        if (arrayType.getComponentType().isArray()) {
            return getFromArray(arrayType.getComponentType());
        } else {
            return arrayType.getComponentType();
        }
    }

    /**
     * Util method for getting default value
     * @param type target type
     * @return default value
     */
    private static String getDefault(Class type) {
        if (type.isPrimitive()) {
            if (Boolean.TYPE.equals(type)) {
                return "false";
            } else if (Void.TYPE.equals(type)) {
                return "";
            } else {
                return "0";
            }
        } else {
            return "null";
        }
    }

    /**
     * Get class's methods
     * @param clazz target class
     * @return array of class's methods
     */
    private static Method[] getMethods(Class clazz) {
        Set<Method> methods = new HashSet<>();
        methods.addAll(Arrays.asList(clazz.getMethods()));
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        return methods.toArray(new Method[methods.size()]);
    }

    /**
     * Get inherited class's methods
     * @param clazz target class
     * @return array of inherited class's methods
     */
    private static Method[] getInheritedMethods(Class clazz) {
        Set<Method> methods = new HashSet<>();
        for (Class c = clazz; c != null; c = c.getSuperclass()) {
            methods.addAll(Arrays.asList(c.getMethods()));
            methods.addAll(Arrays.asList(c.getDeclaredMethods()));
        }
        return methods.toArray(new Method[methods.size()]);
    }
}
