package ru.ifmo.ctddev.solutions.walk;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class RecursiveWalk extends HashWalker {
    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;

    private final StringBuilder result = new StringBuilder();

    private final Path source;
    private final Path destenation;

    private final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            int hash = 0;
            try {
                hash = calculateHashForFile(file.toString());
            } catch (Exception ignore) {} // if thrown just write a zero hash
            writeResultLine(file.toString(), hash);

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            // log exception
            if (!Files.isDirectory(file)) {
                writeResultLine(file.toString(), 0);
            }
            return FileVisitResult.CONTINUE;
        }
    };

    public static void main(String[] args) {
        if (args.length < 2 || args[0] == null || args[1] == null) {
            return;
        }

        RecursiveWalk recursiveWalker;
        try {
            recursiveWalker = new RecursiveWalk(args[0], args[1]);
            recursiveWalker.process();
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Can not construct instance");
        }
    }

    public RecursiveWalk(String inputName, String destName) throws IOException, InvalidPathException {
        source = Paths.get(inputName);
        destenation = Paths.get(destName);
        if (Files.isDirectory(destenation) || Files.isDirectory(source)) {
            throw new IllegalArgumentException("Passed directory instread of regular file");
        }
    }

    private void process() throws IOException {
        try (Stream<String> stream = Files.lines(source, StandardCharsets.UTF_8)) {
            stream.forEach(pathTofile -> {
                        try {
                            Files.walkFileTree(Paths.get(pathTofile), visitor);
                        } catch (IOException | InvalidPathException ignore) {
                            System.out.println("Problem while walking file system");
                            writeResultLine(pathTofile, 0);
                        }
                    }
            );
        }

        Files.write(destenation, result.toString().getBytes());
    }

    private void writeResultLine(String pathTofile, int hash) {
        result.append(String.format("%08x %s\n", hash, pathTofile));
    }

    private static int calculateHashForFile(String path) throws IOException {
        try (InputStream fileReader = new FileInputStream(path)) {
            int rv = FNV_32_INIT;

            byte[] input = new byte[1024];
            int count;
            while ((count = fileReader.read(input)) != -1) {
                for (int i = 0; i < count; i++) {
                    rv = (rv * FNV_32_PRIME) ^ (input[i] & 0xff);
                }
            }
            return rv;
        }
    }
}
