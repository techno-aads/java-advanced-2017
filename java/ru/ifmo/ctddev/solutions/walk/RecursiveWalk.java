package ru.ifmo.ctddev.solutions.walk;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Sergey Egorov
 */

public class RecursiveWalk extends Common {

    private RecursiveWalk(final Path input) {
        final List<String> files = files(input);
        files.forEach(this::recursiveWalk);
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
