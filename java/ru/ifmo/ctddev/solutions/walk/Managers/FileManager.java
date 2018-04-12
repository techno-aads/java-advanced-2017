package ru.ifmo.ctddev.solutions.walk.Managers;

import java.io.File;
import java.io.FileInputStream;

public class FileManager {

    protected File file;
    protected FileInputStream inputStream; //file stream
    protected int chunkSize; //size of file chunk in bytes
    protected long chunksNumber; //number of file chunks
    protected long available = 0; //correct analog of FileInputStream.available() - number of available for reading bytes of file

    public FileManager(File file) throws Exception {
        System.out.print("\nFile path: \"" + file.getPath() + "\"");
        System.out.print("; file size(bytes): " + file.length());

        this.file = file;
        available = file.length();
        setChunkSize();
        setChunksNumber();
        inputStream = new FileInputStream(file.getPath());
    }

    protected void setChunkSize() {
        long chunkSize = file.length();
        long freeMemory = Runtime.getRuntime().freeMemory();

        if (chunkSize > freeMemory) {
            chunkSize = (long) Math.round(freeMemory / 4);
        }

        if (chunkSize > Integer.MAX_VALUE)
            chunkSize = Integer.MAX_VALUE;

        this.chunkSize = (int) chunkSize;
        System.out.print("; chunk size(bytes): " + this.chunkSize);
    }

    protected void setChunksNumber() {
        this.chunksNumber = (long) Math.ceil(file.length() / (double) this.chunkSize);
        System.out.println("; chunks number: " + this.chunksNumber);
    }
}