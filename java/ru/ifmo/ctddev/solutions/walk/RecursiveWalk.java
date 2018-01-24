package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.util.List;

public class RecursiveWalk {
    public static void main( String[] args ) {
        try {
            if (args.length < 2) {
                return;
            }
            String inputFilePath = args[0];
            String outputFilePath = args[1];

            List<File> files = FileUtils.readInputFile(inputFilePath);

            Walk walk = new Walk();
            files.forEach(walk::walk);

            FileUtils.writeOutputFile(outputFilePath, walk.getResult());

        } catch (IOException | IllegalArgumentException ignored) {

        }

    }
}
