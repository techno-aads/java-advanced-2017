package ru.ifmo.ctddev.solutions.walk;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class HashWalker {

    protected static final String INCORRECT_FILE_HASH = "00000000";

    protected static String calculateHash(Path file) throws IOException {
        int currentHash = 0x811c9dc5;

        try (InputStream fileReader = new FileInputStream(file.toString())) {
            byte[] input = new byte[1024];
            int count;
            while ((count = fileReader.read(input)) != -1) {
                for (int i = 0; i < count; i++) {
                    currentHash = updateHash(currentHash, input[i]);
                }
            }
        }
        return String.format("%08x", currentHash);
    }

    protected static String getIncorrectFileHash(String fileName) {
        return INCORRECT_FILE_HASH + " " + fileName;
    }

    private static int updateHash(int currentHash, byte nextByte) {
        return (currentHash * 0x01000193) ^ (nextByte & 0xff);
    }
}
