package ru.ifmo.ctddev.solutions.walk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FVRealization extends SimpleFileVisitor<Path> {

    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int BUFFER_SIZE = 65536;
    List<String> resString;

    FVRealization() {
        resString = new ArrayList<>();
    }

    public static int hashCodeForFile(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        int hval = FNV_32_INIT;
        int bufferSize = BUFFER_SIZE;
        byte[] buffer = new byte[BUFFER_SIZE];
        while (input.available() > 0) {
            if (input.available() < BUFFER_SIZE)
                bufferSize = input.available();
            input.read(buffer, 0, bufferSize);
            for (int i = 0; i < bufferSize; i++) {
                hval = (hval * 0x01000193) ^ (buffer[i] & 0xff);
            }
        }
        return hval;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        int i = hashCodeForFile(file.toFile());
        String s = String.format("%08x %s", i, file.toString());
        resString.add(s);
        System.out.println(s);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
        resString.add("00000000 " + path.toString());
        System.out.println("00000000 " + path.toString());
        return FileVisitResult.CONTINUE;
    }

    public List<String> getResString() {
        return resString;
    }

}
