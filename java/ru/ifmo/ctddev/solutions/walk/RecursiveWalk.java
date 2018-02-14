package ru.ifmo.ctddev.solutions.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk extends HashWalker {
    
    private String inputFile;
    private String outputFile;
    
    
    public RecursiveWalk(String inputFile, String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }
    
    public void walkFiles() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(inputFile), StandardCharsets.UTF_8);
             BufferedWriter bw = Files.newBufferedWriter(Paths.get(outputFile), StandardCharsets.UTF_8)) {
            
            FileVisitor<Path> fileVisitor = new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException, SecurityException {
                    writeHash(bw, calculateHash(file), file.toString());
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    writeHash(bw, getIncorrectFileHash(), file.toString());
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            };
            
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    Files.walkFileTree(Paths.get(line), fileVisitor);
                } catch (InvalidPathException | SecurityException e) {
                    writeHash(bw, getIncorrectFileHash(), line);
                }
            }
        } catch (IOException | InvalidPathException e) {
            System.out.println(e.toString());
        }
    }
    
    private void writeHash(BufferedWriter bw, String hash, String file) throws IOException {
        bw.write(String.format("%s %s", hash, file));
        bw.newLine();
    }
    
    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.out.println("Usage: RecursiveWalk <input file> <output file>");
            return;
        }
        
        RecursiveWalk walker = new RecursiveWalk(args[0], args[1]);
        walker.walkFiles();
    }
}
