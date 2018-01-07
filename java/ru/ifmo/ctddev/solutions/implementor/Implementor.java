package ru.ifmo.ctddev.solutions.implementor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import javax.sql.rowset.CachedRowSet;

public class Implementor implements Impler {

    private Class<?> clazz;
    private String className;
    private Method[] methods;
    private String packageName;
    private Comparator<Class<?>> CLASS_COMPARATOR = Comparator.comparing(Class::getSimpleName);
    private Set<Class<?>> imports;

    @Override
    public void implement(Class<?> clazz, Path path) throws ImplerException {
        try {
            File out = getOutputFile(clazz, path);
            PrintWriter writer = new PrintWriter(out);
            String implementation = getImplementedClass(clazz);
            writer.write(implementation);
            writer.close();
//            PrintWriter secondWriter = new PrintWriter(out.getName());
//            secondWriter.write(implementation);
//            secondWriter.close();
        } catch (Exception e) {
            throw new ImplerException(e);
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

    private String generateHeader() {
        StringBuilder headerStruct = new StringBuilder();
        headerStruct
                .append("package ").append(packageName).append(";\n");
        for (Class<?> s : imports) {
            headerStruct.append("import ").append(s.getName()).append(";\n");
        }
        headerStruct.append("\n");
        headerStruct.append("public class ").append(className)
                .append("Impl implements ").append(className).append(" {\n");

        return headerStruct.toString();
    }

    private String generateBody() {
        StringBuilder body = new StringBuilder();
        for (Method m : methods) {
            body.append(implementMethod(m));
        }
        return body.toString();

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
