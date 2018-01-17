package ru.ifmo.ctddev.solutions.implementor;

import com.sun.org.apache.xpath.internal.operations.Mod;
import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Implementor implements Impler {

    private Class<?> clazz;
    private String className;
    private Method[] methods;
    private String packageName;
    private Comparator<Class<?>> CLASS_COMPARATOR = Comparator.comparing(Class::getSimpleName);
    private Set<Class<?>> imports;
    private boolean isClass;

    @Override
    public void implement(Class<?> clazz, Path path) throws ImplerException {
        try {
            File out = getOutputFile(clazz, path);
            PrintWriter writer = new PrintWriter(out);
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
            PrintWriter secondWriter = new PrintWriter(out.getName());
            secondWriter.write(implementation);
            secondWriter.close();
        } catch (Exception e) {
            throw new ImplerException(e);
        }
    }

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

    private String getImplementedClass(Class<?> currentInterface) {
        extractClassParams(currentInterface);
        return generateHeader() + generateBody() + "}";
    }

    private String getExtendedClass(Class<?> currentClass) {
        extractClassParams(currentClass);
        return generateHeader() + generateClassBody() + "}";
    }

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

    private String generateBody() {
        StringBuilder body = new StringBuilder();
        for (Method m : methods) {
            body.append(implementMethod(m));
        }
        return body.toString();

    }

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

    private String implementMethod(Method method) {
        StringBuilder methodStruct = new StringBuilder();
        methodStruct
                .append("    public ")
                .append(method.getReturnType().getSimpleName())
                .append(" ")
                .append(method.getName())
                .append("(" + methodParams(method) + ") {\n");

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

    public String implementConstructors() {
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

    String methodParams(Method m) {
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

    void addImportsFrom(Method method) {
        addImports(method.getReturnType());
        addImportsFrom((Executable) method);
    }

    void addImportsFrom(Executable executable) {
        addImports(executable.getParameterTypes());
        addImports(executable.getExceptionTypes());
    }

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

    private Class<?> extractType(Class<?> aClass) {
        if (aClass.isArray()) {
            aClass = aClass.getComponentType();
        }
        return aClass.isPrimitive() ? null : aClass;
    }

    private File getOutputFile(Class<?> clazz, Path path) throws IOException {
        String classFileName = clazz.getSimpleName() + "Impl.java";
        String[] packages = clazz.getPackage().getName().split("\\.");
        Path outputPath = Paths.get(path.toAbsolutePath().toString(), packages);
        Files.createDirectories(outputPath);
        outputPath = Paths.get(outputPath.toString(), classFileName);
        return outputPath.toFile();
    }
}