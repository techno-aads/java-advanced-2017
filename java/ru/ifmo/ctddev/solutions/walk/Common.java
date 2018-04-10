package ru.ifmo.ctddev.solutions.walk;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ru.ifmo.ctddev.solutions.walk.HashWalker.fileFnvHash;

/**
 * @author Sergey Egorov
 */

public class Common {

    private final List<String> results = new ArrayList<>();

    protected void addResult(final String fileName, final int hash) {
        results.add(String.format("%08x", hash) + " " + fileName);
    }

    protected List<String> files(final Path inputFile) {
        try {
            return Files.readAllLines(inputFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't read file paths.", e);
        }
    }

    protected void writeResults(final Path outputFile) {
        try {
            Files.write(outputFile, results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void collectFileInfo(final File file) throws IOException {
        int hash = fileFnvHash(file);
        addResult(file.getPath(), hash);
    }
}
