package ru.ifmo.ctddev.solutions.implementor;

import com.squareup.javapoet.JavaFile;
import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static java.nio.file.Paths.get;

/**
 * Class for choice of generation algorithm
 *
 * @author d.kudryashov
 */
public class ImplementorProcessor implements Impler, JarImpler {

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        try {
            if (token.isPrimitive() || Enum.class.equals(token) || Modifier.isFinal(token.getModifiers())) {
                throw new ImplerException();
            }
            if (!token.isInterface() && checkConstructor(token)) {
                throw new ImplerException();
            }
            Implementor implementor = token.isInterface() ? new ImplementorInterface() : new ImplementorClass();
            JavaFile javaFile = implementor.process(token);
            write(root, token, javaFile);
        } catch (IOException | ClassNotFoundException e) {
            throw new ImplerException();
        }
    }

    /**
     * Method for find private {@link Constructor}
     *
     * @param token {@link Class} which is necessary implement
     * @return {@link Boolean} is contains private {@link Constructor}
     */
    private boolean checkConstructor(Class<?> token) {
        Set<Constructor> constructorSet = new HashSet<>(Arrays.asList(token.getConstructors()));
        constructorSet.addAll(Arrays.asList(token.getDeclaredConstructors()));
        return !constructorSet.stream()
                .anyMatch(constructor -> !Modifier.isPrivate(constructor.getModifiers()));
    }

    /**
     * Write class in file
     *
     * @param root          {@link Path} to out dir
     * @param token         {@link Class} that is necessary implement
     * @param implementFile {@link JavaFile} implement class
     * @throws IOException exception at write implement class
     */
    private void write(Path root, Class<?> token, JavaFile implementFile) throws IOException {
        String implementPackageName = token.getPackage().getName();
        String implementClassName = format("%sImpl", token.getSimpleName());
        Path packagePath = buildPackagePath(implementPackageName, root.toString());
        Path outPath = get(format("%s/%s.java", packagePath.toString(), implementClassName));
        try (BufferedWriter writer = Files.newBufferedWriter(outPath)) {
            writer.write(implementFile.toString());
        }
    }

    /**
     * Build package path from implement class
     *
     * @param packagePath {@link String} path to input class
     * @param root        {@link String} path to root
     * @return
     * @throws IOException
     */
    private Path buildPackagePath(String packagePath, String root) throws IOException {
        Path path = get(format("%s/%s", root, packagePath.replaceAll("\\.", "/")));
        Files.createDirectories(path);
        return path;
    }

    @Override
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {
        String root = format("%s/%s/", path.toString(), aClass.getPackage().getName().replaceAll("\\.", "/"));
        String javaPath = format("%s/%sImpl.java", root, aClass.getSimpleName());
        String jarPath = format("%s/%s.jar", root, aClass.getSimpleName());
        String classPath = format("%s/%sImpl.class", root, aClass.getSimpleName());
        implement(aClass, path);
        JarCreator.compileFile(javaPath);
        JarCreator.createJar(jarPath, classPath);
    }
}
