package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Implementor implements Impler {

    Set<String> methodId = new HashSet<>();

    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        StringBuilder result = new StringBuilder();

        if (Modifier.isFinal(aClass.getModifiers())) {
            throw new ImplerException("Class is final");
        }
        if (aClass.isEnum() || aClass == Enum.class) {
            throw new ImplerException("Class is Enum");
        }
        if (aClass.isPrimitive()) {
            throw new ImplerException("Class is Primitive");
        }
        setPackage(aClass, result);
        setClass(aClass, result);

        try {
            writeResultFile(aClass, path, result);
            methodId = new HashSet<>();
        } catch (IOException e) {
            throw new ImplerException("Error while writing the result file.", e);
        }
    }

    private void setPackage(Class<?> aClass, StringBuilder result) {
        result.append("package ")
                .append(aClass.getPackage().getName())
                .append(";\n");
    }

    private void setClass(Class<?> aClass, StringBuilder result) throws ImplerException {
        result.append("public class ")
                .append(aClass.getSimpleName())
                .append("Impl ")
                .append(aClass.isInterface() ? "implements " : "extends ")
                .append(aClass.getName())
                .append(" {\n");

        setConstructors(aClass, result);
        setMethods(aClass, result);

        result.append("}");
    }

    private void setConstructors(Class<?> aClass, StringBuilder result) throws ImplerException {
        boolean hasConstructors = false;

        for (Constructor constructor : aClass.getDeclaredConstructors()) {
            if (Modifier.isPrivate(constructor.getModifiers())) {
                continue;
            }
            hasConstructors = true;

            result.append("public ")
                    .append(aClass.getSimpleName())
                    .append("Impl (");

            int countParam = constructor.getParameterCount();
            int counter = 1;

            StringBuilder params = new StringBuilder();

            for (Parameter parameter : constructor.getParameters()) {
                result.append(parameter.getParameterizedType().getTypeName())
                        .append(" ")
                        .append(parameter.getName());
                params.append(parameter.getName());

                if (countParam > counter++) {
                    result.append(", ");
                    params.append(", ");
                }
            }
            result.append(") ");

            int countExc = constructor.getExceptionTypes().length;
            int counterExc = 1;
            if (countExc > 0) result.append("throws ");
            for (Class<?> exception : constructor.getExceptionTypes()) {
                result.append(exception.getCanonicalName());
                if (countExc > counterExc++) result.append(", ");
            }

            result.append(" {\n")
                    .append("super(")
                    .append(params)
                    .append(");\n")
                    .append("}\n");
        }

        if (!hasConstructors && !aClass.isInterface()) {
            throw new ImplerException("Has not public constructors");
        }
    }

    private void setMethods(Class<?> aClass, StringBuilder result) {

        if (aClass.isInterface()) {
            setMethods(aClass.getMethods(), result);
            return;
        }

        setMethods(aClass.getDeclaredMethods(), result);

        if (aClass.getSuperclass() != null) {
            setMethods(aClass.getSuperclass(), result);
        }
    }

    private String getMethodId(Method method) {
        StringBuilder methodId = new StringBuilder(method.getName());
        for (Class<?> type : method.getParameterTypes()) {
            methodId.append(type.toString());
        }
        return methodId.toString();
    }

    private void setMethods(Method[] methods, StringBuilder result) {
        for (Method method : methods) {

            int modifiers = method.getModifiers();

            if (!Modifier.isAbstract(modifiers) || Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers)
                    || Modifier.isStatic(modifiers) || methodId.contains(getMethodId(method))) {
                continue;
            }

            methodId.add(getMethodId(method));

            result.append("public ")
                    .append(method.getReturnType().getCanonicalName())
                    .append(" ")
                    .append(method.getName())
                    .append(" (");

            int countParam = method.getParameterCount();
            int counter = 1;
            for (Class<?> parameterType : method.getParameterTypes()) {
                result.append(parameterType.getCanonicalName())
                        .append(" ")
                        .append("arg")
                        .append(counter);

                if (countParam > counter++)
                    result.append(", ");
            }
            result.append(") ");

            int countExc = method.getExceptionTypes().length;
            int counterExc = 1;
            if (countExc > 0) result.append("throws ");
            for (Class<?> exception : method.getExceptionTypes()) {
                result.append(exception.getCanonicalName());
                if (countExc > counterExc++) result.append(", ");
            }

            result.append(" {\n")
                    .append("return")
                    .append(" ")
                    .append(getDefaultParameterForType(method.getReturnType()))
                    .append(";\n")
                    .append("}\n");
        }
    }

    private String getDefaultParameterForType(Class<?> returnType) {
        String returnValue = "";
        if (returnType != void.class) {
            returnValue = "null";
            if (returnType == boolean.class) {
                returnValue = Boolean.FALSE.toString();
            } else if (returnType.isPrimitive()) {
                returnValue = "0";
            }
        }
        return returnValue;
    }

    private void writeResultFile(Class<?> aClass, Path path, StringBuilder result) throws IOException {
        String destDirName = path + File.separator + aClass.getPackage().getName().replace(".", File.separator);

        Files.createDirectories(Paths.get(destDirName));
        Files.write(Paths.get(destDirName + File.separator + aClass.getSimpleName() + "Impl.java"),
                result.toString().getBytes());
    }
}
