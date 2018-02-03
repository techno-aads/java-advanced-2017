package ru.ifmo.ctddev.solutions.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class HashWalker {
    
    protected final static String INCORRECT_FILE_HASH = "00000000";
    
    protected static String getIncorrectFileHash() {
        return INCORRECT_FILE_HASH;
    }
    
    protected static String calculateHash(Path path) {
        int hash = 0x811C9DC5;
        
        try (InputStream is = Files.newInputStream(path)) {
            int h = 0x811C9DC5;
            byte[] buffer = new byte[8192];
            for (int read; (read = is.read(buffer, 0, buffer.length)) != -1; ) {
                for (int i = 0; i < read; i++) {
                    h = (h * 0x01000193) ^ (buffer[i] & 0xFF);
                    hash = updateHash(hash, buffer[i]);
                }
            }
        } catch (IOException e) {
            return INCORRECT_FILE_HASH;
        }
        
        return String.format("%08x", hash);
    }
    
    private static int updateHash(int currentHash, byte nextByte) {
        return (currentHash * 0x01000193) ^ (nextByte & 0xFF);
    }
}
