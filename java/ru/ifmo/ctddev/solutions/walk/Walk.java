package ru.ifmo.ctddev.solutions.walk;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Walk {
    private Map<String, Integer> result = new TreeMap<>();

    void walk(File file) {
        if (!file.isDirectory()) {
            addToResult(file.toString(), FileUtils.hash(file));
        }
        try {
            Files.walk(file.toPath())
                    .filter(Files::isRegularFile)
                    .forEach(x -> addToResult(x.toFile().toString(), FileUtils.hash(x.toFile())));
        } catch (Exception ignored) {
            addToResult(file.toString(), 0);
        }
    }

    private void addToResult(String fileToString, int hashValue) {
        result.put(fileToString, hashValue);
    }

    List<String> getResult() {
        List<String> strings = new ArrayList<>();

        result.forEach((key, value) -> strings.add(String.format("%08x", value) + " " + key));

        return strings;
    }
}
