package ru.ifmo.ctddev.solutions.walk;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Files;

public class HashWalker {

    protected static final String INCORRECT_FILE_HASH = "00000000";
    protected static final int FNV_OFFSET_BASIS = 0x811c9dc5;
    protected static final int FNV_PRIME = 16777619;

    protected static String calculateHash(Path filePath) {
        try {
            if (!Files.exists(filePath))
                throw new FileNotFoundException();

            int hash = FNV_OFFSET_BASIS;
            byte[] bytes = Files.readAllBytes(filePath);

            for (final byte b : bytes) {
                hash *= FNV_PRIME;
                hash ^= b;
            }

            return String.format("%08x", hash);
        }
        catch (Exception ex){
            return INCORRECT_FILE_HASH;
        }
    }
}