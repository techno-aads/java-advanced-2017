package ru.ifmo.ctddev.solutions.implementor;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Stream.of;

/**
 * Class for generate implementor class
 *
 * @author d.kudryashov
 */
public class ImplementorClass extends Implementor {

    @Override
    public JavaFile process(Class<?> clazz) throws IOException, ClassNotFoundException {
        String implementClassName = format("%sImpl", clazz.getSimpleName());
        String implementPackageName = clazz.getPackage().getName();
        List<MethodSpec> methodSpecList = buildMethodList(clazz).stream().distinct().collect(Collectors.toList());
        methodSpecList.addAll(buildConstructorList(clazz).stream().distinct().collect(Collectors.toList()));
        TypeSpec typeSpec = buildTypeSpec(clazz, implementClassName, methodSpecList);
        return JavaFile.builder(implementPackageName, typeSpec).build();
    }

    /**
     * Build list constructor use {@link ImplementorClass#buildConstructor(Constructor)}
     *
     * @param clazz {@link java.lang.Class} for which build {@link List<MethodSpec>}
     * @return {@link List<MethodSpec>} list all of constructor
     */
    private List<MethodSpec> buildConstructorList(Class<?> clazz) {
        Set<Constructor> constructorSet = new HashSet<>(asList(clazz.getConstructors()));
        constructorSet.addAll(asList(clazz.getDeclaredConstructors()));
        return constructorSet.stream()
                .map(this::buildConstructor)
                .collect(Collectors.toList());
    }

    /**
     * Build constructor with modifiers, exceptions, params and body code
     *
     * @param constructor {@link Constructor} for which build {@link MethodSpec}
     * @return {@link MethodSpec} constructor
     */
    private MethodSpec buildConstructor(Constructor constructor) {
        List<ParameterSpec> parameters = of(constructor.getParameters())
                .map(parameter -> ParameterSpec.builder(parameter.getType(), parameter.getName()).build())
                .collect(Collectors.toList());
        List<ClassName> exceptions = of(constructor.getExceptionTypes()).map(ClassName::get).collect(Collectors.toList());
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameters)
                .addExceptions(exceptions)
                .addCode(format("super(%s);", of(constructor.getParameters()).map(Parameter::getName).collect(Collectors.joining(", "))))
                .build();
    }

    /**
     * Build list method use {@link ImplementorClass#buildMethodSpec(Method)}
     *
     * @param clazz {@link Class} for which build {@link List<MethodSpec>}
     * @return {@link List<MethodSpec>} list method
     */
    private List<MethodSpec> buildMethodList(Class<?> clazz) {
        Set<Method> methods = new HashSet<>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            methods.addAll(asList(c.getMethods()));
            methods.addAll(asList(c.getDeclaredMethods()));
        }
        return methods.stream()
                .filter(method -> !method.isDefault() && java.lang.reflect.Modifier.isAbstract(method.getModifiers()))
                .map(this::buildMethodSpec)
                .collect(Collectors.toList());
    }

    /**
     * Build method with modifiers, return type, params and body code
     *
     * @param method {@link Method} for which build {@link MethodSpec}
     * @return {@link MethodSpec} method
     */
    private MethodSpec buildMethodSpec(Method method) {
        List<ParameterSpec> parameters = of(method.getParameters())
                .map(parameter -> ParameterSpec.builder(parameter.getType(), parameter.getName()).build())
                .collect(Collectors.toList());
        return MethodSpec
                .methodBuilder(method.getName())
                .returns(method.getReturnType())
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameters)
                .addCode(buildCode(method))
                .build();
    }

    /**
     * Build all Class code from {@link Class}
     *
     * @param clazz              {@link Class} from for generate
     * @param implementClassName {@link String} Name for class implement
     * @param methodSpecList     {@link List<MethodSpec>} List method for generated class
     * @return {@link TypeSpec} which contains all information for implement class
     */
    private TypeSpec buildTypeSpec(Class<?> clazz, String implementClassName, List<MethodSpec> methodSpecList) {
        return TypeSpec.classBuilder(implementClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(clazz))
                .addMethods(methodSpecList)
                .build();
    }
}
