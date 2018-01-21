package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class RecursiveWalk {
    private static final String ERROR_HASH = "00000000";

    private String inputFileName;
    private String outputFileName;

    public RecursiveWalk(String inputFileName, String outputFileName) throws IOException, InvalidPathException {
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            return;
        }
        try {
            RecursiveWalk walker = new RecursiveWalk(args[0], args[1]);
            walker.run();
        } catch (Exception ignored) {
        }
    }

    private void run() throws IOException {

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
        }

        for (int i = 0; i < fileNames.size(); i++) {
            try {
                Path pathToDir = Paths.get(fileNames.get(i));

                Files.walkFileTree(pathToDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        WriteToFile(outputFileName, CalculateHash(file) + " " + file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        WriteToFile(outputFileName, ERROR_HASH + " " + file);
                        return FileVisitResult.CONTINUE;
                    }
                });

            } catch (Exception ex) {
                WriteToFile(outputFileName, ERROR_HASH + " " + fileNames.get(i));
            }
        }
    }

    private void WriteToFile(String pathToFile, String line) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathToFile, true))) {
            bufferedWriter.append(line);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (Exception ignored) {
        }
    }

    private String CalculateHash(Path file) {
        Integer hash = 0x811c9dc5;
        try (FileInputStream fileReader = new FileInputStream(file.toFile())) {
            byte[] input = new byte[1024];
            int counter;
            while ((counter = fileReader.read(input)) != -1) {
                for (int i = 0; i < counter; i++) {
                    hash = (hash * 0x01000193) ^ (input[i] & 0xff);
                }
            }
        } catch (IOException e) {
            return ERROR_HASH;
        }
        return String.format("%08x", hash);
    }
}
