package ru.ifmo.ctddev.solutions.walk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sergey Egorov
 */

public class HashWalker {
    private static final int FNV_32_PRIME = 0x01000193;
    private static final int START_HASH_VALUE = 0x811c9dc5;
    private static final int BUFFER_SIZE = 100_000;

    public HashWalker() {
    }

    private static int fnvHash(final int startHash, final byte[] bytes, final int length) {
        int hash = startHash;
        for (int i = 0; i < length; i++) {
            hash = (hash * FNV_32_PRIME) ^ (0xff & bytes[i]);
        }
        return hash;
    }

    public static int fileFnvHash(final File file) throws IOException {
        int result = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        InputStream inputStream = new FileInputStream(file);
        int length = inputStream.read(buffer);
        do {
            if (result == 0) {
                result = fnvHash(START_HASH_VALUE, buffer, length);
            } else {
                result = fnvHash(result, buffer, length);
            }
            length = inputStream.read(buffer);
        }
        while (length != -1);
        return result;
    }
}
