package ru.ifmo.ctddev.solutions.implementor;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.util.*;
//import java.lang.*;
import java.io.*;

public class Implementor implements Impler {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private Class<?> clazz;
    private Method[] methods;
    private Set<Class<?>> imports;

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
        } catch(Exception e) {
            throw new ImplerException(e);
        }
    }

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

    private String getPackage(Class<?> token) {
        return "package " + token.getPackage().getName() + ";" + LINE_SEPARATOR;
    }

    private boolean checkClassType(Class<?> token) {
        return (!token.isPrimitive()
                && (!token.getName().startsWith("java.lang")))
                && (!token.getPackage().getName().equals(clazz.getPackage().getName()));
    }

    private Method[] getMethods(Class<?> token) {
        if (token == null)
            return new Method[0];

        return token.getMethods();
    }

    protected String componentTypeToString(Class<?> token) {
        return token.isArray() ? componentTypeToString(token.getComponentType()) + "[]" : token.getName();
    }

    protected Class<?> extractType(Class<?> token) {
        return token.isArray() ? extractType(token.getComponentType()) : token;
    }

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

    private File getFilename(Class<?> token, Path path) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(path.toString()).append("\\\\")
                .append(token.getPackage().getName().replaceAll("\\.", "\\\\"));
        if (!token.getPackage().getName().isEmpty()) {
            fileName.append("\\\\");
        }
        fileName.append(token.getSimpleName()).append("Impl.java");
        System.out.println(fileName);
        return new File(fileName.toString());
    }

    public static final void main(String[] args) {
        Implementor impl = new Implementor();
        try {
            impl.implement(Class.forName("javax.sql.rowset.CachedRowSet"), new File("D:\\Test").toPath());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

}