package ru.ifmo.ctddev.solutions.walk;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FNVHash {
    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;
    private static final int BUFFER_SIZE = 65536;
    public static int hashCodeForFile(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        int hval = FNV_32_INIT;
        int bufferSize = BUFFER_SIZE;
        byte[] buffer = new byte[BUFFER_SIZE];
        while(input.available() > 0) {
            if(input.available() < BUFFER_SIZE)
                bufferSize = input.available();
            input.read(buffer, 0, bufferSize);
            for(int i = 0; i < bufferSize; i++) {
                hval *= FNV_32_PRIME;
                hval ^= buffer[i];
            }
        }
        return hval;
    }
}
