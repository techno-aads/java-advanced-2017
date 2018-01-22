package ru.ifmo.ctddev.solutions.implementor;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarCreator {

    public static void compileFile(String path) {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        javaCompiler.run(null, null, null, path);
    }

    public static void createJar(String pathToJar, String pathToClass) {
        Manifest manifest = buildManifest();
        try (JarOutputStream target = new JarOutputStream(new FileOutputStream(pathToJar), manifest);
             BufferedInputStream bufferedInputStream = getSourceInputStream(pathToClass, target)) {
            byte[] buffer = new byte[1024];
            while (true) {
                int count = bufferedInputStream.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Manifest buildManifest() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "ru.dkudryashov.lab3.Main");
        return manifest;
    }

    private static BufferedInputStream getSourceInputStream(String pathToClass, JarOutputStream target) throws IOException {
        File source = new File(pathToClass);
        JarEntry entry = new JarEntry(pathToClass.replace("\\", "/"));
        entry.setTime(source.lastModified());
        target.putNextEntry(entry);
        return new BufferedInputStream(new FileInputStream(source));
    }
}
