package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class Walk {
    static Integer FNV_32_PRIME = 0x01000193;

    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }

        String inputFileName = args[0];
        String outputFileName = args[1];
        try {
            File outputFile = new File(outputFileName);
            if (outputFile.exists()) {
                outputFile.delete();
            }
        } catch (Exception ex) {
            return;
        }

        ArrayList<String> fileNames = new ArrayList<>();
        String fileName;
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(inputFileName), StandardCharsets.UTF_8))) {
            while ((fileName = buffer.readLine()) != null) {
                fileNames.add(fileName);
            }
        } catch (IOException ex) {
            return;
        } finally {
        }

        for (int i = 0; i < fileNames.size(); i++) {
            try {
                Path pathToDir = Paths.get(fileNames.get(i));

                Files.walkFileTree(pathToDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        WriteToFile(outputFileName, CalcFileHash(file) + " " + file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        WriteToFile(outputFileName, "00000000 " + file);
                        return FileVisitResult.CONTINUE;
                    }
                });

            } catch (Exception ex) {
                WriteToFile(outputFileName, "00000000 " + fileNames.get(i));
            }
        }
    }

    private static void WriteToFile(String pathToFile, String line) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathToFile, true))) {
            bufferedWriter.append(line);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (Exception ex) {

        }
    }

    private static String CalcFileHash(Path file) {
        File f = file.toFile();
        Integer hash = 0x811c9dc5;
        byte fbyte;
        try (FileInputStream inFile = new FileInputStream(f)) {
            try (FileChannel inChannel = inFile.getChannel()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (inChannel.read(buffer) > 0) {
                    buffer.flip();
                    for (int i = 0; i < buffer.limit(); i++) {
                        fbyte = buffer.get();
                        hash = (hash * 0x01000193) ^ (fbyte & 0xff);
                    }
                    buffer.clear();
                }
            } catch (IOException ex1) {
                return "00000000";
            }
        } catch (IOException ex) {
            return "00000000";
        }
        return String.format("%08x", hash);
    }
}
