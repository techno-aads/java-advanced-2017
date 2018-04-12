package ru.ifmo.ctddev.solutions.walk.Managers;

import java.io.File;

public class HashManager {

    protected static final String INCORRECT_FILE_HASH = "00000000";
    protected static final long FNV_OFFSET_BASIS = 2166136261L;
    protected static final int FNV_PRIME = 16777619;
    private long hash; //represents hash value

    public HashManager() {
        this.hash = FNV_OFFSET_BASIS;
    }

    public String getHash() {
        if (this.hash == -1)
            return INCORRECT_FILE_HASH;
        else
            return String.format("%08x", this.hash);
    }

    public void setHash(long newHash) {
        this.hash = newHash;
    }

    public void calculate(File file) throws Exception {
        FileManager fileManager = new FileManager(file);
        showCalculatingProcessStatus(0);

        for (int i = 0; i < fileManager.chunksNumber; i++) {
            if (fileManager.chunkSize > fileManager.available)
                fileManager.chunkSize = (int) fileManager.available;

            byte[] fileChunk = new byte[fileManager.chunkSize];
            fileManager.inputStream.read(fileChunk, 0, fileChunk.length);
            fileManager.available -= fileChunk.length;

            calculate(fileChunk);
            showCalculatingProcessStatus(fileManager);
        }
    }

    public void calculate(byte[] fileBytes) throws Exception {
        for (final byte b : fileBytes) {
            this.hash *= FNV_PRIME;
            this.hash ^= b;
        }
    }

    protected void showCalculatingProcessStatus(String error) {
        System.out.print("\rHash calculating process: " + error);
    }

    protected void showCalculatingProcessStatus(double percent) {
        String formatPattern = "%s";

        if ((long) percent == percent) {
            formatPattern = "%.0f";
        }

        System.out.print("\rHash calculating process: " + String.format(formatPattern, percent) + "%");
    }

    protected void showCalculatingProcessStatus(FileManager fileManager) {
        double percent = ((fileManager.file.length() - fileManager.available) / (double) fileManager.file.length()) * 100;
        showCalculatingProcessStatus(percent);
    }
}