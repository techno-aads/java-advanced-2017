package ru.ifmo.ctddev.solutions.walk;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.file.Paths.get;

public class RecursiveWalk {

    public static void main(String[] args) {
        RecursiveWalk recursiveWalk = new RecursiveWalk();
        recursiveWalk.activate(args);
    }

    private void activate(String[] args) {
        if (args.length == 2) {
            try (BufferedWriter writer = Files.newBufferedWriter(get(args[1]))) {
                Files.readAllLines(get(args[0])).forEach(inPath -> process(inPath, writer));
            } catch (IOException | InvalidPathException ignore) {
            }
        }
    }

    private void process(String in, BufferedWriter writer) {
        try {
            Path inPath = get(in);
            if (inPath.toFile().isDirectory()) {
                try (Stream<Path> paths = Files.walk(inPath)) {
                    paths.filter(Files::isRegularFile).forEach(path -> writeFile(path, writer));
                }
            } else {
                writeFile(inPath, writer);
            }
        } catch (IOException | InvalidPathException e) {
            try {
                writer.write(join(" ", format("%08x", 0), in));
            } catch (IOException ignore) {
            }
        }
    }

    private void writeFile(Path inPath, BufferedWriter writer) {
        try {
            if (!inPath.toFile().isDirectory()) {
                writer.write(getHash(inPath) + "\r\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHash(Path inPath) throws IOException {
        int hash = 0x811c9dc5;
        int size = 1024;
        try (FileInputStream inputStream = new FileInputStream(inPath.toFile())) {
            byte[] buffer = new byte[size];
            int countRead = 0;
            while ((countRead = inputStream.read(buffer)) != -1) {
                hash = hash(buffer, hash, countRead);
            }
        } catch (IOException ignore) {
            hash = 0;
        }
        return join(" ", format("%08x", hash), inPath.toString());
    }

    private int hash(final byte[] bytes, int beforeHash, int countRead) {
        for (int i = 0; i < countRead; i++) {
            beforeHash = (beforeHash * 0x01000193) ^ (bytes[i] & 0xff);
        }
        return beforeHash;
    }
}
