package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk extends HashWalker {

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Invalid input");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[1]), StandardCharsets.UTF_8)) {
            try {
                String line;
                SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException,
                            SecurityException {
                        writer.write(String.format("%s %s\n", calculateHash(file), file.toString()));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        writer.write(String.format("%s\n", getIncorrectFileHash(file.toString())));
                        return FileVisitResult.CONTINUE;
                    }
                };

                while ((line = reader.readLine()) != null) {
                    try {
                        Path path = Paths.get(line);
                        Files.walkFileTree(path, visitor);
                    } catch (InvalidPathException | SecurityException e) {
                        writer.write(String.format("%s\n", getIncorrectFileHash(line)));
                    }
                }
            } catch (IOException e) {
                System.out.println("Can't read line from Input file");
            }

        } catch (NoSuchFileException e) {
            System.out.println("Input file not found");
        } catch (IOException e) {
            System.out.println("IOException was catched ");
        } catch (InvalidPathException e) {
            System.out.println("InvalidPathException was catched ");
        }
    }
}
