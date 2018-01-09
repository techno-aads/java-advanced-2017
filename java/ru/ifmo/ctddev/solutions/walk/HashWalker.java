package ru.ifmo.ctddev.solutions.walk;

import java.nio.file.Path;

public class HashWalker {

    protected static final String INCORRECT_FILE_HASH = "00000000";

    //fixme: change method signature if needed
    protected static String calculateHash(Path file) {
        int currentHash = 0x811c9dc5;
        //todo: for all bytes — currentHash = updateHash(currentHash, nextByte);
        return String.format("%08x", currentHash);
    }

    protected static String getIncorrectFileHash(String fileName) {
        return INCORRECT_FILE_HASH + " " + fileName;
    }

    private static int updateHash(int currentHash, byte nextByte) {
        return (currentHash * 0x01000193) ^ (nextByte & 0xff);
    }
}
