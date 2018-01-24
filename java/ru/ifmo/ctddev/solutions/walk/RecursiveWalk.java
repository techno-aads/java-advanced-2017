package ru.ifmo.ctddev.solutions.walk;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static ru.ifmo.ctddev.solutions.walk.HashUtils.fileFnvHash;

/**
 * @author Nikita Sokeran
 */
public class RecursiveWalk {
    private final List<String> results = new ArrayList<>();

    private RecursiveWalk(final Path input) {
        final List<String> files = files(input);
        files.forEach(this::recursiveWalk);
    }

    private void addResult(final String fileName, final int hash) {
        results.add(String.format("%08x", hash) + " " + fileName);
    }

    private List<String> files(final Path inputFile) {
        try {
            return Files.readAllLines(inputFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't read file paths.", e);
        }
    }

    public void writeResults(final Path outputFile) {
        try {
            Files.write(outputFile, results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void collectFileInfo(final File file) throws IOException {
        int hash = fileFnvHash(file);
        addResult(file.getPath(), hash);
    }

    private void recursiveWalk(final String fileName) {
        try {
            Files.walkFileTree(Paths.get(fileName),
                    EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                                throws IOException {
                            collectFileInfo(file.toFile());
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException | RuntimeException e) {
            addResult(fileName, 0);
        }
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            Path inputFile;
            Path outputFile;
            try {
                inputFile = Paths.get(args[0]);
                outputFile = Paths.get(args[1]);
            } catch (RuntimeException e) {
                e.printStackTrace();
                return;
            }
            if (Files.exists(inputFile) && !Files.isDirectory(inputFile)) {
                RecursiveWalk walk = new RecursiveWalk(inputFile);
                walk.writeResults(outputFile);
            }
        }
    }
}
