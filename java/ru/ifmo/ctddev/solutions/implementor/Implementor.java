package ru.ifmo.ctddev.solutions.implementor;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;


public class Implementor implements Impler
{

    private Class<?> classExample; // class example we have to implement
    private String className;
    private Method[] methods;
    private String packageName;
    private Comparator<Class<?>> CLASS_COMPARATOR = Comparator.comparing(Class::getSimpleName);
    private Set<Class<?>> imports;

//---------------------------------------------------------------------------------------------------------------------*
//  Implementing the interface
//---------------------------------------------------------------------------------------------------------------------*
    @Override
    public void implement(Class<?> classExample, Path path) throws ImplerException
    {
        try
        {

            File out = getOutputFile(classExample, path);
            PrintWriter writer = new PrintWriter(out);
            String implementation = getImplementedClass(classExample);

            writer.write(implementation);
            writer.close();

            PrintWriter secondWriter = new PrintWriter(out.getName());

            secondWriter.write(implementation);
            secondWriter.close();
        }
        catch (Exception e)
        {
            throw new ImplerException(e);
        }
    }

//---------------------------------------------------------------------------------------------------------------------*
//  Get required class data and generating the interface declaration.
//---------------------------------------------------------------------------------------------------------------------*
    private String getImplementedClass(Class<?> currentInterface)
    {
        extractClassParams(currentInterface);
        return generateHeader() + generateBody() + "}";
    }

//---------------------------------------------------------------------------------------------------------------------*
//  Reading the data required to implement requested interface code.
//---------------------------------------------------------------------------------------------------------------------*
    private void extractClassParams(Class<?> currentInterface)
    {
        this.classExample = currentInterface;
        this.className = currentInterface.getSimpleName();
        this.packageName = currentInterface.getPackage().getName();
        this.methods = currentInterface.getMethods();
        this.imports = new TreeSet<>(CLASS_COMPARATOR);

        this.imports.add(currentInterface);

        for (Method m : methods)
        {
            addImportsFrom(m);
        }
    }
//---------------------------------------------------------------------------------------------------------------------*
//  Methods for getting the required package imports
//---------------------------------------------------------------------------------------------------------------------*
    void addImportsFrom(Method method)
    {
        addImports(method.getReturnType());
        addImportsFrom((Executable) method);
    }

    void addImportsFrom(Executable executable)
    {
        addImports(executable.getParameterTypes());
        addImports(executable.getExceptionTypes());
    }

    private void addImports(Class<?>... types)
    {
        this.imports.addAll( Arrays.stream(types)
                             .map(this::getType)
                             .filter(this::isValid)
                             .collect(Collectors.toList())
                            );
    }

//---------------------------------------------------------------------------------------------------------------------*
//  Method that returns the type of interface
//---------------------------------------------------------------------------------------------------------------------*

    private Class<?> getType(Class<?> aClass)
    {
        if (aClass.isArray())
        {
            aClass = aClass.getComponentType();
        }
        return aClass.isPrimitive() ? null : aClass;
    }
//---------------------------------------------------------------------------------------------------------------------*
//  Method that checks if imported packages are valid
//---------------------------------------------------------------------------------------------------------------------*
    private boolean isValid(Class<?> aClass)
    {
        if (aClass == null)
        {
            return false;
        }

        if (aClass.isLocalClass() || aClass.isMemberClass())
        {
            return false;
        }

        Package p = aClass.getPackage();

        return !p.getName().equals("java.lang") && !p.equals(classExample.getPackage());
    }

//---------------------------------------------------------------------------------------------------------------------*
//  Reading the data required to implement requested interface code.
//---------------------------------------------------------------------------------------------------------------------*
    private File getOutputFile(Class<?> classExample, Path path) throws IOException
    {
        String classFileName = classExample.getSimpleName() + "Impl.java";
        String[] packages = classExample.getPackage().getName().split("\\.");
        Path outputPath = Paths.get(path.toAbsolutePath().toString(), packages);

        Files.createDirectories(outputPath);
        outputPath = Paths.get(outputPath.toString(), classFileName);
        return outputPath.toFile();
    }

//---------------------------------------------------------------------------------------------------------------------*
//  Generating the code for the header of the interface declaration
//---------------------------------------------------------------------------------------------------------------------*

    private String generateHeader()
    {
        StringBuilder headerStruct = new StringBuilder();

        headerStruct.append("package ").append(packageName).append(";\n");

        for (Class<?> s : imports)
        {
            headerStruct.append("import ").append(s.getName()).append(";\n");
        }

        headerStruct.append("\n");
        headerStruct.append("public class ").append(className).append("Impl implements ").append(className).append(" {\n");

        return headerStruct.toString();
    }

//---------------------------------------------------------------------------------------------------------------------*
//  Generating the code for the body of the interface declaration
//---------------------------------------------------------------------------------------------------------------------*

    private String generateBody()
    {
        StringBuilder body = new StringBuilder();
        for (Method m : methods)
        {
            body.append(implementMethod(m));
        }
        return body.toString();

    }

//---------------------------------------------------------------------------------------------------------------------*
//  Generating the code for the method of interface
//---------------------------------------------------------------------------------------------------------------------*

    private String implementMethod(Method method)
    {
        StringBuilder methodStruct = new StringBuilder();
        methodStruct.append("    public ").append( method.getReturnType().getSimpleName() )
                    .append(" ").append(method.getName()).append("(" + methodParams(method) + ") {\n");

        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE))
        {
            methodStruct.append("        ");
            if (returnType.isPrimitive())
            {
                if (returnType.equals(Boolean.TYPE))
                {
                    methodStruct.append("return false;\n");
                }
                else if (returnType.equals(Character.TYPE))
                {
                    methodStruct.append("return '\\0';\n");
                }
                else
                {
                    methodStruct.append("return 0;\n");
                }
            }
            else
            {
                methodStruct.append("return null;\n");
            }
        }

        methodStruct.append("    }\n\n");

        return methodStruct.toString();
    }

//---------------------------------------------------------------------------------------------------------------------*
//  Method that returns the string of parameters for method
//---------------------------------------------------------------------------------------------------------------------*
    String methodParams(Method m)
    {
        Class<?>[] params = m.getParameterTypes();

        return IntStream.range(0, params.length).mapToObj( i ->
        {
           String s;
           if (params[i].isArray())
           {
               s = params[i].getComponentType().getName() + "[]";
           }
           else
           {
               s = params[i].getName();
           }

           return s + " arg" + i;

        } ).collect(Collectors.joining(", "));
    }
}
