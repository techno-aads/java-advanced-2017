package ru.ifmo.ctddev.solutions.walk;

import java.io.*;

public class HashWalker {

    private static final String INCORRECT_FILE_HASH = "00000000";
    private static final Integer PRIME_NUMBER = 0x01000193;
    private static final Integer START_HASH_VALUE = 0x811c9dc5;

    protected String getHashString(String pathName) {
        File file = new File(pathName);
        if (!file.exists())
            return getIncorrectFileHash(pathName);

        return calculateHash(pathName) + " " + pathName;
    }

    private String calculateHash (String pathName) {
        int hash = START_HASH_VALUE;
        try (InputStream fis = new FileInputStream(new File(pathName))) {

            byte[] data = new byte[2048];
            int readAmount;
            while ((readAmount = (fis.read(data))) != -1) {

                for (int i = 0; i < readAmount; i++) {
                    hash = (hash * PRIME_NUMBER) ^ (data[i] & 0xff);
                }
            }
            return String.format("%08x", hash);
        }
        catch (IOException e) {
            return INCORRECT_FILE_HASH;
        }
    }

    protected static String getIncorrectFileHash(String fileName) {
        return INCORRECT_FILE_HASH + " " + fileName;
    }
}
