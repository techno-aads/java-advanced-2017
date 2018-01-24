package ru.ifmo.ctddev.solutions.walk;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;


public class RecursiveWalk {

    protected static final String INCORRECT_FILE_HASH = "00000000";
    private static StringBuilder outputContent;

    public static void main(String[] args) {
        try (Stream<String> stream = Files.lines(Paths.get(args[0]))) {
            outputContent = new StringBuilder();
            stream.forEach(RecursiveWalk::recursiveWalk);
            Files.write(Paths.get(args[1]), outputContent.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void recursiveWalk(String path) {
        try {
            Files.walk(Paths.get(path))
                    .filter(file -> {
                        try {
                            return Files.isRegularFile(file);
                        } catch (SecurityException e) {
                            return false;
                        }
                    })
                    .forEach(RecursiveWalk::calculateFNVHash);
        } catch (Exception e) {
            outputContent.append(HASH_STUB).append(" ").append(path).append("\n");
            System.out.println(e.getMessage());
        }
    }

    private static void calculateFNVHash(Path path) {
        int currentHash = 0x811c9dc5;
        try(FileChannel fileChannel = new FileInputStream(path.toString()).getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (fileChannel.read(buffer) > 0) {
                buffer.flip();
                for (int i = 0; i < buffer.limit(); ++i) {
                    currentHash = (currentHash * 0x01000193) ^ (buffer.get() & 0xff);
                }
                buffer.clear();
            }
            outputContent.append(String.format("%08X", currentHash).toLowerCase()).append(" ").append(path).append("\n");
        } catch (Exception e) {
            outputContent.append(INCORRECT_FILE_HASH).append(" ").append(path).append("\n");
            System.out.println(e.getMessage());
        }
    }
}
