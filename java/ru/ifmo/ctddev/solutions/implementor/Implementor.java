package ru.ifmo.ctddev.solutions.implementor;

import com.sun.istack.internal.Nullable;
import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Generate an implementation of passed class and writes It to the specified directory
 *
 * @author andrey
 */
public class Implementor implements Impler {
    /**
     * The name suffix of the generated class implementation
     */
    private static final String IMPL_SUFFIX = "Impl";

    private static final String PUBLIC = " public ";
    private static final String CLASS = " class ";
    private static final String IMPLEMENTS = " implements ";
    private static final String EXTENDS = " extends ";

    private static final String NEXT_LINE = " \n ";
    private static final String SEMICOLON = " ; ";
    private static final String OPEN_BRACE = " { ";
    private static final String CLOSE_BRACE = " } ";
    private static final String BASE_PARAM_NAME = " param";


    /**
     * Store the information about which methods of class already have been processed.
     * <p>
     * Class represented by canonical name string value.
     */
    private final Map<String, Set<String>> methods = new HashMap<>();

    /**
     * Responsible for genetating the implementation of specified class that represented by passed {@link Class}
     * token
     *
     * @param aClass token of the class to implement
     * @param path the destination directory where implementation should be located
     * @throws ImplerException if class cannot be implemented by some reasons
     */
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        if (Modifier.isFinal(aClass.getModifiers()) || aClass == Enum.class) { // todo: enum cannot be implemented
            throw new ImplerException("Class is final or Enum");
        }

