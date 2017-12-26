package ru.ifmo.ctddev.solutions.implementor;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @author zakhar.razzhivin
 */
public class JarHelper {

    /**
     * Compile java class
     * @param pathToFile path to final class file
     * @throws IOException due to incorrect path
     */
    public static void compileFile(String pathToFile) throws IOException {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        String[] compilationUnits = {"-encoding", "UTF8", pathToFile};
        int exitCode = -1;
        try {
            exitCode = javaCompiler.run(null, null, null, compilationUnits);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (exitCode != 0) {
            System.out.println("Compile error with code: " + exitCode);
        }
    }

    /**
     * Generate jar file
     * @param pathToJar path to jar
     * @param pathToClass path to class
     * @throws IOException due to incorrect path
     */
    public static void createJar(String pathToJar, String pathToClass) throws IOException {
        FileOutputStream fileOutputStream = null;
        JarOutputStream jarOutputStream = null;
        try {
            File f = new File(pathToJar);
            if(f.getParentFile()!= null) {
                f.getParentFile().mkdirs();
            }
            if(f != null) {
                f.createNewFile();
            } else {
                System.out.println("Invalid path to jar file");
                System.exit(1);
            }

            fileOutputStream = new FileOutputStream(pathToJar);
            String newClassPath = pathToClass.substring(pathToClass.indexOf("tmp/") + 4);
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, newClassPath);
            jarOutputStream = new JarOutputStream(fileOutputStream, manifest);
            jarOutputStream.putNextEntry(new ZipEntry(newClassPath));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(pathToClass));

            int bytesRead;
            byte[] buffer = new byte[8 * 1024];
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                jarOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        } finally {
            jarOutputStream.closeEntry();
            jarOutputStream.close();
            fileOutputStream.close();
        }
    }
}
