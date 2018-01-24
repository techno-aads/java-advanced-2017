package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class JarImplementor implements JarImpler {
    /**
     * Constant which defines path to temp directory
     */
    private static final String TEMP_DIRECTORY = "./temp/";

    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param aClass type token to create implementation for.
     * @param path target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {
        Implementor implementor = new Implementor();
        implementor.implement(aClass, Paths.get(TEMP_DIRECTORY));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, getPath(aClass, ".java"));
        String compiledClassPath = getPath(aClass, ".class");

        try {
            generateJarFile(path.toString(), compiledClassPath);
        } catch (IOException e) {
            throw new ImplerException();
        }
    }

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
        Implementor implementor = new Implementor();
        implementor.implement(aClass, path);
    }

    /**
     * Method generates jar file with compiled java file
     *
     * @param filePath path to output jar file
     * @param compiledClassPath path of compiled java file (.class)
     * @throws IOException
     */
    private static void generateJarFile(String filePath, String compiledClassPath) throws IOException {
        FileOutputStream fileOutput = new FileOutputStream(filePath);
        JarOutputStream jarOutput = new JarOutputStream(fileOutput);
        jarOutput.putNextEntry(new ZipEntry(getPackageFromPath(compiledClassPath) + "/"));
        jarOutput.putNextEntry(new ZipEntry(getPackageFromPath(compiledClassPath) + "/"
                + getClassNameFromPath(compiledClassPath)));
        jarOutput.write(Files.readAllBytes(Paths.get(compiledClassPath)));
        jarOutput.closeEntry();
        createManifestFile();
        jarOutput.putNextEntry(new ZipEntry("META-INF/"));
        jarOutput.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        jarOutput.write(Files.readAllBytes(Paths.get(TEMP_DIRECTORY + "manifest.txt")));
        jarOutput.closeEntry();
        jarOutput.close();
        fileOutput.close();
    }

    /**
     * Method generates manifest file to generating jar file
     */
    private static void createManifestFile() {
        try {
            FileWriter fileWriter = new FileWriter(TEMP_DIRECTORY + "manifest.txt");
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write("Manifest-Version: 1.0\n");
            writer.write("Created-By: 1.8.0_151 (Oracle Corporation)\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method defines path of .java or .class file which we want to implement
     *
     * @param aClass specified class
     * @param fileExtension extension for which file we want to define string path (.java or compiled .class)
     * @return string path of file
     */
    private static String getPath(Class<?> aClass, String fileExtension) {
        String[] strings = aClass.getPackage().getName().split("\\.");
        StringBuilder pathBuilder = new StringBuilder(TEMP_DIRECTORY);
        Arrays.stream(strings).forEach(x -> pathBuilder.append(x).append("/"));
        pathBuilder.append(aClass.getSimpleName()).append("Impl").append(fileExtension);
        return pathBuilder.toString();
    }

    /**
     * Method defines package name of specified path to java file
     *
     * @param path String path to file
     * @return String package name
     */
    private static String getPackageFromPath(String path) {
        String[] strings = path.split("/");
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < strings.length - 1; i++) {
            builder.append(strings[i]).append("/");
        }
        return builder.toString().substring(0, builder.length() - 1);
    }

    /**
     * Method defines simple class name of specified path to java file
     *
     * @param path String path to file
     * @return String simple class name
     */
    private static String getClassNameFromPath(String path) {
        String[] strings = path.split("/");
        return strings[strings.length - 1];
    }

}
