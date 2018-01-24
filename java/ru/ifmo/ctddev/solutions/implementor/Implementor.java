package ru.ifmo.ctddev.solutions.implementor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Implementor implements Impler {

    /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <tt>$root/java/util/ListImpl.java</tt>
     *
     *
     * @param aClass type token to create implementation for.
     * @param path root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     * generated.
     */
    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        if (aClass.isPrimitive()
                || Enum.class.isAssignableFrom(aClass)
                || Modifier.isFinal(aClass.getModifiers())) {
            throw new ImplerException();
        }
        Set<Method> methodSet = new HashSet<>(Arrays.asList(aClass.getMethods()));
        addMethodsOfClass(aClass, methodSet);
        List<MethodSpec> targetMethods = new ArrayList<>();
        methodSet.forEach(x -> addAllMethods(targetMethods, x));

        Constructor<?>[] constructors = aClass.getDeclaredConstructors();
        boolean shouldWork = false;
        for (Constructor c : constructors) {
            shouldWork = shouldWork || !Modifier.isPrivate(c.getModifiers());
        }
        if (!aClass.isInterface() && !shouldWork) throw new ImplerException();
        Arrays.stream(constructors).forEach(x -> addAllConstructor(targetMethods, x));

        TypeSpec.Builder hello = TypeSpec.classBuilder(aClass.getSimpleName() + "Impl")
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .addMethods(targetMethods);
        if (aClass.isInterface()) {
            hello.addSuperinterface(aClass);
        } else {
            hello.superclass(aClass);
        }

        JavaFile javaFile = JavaFile.builder(
                getPackageNameFromClass(aClass),
                hello.build())
                .build();
        try {
            javaFile.writeTo(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method adds to collection all methods which needs to implement
     *
     * @param c specified class
     * @param collection object in which we collect all methods data
     */
    private static void addMethodsOfClass(Class<?> c, Collection<Method> collection) {
        collection.addAll(Arrays.stream(c.getDeclaredMethods())
                .filter(method -> Modifier.isAbstract(method.getModifiers())
                        && !Modifier.isPublic(method.getModifiers()))
                .collect(Collectors.toList()));
        if (c.getSuperclass() == null) {
            return;
        }
        addMethodsOfClass(c.getSuperclass(), collection);
    }

    /**
     * Method defines list of specified method's modifiers
     *
     * @param x int value, contains bit mask with data of defined modifiers
     * @return list of modifiers defining from int value
     */
    private static List<javax.lang.model.element.Modifier> getModifiers(int x) {
        String[] strings = Modifier.toString(x).split(" ");
        if (strings.length == 0) return new ArrayList<>();
        return Arrays.stream(strings)
                .filter(m -> m.length() > 0)
                .map(m -> javax.lang.model.element.Modifier.valueOf(m.toUpperCase()))
                .filter(m -> !(m.equals(javax.lang.model.element.Modifier.ABSTRACT)
                        || m.equals(javax.lang.model.element.Modifier.TRANSIENT)
                        || m.equals(javax.lang.model.element.Modifier.NATIVE)))
                .collect(Collectors.toList());
    }

    /**
     * Method defines package name from specified class
     *
     * @param aClass object from which we take package name
     * @return String which contains package name
     */
    private static String getPackageNameFromClass(Class<?> aClass) {
        Package aPackage = aClass.getPackage();
        if (Objects.isNull(aPackage)) return "";
        return aPackage.getName();
    }

    /**
     * Method adds to method list data of class's methods
     *
     * @param targetMethods list object in which we put all methods data
     * @param x object of which we take all data of method
     */
    private static void addAllMethods(Collection<MethodSpec> targetMethods, Method x) {
        String s = Modifier.toString(x.getModifiers());
        if (s.contains("final") || s.contains("volatile")) {
            return;
        }
        MethodSpec.Builder method = MethodSpec.methodBuilder(x.getName())
                .returns(x.getReturnType())
                .addModifiers(getModifiers(x.getModifiers()));
        addParametersToMethod(x, method);
        addReturnStatementToMethod(x, method);
        targetMethods.add(method.build());
    }

    /**
     * Method adds to method list data of class's constructors
     *
     * @param targetMethods list object in which we put all constructors data
     * @param x object of which we take all data of constructor
     */
    private static void addAllConstructor(List<MethodSpec> targetMethods, Constructor x) {
        MethodSpec.Builder constr = MethodSpec.constructorBuilder()
                .addModifiers(getModifiers(x.getModifiers()));
        addParametersToMethod(x, constr);
        Arrays.stream(x.getExceptionTypes()).forEach(constr::addException);
        StringBuilder callSuper = new StringBuilder("super(");
        Arrays.stream(x.getParameters()).forEach(p -> callSuper.append(p.getName()).append(", "));
        String result = x.getParameters().length > 0
                ? callSuper.substring(0, callSuper.length() - 2) + ")"
                : callSuper.append(")").toString();
        constr.addStatement(result);
        targetMethods.add(constr.build());
    }

    /**
     * Method adds return statement string equivalent to method builder
     *
     * @param x the object of which we take data return statement
     * @param method the object in which we put return statement string equivalent
     */
    private static void addReturnStatementToMethod(Method x, MethodSpec.Builder method) {
        if (!x.getReturnType().getTypeName().equals("void")) {
            method.addStatement("return " + defineReturn(x.getReturnType()));
        }
    }

    /**
     * Method adds parameters to method builder
     *
     * @param x the object of which we take data of parameters
     * @param method the object in which we put parameters data
     */
    private static void addParametersToMethod(Executable x, MethodSpec.Builder method) {
        Arrays.stream(x.getParameters())
                .forEach(p -> {
                    ParameterSpec parameterSpec = ParameterSpec.builder(p.getType(), p.getName())
                            .addModifiers(getModifiers(p.getModifiers())).build();
                    method.addParameter(parameterSpec);
                });
    }

    /**
     * Method defines default value for specified class
     *
     * @param c class for which we are defining default value
     * @return string equivalent for return value
     */
    private static String defineReturn(Class c) {
        if (c.isPrimitive()) {
            switch (c.getTypeName()) {
                case "boolean" : return "false";
                case "char" : return "'\0'";
                case "byte" : return "(byte) 0";
                case "short" : return "(short) 0";
                case "int" : return "0";
                case "long" : return "0L";
                case "float" : return "0f";
                case "double" : return "0d";
                default: return "null";
            }
        } else {
            return "null";
        }
    }

}
