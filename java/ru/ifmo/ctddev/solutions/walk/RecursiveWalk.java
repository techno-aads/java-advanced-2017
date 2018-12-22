package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecursiveWalk extends HashWalker {
    private String inputFileName;
    private String outputFileName;

    private RecursiveWalk (String inputFileName, String outputFileName) {
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.out.println("Incorrect amount of input parameters: two names are expected");
            return;
        }

        String inputFileName = args[0];
        String outputFileName = args[1];

        if (inputFileName == null || inputFileName.equals("") || outputFileName == null || outputFileName.equals("")) {
            System.out.println("The given path names are incorrect. Please validate them and retry");
            return;
        }

        RecursiveWalk walk = new RecursiveWalk(inputFileName, outputFileName);
        walk.performWalk();
    }

    private void performWalk() {
        List<String> fileNames = getFileNames(inputFileName);
        File outputFile = new File(outputFileName);

        for (String fileName: fileNames) {
            try {
                Path filePath = Paths.get(fileName);
                Files.walkFileTree(filePath, new RecursiveFileVisitor(outputFile));
            } catch (Exception e) {
                System.out.println("An error has occurred while performing a file walk: " + e.getMessage());
                writeToFile(getIncorrectFileHash(fileName), outputFile);
            }
        }
    }

    private class RecursiveFileVisitor extends SimpleFileVisitor<Path> {
        private File outputFile;

        RecursiveFileVisitor(File outputFile) {
            this.outputFile = outputFile;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            writeToFile(getHashString(file.toString()), outputFile);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            writeToFile(getIncorrectFileHash(file.toString()), outputFile);

            return FileVisitResult.CONTINUE;
        }
    }

    private void writeToFile (String nextLine, File outputFile) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile, true))) {
            bufferedWriter.append(nextLine);
            bufferedWriter.newLine();
        }
        catch (IOException e) {
            System.out.println("An error occurred while writing to output file: " + e.getMessage());
        }
    }

    private static List<String> getFileNames (String pathToFile) {
        List<String> fileNames = new ArrayList<>();

        String fileName;
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(pathToFile), StandardCharsets.UTF_8))) {
            while ((fileName = buffer.readLine()) != null) {
                fileNames.add(fileName);
            }
        } catch (IOException ex) {
            return Collections.emptyList();
        }

        return fileNames;
    }

}
