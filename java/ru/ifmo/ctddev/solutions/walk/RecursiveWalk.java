package ru.ifmo.ctddev.solutions.walk;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk{

    public static void main(String[] args) {
        if (args == null || args.length != 2){
            System.out.println("Incorrect args size!");
            return;
        } else if ( args[0] == null ){
            System.out.println("Incorrect first arg (inputFile)");
            return;
        } else if (args[1] == null) {
            System.out.println("Incorrect second arg (outputFile)");
            return;
        }

        Path inputFile = getPath(args[0]);
        Path outputFile = getPath(args[1]);

        if (inputFile == null) {
            System.err.println("Incorrect input path file: '" + args[0] + "'");
            return;
        }

        if (outputFile == null) {
            System.err.println("Incorrect output path file: '" + args[1] + "'");
            return;
        }

        Walk walker = new Walk();
        walker.start(inputFile.toString(), outputFile.toString());

    }

    public static Path getPath(String s) {
        Path path;
        try {
            path = Paths.get(s);
        } catch (Exception e) {
            path = null;
        }
        return path;
    }
}