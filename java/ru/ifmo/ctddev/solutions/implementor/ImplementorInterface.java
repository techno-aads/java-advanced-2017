package ru.ifmo.ctddev.solutions.implementor;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Class for generate implementor interface
 *
 * @author d.kudryashov
 */
public class ImplementorInterface extends Implementor {

    @Override
    public JavaFile process(Class<?> clazz) throws IOException, ClassNotFoundException {
        String implementPackageName = clazz.getPackage().getName();
        String implementClassName = format("%sImpl", clazz.getSimpleName());
        List<MethodSpec> methodSpecList = buildMethodList(clazz);
        TypeSpec typeSpec = buildTypeSpec(clazz, implementClassName, methodSpecList);
        return JavaFile.builder(implementPackageName, typeSpec).build();
    }

    /**
     * Build list method use {@link ImplementorInterface#processMethod(Method)}
     *
     * @param clazz {@link Class} for which build {@link List<MethodSpec>}
     * @return {@link List<MethodSpec>} list method
     */
    private List<MethodSpec> buildMethodList(Class<?> clazz) {
        return Stream.of(clazz.getMethods())
                .filter(method -> !method.isDefault())
                .map(this::processMethod)
                .collect(Collectors.toList());
    }

    /**
     * Build method with modifiers, params and body code
     *
     * @param method {@link Method} for which build {@link MethodSpec}
     * @return {@link MethodSpec} method
     */
    private MethodSpec processMethod(Method method) {
        MethodSpec.Builder methodSpecBuild = MethodSpec
                .methodBuilder(method.getName())
                .returns(method.getReturnType());
        if (java.lang.reflect.Modifier.isAbstract(method.getModifiers())) {
            List<ParameterSpec> parameters = Stream.of(method.getParameters())
                    .map(parameter -> ParameterSpec.builder(parameter.getType(), parameter.getName()).build())
                    .collect(Collectors.toList());
            return methodSpecBuild
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameters)
                    .addCode(buildCode(method))
                    .build();
        }
        return methodSpecBuild
                .addModifiers(findModifier(method.getModifiers()))
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
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(implementClassName)
                .addMethods(methodSpecList);
        typeSpecBuilder.addSuperinterface(ParameterizedTypeName.get(clazz));
        return typeSpecBuilder.build();
    }

    /**
     * Find modifier by modifierCode, look {@link Modifier}
     *
     * @param modifierValue {@link Integer} modifier value
     * @return {@link List<Modifier>} list modifier
     */
    private List<Modifier> findModifier(int modifierValue) {
        String[] modifierList = java.lang.reflect.Modifier.toString(modifierValue).split(" ");
        return Arrays.stream(modifierList)
                .map(String::toUpperCase)
                .map(Modifier::valueOf)
                .collect(Collectors.toList());
    }
}
