package ru.ifmo.ctddev.solutions.walk;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class HashWalker {

    protected static final String INCORRECT_FILE_HASH = "00000000";

    protected static String calculateHash(Path file) {
        int currentHash = 0x811c9dc5;

        try {
            FileInputStream fin = new FileInputStream(file.toFile());
            FileChannel channel = fin.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(1024 * 8);
            try {
                while (channel.read(buf) != -1) {
                    buf.flip();
                    while (buf.hasRemaining()) {
                        currentHash = updateHash(currentHash, buf.get());
                    }
                    buf.clear();
                }
            } catch (IOException e) {
                System.out.println("Can't read one more byte from " + file.getFileName());
                try {
                    channel.close();
                } catch (IOException e1) {
                    e.printStackTrace();
                }
                try {
                    fin.close();
                } catch (IOException e2) {
                    System.out.println("Can't close " + file.toAbsolutePath().toString());
                }
                return INCORRECT_FILE_HASH;
            }
        } catch (SecurityException e) {
            return INCORRECT_FILE_HASH;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file.getFileName());
            return INCORRECT_FILE_HASH;
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
