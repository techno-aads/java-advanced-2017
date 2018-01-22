package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author Nikita Sokeran
 */
public class JarImplementor extends Implementor implements JarImpler {

    /**
     * {@link JavaCompiler} used to compile Java code.
     */
    private static final JavaCompiler JAVA_COMPILER = ToolProvider
            .getSystemJavaCompiler();

    /**
     * @param root         {@link Path} - root path of directory
     * @param clazz        {@link Class} that needs to implement
     * @param constructors {@link Collection<Constructor>} - collection of
     *                     constructors for implementation
     * @param methods      {@link Collection<Method>} - collection of methods for
     *                     implementation
     */
    @Override
    protected void writeImplementation(final Path root, final Class clazz,
                                       final Collection<Constructor> constructors,
                                       final Collection<Method> methods) {
        super.writeImplementation(root, clazz, constructors, methods);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        String packageName = clazz.getPackage().getName().replaceAll("\\.", "/");
        String javaFileName = root.toString() + File.separator + packageName + File.separator + clazz.getSimpleName()
                + Implementor.CLASS_EXT + ".java";
        String classFileName = root.toString() + File.separator + packageName + File.separator + clazz.getSimpleName()
                + Implementor.CLASS_EXT + ".class";
        String jarEntryName = packageName + File.separator + clazz.getSimpleName() + Implementor.CLASS_EXT + ".class";
        String jarFileName = root.toString() + ".jar";

        try (JarOutputStream jar = new JarOutputStream(
                new FileOutputStream(jarFileName), manifest);
             BufferedOutputStream outputStream = new BufferedOutputStream(jar)) {
            JAVA_COMPILER.run(null, null, null, javaFileName);
            byte[] bytes = Files.readAllBytes(Paths.get(classFileName));
            jar.putNextEntry(new JarEntry(jarEntryName));
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, ImplerException {
        JarImplementor implementor = new JarImplementor();
        implementor.implementJar(ClassLoader.getSystemClassLoader().loadClass(args[0]), Paths.get("."));
    }

    /**
     * @param token   type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile)
            throws ImplerException {
        implement(token, jarFile);
    }
}