        StringBuilder result = new StringBuilder();
        processPackage(aClass, result);
        processBody(aClass, result);
        try {
            writeResultFile(aClass, path, result);
        } catch (IOException e) {
            throw new ImplerException("Error while writing the result file.", e);
        }
    }

    /**
     * Responsible for generation of the body of the target class
     *
     * <p>Append to the {@link StringBuilder} result the class definition including the class modifier, name,
     * inheritance information, constructors methods and It's information.
     *
     * <p>Retrieves the constructor information using {@link #processConstructors} method
     *
     * <p>Retrieves the method information using {@link #processMethods} method
     *
     * @param aClass token of the class to implement
     * @param result {@link StringBuilder} represents the result string in where all generated information
     *                                    will be placed
     * @throws ImplerException if class cannot be implemented by some reasons
     */
    private void processBody(Class<?> aClass, StringBuilder result) throws ImplerException {
        Objects.requireNonNull(result);

        result.append(PUBLIC + CLASS).append(aClass.getSimpleName()).append(IMPL_SUFFIX)
                .append(aClass.isInterface() ? IMPLEMENTS : EXTENDS).append(aClass.getCanonicalName())
                .append(OPEN_BRACE + NEXT_LINE);

        processConstructors(aClass, result);
        processMethods(aClass, result, aClass);

        result.append(CLOSE_BRACE + NEXT_LINE);
    }

    /**
     * Responsible for generating the constructors information of implementing class.
     *
     * <p>Get all non-private constructors of the base class and appends those constructors to
     * implementing class. All constructors has {@code public} modifier regardless the the modifier
     * of corresponding constructor of the base class.
     *
     * <p>Generated constructors has the same {@code throws} section as corresponding constructor in
     * the base class. {@code throws} section retrieving by {@link #getThrowsSection} method
     *
     * <p>All generated constructors do the call to the super constructor passing all received arguments
     *
     * @param aClass token of the class to implement
     * @param result {@link StringBuilder} represents the result string in where all generated information
     *                                    will be placed
     * @throws ImplerException if there are no constructors that can be implemented
     */
    private void processConstructors(Class<?> aClass, StringBuilder result) throws ImplerException {
        Objects.requireNonNull(result);

        int publicConstructors = 0;

        for (Constructor<?> constructor : aClass.getDeclaredConstructors()) {
            if (Modifier.isPrivate(constructor.getModifiers())) {
                continue;
            }
            publicConstructors++;

            result.append(PUBLIC).append(aClass.getSimpleName()).append(IMPL_SUFFIX).append("(");

            int paramNum = 0;
            StringBuilder superValue = new StringBuilder("super(");
            for (Class<?> paramType : constructor.getParameterTypes()) {
                result.append(paramType.getCanonicalName()).append(" ").append(BASE_PARAM_NAME).append(paramNum);
                superValue.append(BASE_PARAM_NAME).append(paramNum++);
                if (paramNum < constructor.getParameterCount()) {
                    result.append(", ");
                    superValue.append(", ");
                }
            }

            StringBuilder throwsSection = getThrowsSection(constructor);
            result.append(")").append(throwsSection).append(OPEN_BRACE).append(NEXT_LINE);
            result.append(superValue).append(")").append(SEMICOLON).append(NEXT_LINE);
            result.append(CLOSE_BRACE + NEXT_LINE);
        }

        if (publicConstructors == 0 && !aClass.isInterface()) {
            throw new ImplerException("No public constructors provided by this class");
        }
    }

    /**
     * Retrieves the information about which type of exception defined in the {@code throws} section
     * of the specofoed method
     *
     * @param someMethod the method to get throws section from
     * @return the string representation of the throws seection
     */
    private StringBuilder getThrowsSection(Executable someMethod) {
        Class<?>[] exceptionTypes = someMethod.getExceptionTypes();
        StringBuilder exceptions = new StringBuilder();
        int index = 0;
        if (exceptionTypes.length > 0) {
            exceptions.append(" throws ");
            for (Class<?> exception : exceptionTypes) {
                exceptions.append(exception.getCanonicalName());
                if (++index < exceptionTypes.length) {
                    exceptions.append(", ");
                }
            }
        }
        return exceptions;
    }

    /**
     * Responsible for generating the methods information of implementing class.
     *
     * <p>Works only with methods of abstract classes and interfaces.
     *
     * <p>If specified {@link Class} token represents the interface, method get all public methods of
     * this interface and public methods of all It's superinterfaces using {@link Class#getMethods} and
     * process them.
     *
     * <p>If specified {@link Class} token represents the class, method get all methods of this class
     * by using the {@link Class#getDeclaredMethods} process then and then do recursive call of
     * {@link #processMethods} for It's superclass and superinterfaces
     *
     * @param aClass token of the class to implement
     * @param result {@link StringBuilder} represents the result string in where all generated information
     *                                    will be placed
     * @param derivedClass the class token that initially should be implemented
     */
    private void processMethods(Class<?> aClass, StringBuilder result, Class<?> derivedClass) {
        if (!Modifier.isAbstract(aClass.getModifiers())) {
            return;
        }

        if (aClass.isInterface()) {
            for (Method method : aClass.getMethods()) {
                processMethod(method, derivedClass, result);
            }
            return;
        }

        for (Method method : aClass.getDeclaredMethods()) {
            processMethod(method, derivedClass, result);
        }

        if (aClass.getSuperclass() != null) {
            processMethods(aClass.getSuperclass(), result, derivedClass);
        }
        for (Class<?> inter : aClass.getInterfaces()) {
            processMethods(inter.getClass(), result, derivedClass);
        }
    }

    /**
     * Override specified method and append this generated methods to the result.
     *
     * <p>Do nothing if specified method is not {@code abstract}.
     *
     * <p>Generate method implementation only if there is such method hasn't been processed yet
     * for specified class. To determine if method already implemented used the {@link #methods} map.
     * This map store the set of implemented methods for each class. The key of this map is a result of
     * {@link Class#getCanonicalName()} method call on specified {@link Class} token.
     *
     * <p>All generated methods has {@code public} modifier. Methods includes the throws similar
     * {@code throws} section as a corresponding method of a superclass and the same return type.
     *
     * <p>Generated methods returns default values of It's return type.     *
     *
     * @param method that should be implemented
     * @param aClass token of the class to implement
     * @param result {@link StringBuilder} represents the result string in where all generated information
     *                                    will be placed
     */
    private void processMethod(Method method, Class<?> aClass, StringBuilder result) {
        Objects.requireNonNull(result);

        if (!Modifier.isAbstract(method.getModifiers())) {
            return;
        }

        String methodIdentifier = getMethodIdentifier(method);

        methods.putIfAbsent(aClass.getCanonicalName(), new HashSet<>());
        if (methods.get(aClass.getCanonicalName()).contains(methodIdentifier)) {
            return;
        }
        methods.get(aClass.getCanonicalName()).add(methodIdentifier);

        StringBuilder exceptions = getThrowsSection(method);

        result.append(PUBLIC).append(method.getReturnType().getCanonicalName())
                .append(" ").append(method.getName()).append("(");

        int paramNum = 0;
        for (Class<?> paramType : method.getParameterTypes()) {
            result.append(paramType.getCanonicalName()).append(" ").append(BASE_PARAM_NAME).append(paramNum++);
            if (paramNum < method.getParameterCount()) {
                result.append(", ");
            }
        }
        result.append(")").append(exceptions).append(OPEN_BRACE).append(NEXT_LINE);

        String defaultValue = getDefaultParameterForType(method.getReturnType());
        if (!Objects.isNull(defaultValue)) {
            result.append("return ").append(defaultValue).append(SEMICOLON).append(NEXT_LINE);
        }
        result.append(CLOSE_BRACE + NEXT_LINE);
    }

    @Nullable
    private String getDefaultParameterForType(Class<?> returnType) {
        if (returnType != void.class) {
            String returnInstance = "null";
            if (returnType == boolean.class) {
                returnInstance = Boolean.FALSE.toString();
            } else if (returnType.isPrimitive()) {
                returnInstance = "0";
            }
            return returnInstance;
        }
        return null;
    }

    private String getMethodIdentifier(Method method) {
        StringBuilder methodIdentifier = new StringBuilder(method.getName());
        for (Class<?> type : method.getParameterTypes()) {
            methodIdentifier.append(type.toString());
        }
        return methodIdentifier.toString();
    }

    private void processPackage(Class<?> aClass, StringBuilder result) {
        Objects.requireNonNull(result).append("package ")
                .append(aClass.getPackage().getName())
                .append(SEMICOLON)
                .append(NEXT_LINE);
    }

    private void writeResultFile(Class<?> aClass, Path path, StringBuilder result) throws IOException {
        Objects.requireNonNull(result);

        String destDirName = path + File.separator + aClass.getPackage().getName().replace(".", File.separator);

        Files.createDirectories(Paths.get(destDirName));
        Files.write(Paths.get(destDirName + File.separator + aClass.getSimpleName() + IMPL_SUFFIX + ".java"),
                result.toString().getBytes());
    }
}
