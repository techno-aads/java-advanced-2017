package ru.ifmo.ctddev.solutions.walk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class RecursiveWalk extends HashWalker {
    private final StringBuilder result = new StringBuilder();

    private final Path source;
    private final Path destination;

    private final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String hash = INCORRECT_FILE_HASH;
            try {
                hash = calculateHash(file);
            } catch (Exception ignore) {
            } // if thrown just write a zero hash
            writeResultLine(file.toString(), hash);

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            // log exception
            if (!Files.isDirectory(file)) {
                writeResultLine(file.toString(), INCORRECT_FILE_HASH);
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

    public RecursiveWalk(String inputName, String destName) throws InvalidPathException {
        source = Paths.get(inputName);
        destination = Paths.get(destName);
        if (Files.isDirectory(destination) || Files.isDirectory(source)) {
            throw new IllegalArgumentException("Passed directory instead of regular file");
        }
    }

    private void process() throws IOException {
        try (Stream<String> stream = Files.lines(source, StandardCharsets.UTF_8)) {
            stream.forEach(pathTofile -> {
                        try {
                            Files.walkFileTree(Paths.get(pathTofile), visitor);
                        } catch (IOException | InvalidPathException ignore) {
                            System.out.println("Problem while walking file system");
                            writeResultLine(pathTofile, INCORRECT_FILE_HASH);
                        }
                    }
            );
        }

        Files.write(destination, result.toString().getBytes());
    }

    private void writeResultLine(String pathTofile, String hash) {
        result.append(String.format("%s %s\n", hash, pathTofile));
    }
}
