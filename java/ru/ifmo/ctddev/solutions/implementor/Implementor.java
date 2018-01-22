package ru.ifmo.ctddev.solutions.implementor;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public abstract class Implementor {
    final static Map<Class<?>, String> defaultValues = new HashMap<>();

    static {
        defaultValues.put(int.class, "0");
        defaultValues.put(long.class, "0L");
        defaultValues.put(char.class, "'\0'");
        defaultValues.put(boolean.class, "true");
        defaultValues.put(byte.class, "0");
        defaultValues.put(float.class, "0f");
        defaultValues.put(double.class, "0d");
        defaultValues.put(short.class, "0");
    }

    public abstract JavaFile process(Class<?> clazz) throws IOException, ClassNotFoundException;

    /**
     * Build body cody from method
     * @param method {@link java.lang.reflect.Method} for which is generated body code
     * @return {@link java.lang.String} body code
     */
    String buildCode(Method method) {
        Class<?> returnClass = method.getReturnType();
        if (returnClass.equals(void.class) || returnClass.equals(Void.class)) {
            return "";
        }
        String value = returnClass.isPrimitive() ? defaultValues.get(returnClass) : "null";
        return format("return %s;", value);
    }
}
