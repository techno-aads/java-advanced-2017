package ru.ifmo.ctddev.solutions.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;

import java.nio.file.attribute.BasicFileAttributes;

public class FileVisitor extends SimpleFileVisitor<Path> {

    private final Writer writer;

    private static int FNV_32_PRIME = 0x01000193;
    private static int FNV0 = 0x811c9dc5;

    public static String getHash(Path path) {

        int hash = FNV0;
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            byte[] buffer = new byte[4096];
            int len;
            while((len = in.read(buffer)) >= 0) {
                for (int i = 0; i < len; i++) {
                    hash = (hash * FNV_32_PRIME) ^ (buffer[i] & 0xff);
                }
            }
        } catch (NoSuchFileException e) {
            System.err.println("File :" + path + " not found");
        } catch (AccessDeniedException e) {
            System.err.println("File :" + path + " security violation");
        } catch (IOException e) {
            System.err.println("IO error :" + path + " :" + e.getMessage());
        }
        return String.format("%08x", hash);
    }

    public FileVisitor(Writer writer) {
        this.writer = writer;
    }

    private void printHash(Path file, String hash) throws IOException {
        writer.write(hash + " " + file.toString() + "\n");
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        printHash(file, getHash(file));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        printHash(file, "00000000");
        return FileVisitResult.CONTINUE;
    }
}