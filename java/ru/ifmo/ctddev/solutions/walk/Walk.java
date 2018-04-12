package ru.ifmo.ctddev.solutions.walk;

import ru.ifmo.ctddev.solutions.walk.Managers.HashManager;

import java.io.*;
import java.util.Scanner;

public class Walk {

    /**
     * Main method
     *
     * @param args [input file path] [output file path]
     */
    public static void main(String[] args) {
        args = getCorrectProgramInput(args);

        try (FileWriter outputFileWriter = new FileWriter(args[1], true)) {
            FileReader reader = new FileReader(args[0]);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String path;

            while ((path = bufferedReader.readLine()) != null) {
                try {
                    File file = new File(path);

                    if (file.isDirectory())
                        processDirectoryPath(file, outputFileWriter);
                    else
                        processFilePath(file, outputFileWriter);
                } catch (Exception e) {
                    System.out.println("\n\nAn error occurred while processing file: \"" + path + "\"");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Method is used to process each row of input file which represents directory path.
    Method performs execution of processFilePath() for each file in directory.
     */
    protected static void processDirectoryPath(File directory, FileWriter outputFileWriter) throws Exception {
        for (File file : directory.listFiles()) {
            if (file.isDirectory())
                processDirectoryPath(file, outputFileWriter);
            else
                processFilePath(file, outputFileWriter);
        }
    }

    /*
    Method is used to process each row of input file which represents file path (not directory).
    Method performs file validation, hash calculating, output printing.
    */
    protected static void processFilePath(File file, FileWriter outputFileWriter) throws Exception {
        long startTime = System.nanoTime();
        outputFileWriter.write("Path:" + "\"" + file.getPath() + "\"");
        HashManager hashManager = new HashManager();

        try {
            hashManager.calculate(file);
        } catch (Exception e) {
            hashManager.setHash(-1);
        } finally {
            outputFileWriter.write(" Hash:" + hashManager.getHash());
            outputFileWriter.write(" Elapsed time: " + getElapsedTimeInSeconds(startTime) + " seconds\n");
        }
    }

    protected static String[] getCorrectProgramInput(String[] args) {
        try {
            if (args.length != 2 || !areValidPaths(args)) {
                getCorrectProgramInput(getProgramInputFromUser());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return args;
    }

    protected static String[] getProgramInputFromUser() {
        var inputFromUser = new String[]{};

        System.out.println("\nObtained input arguments are incorrect. " +
                "Please enter correct values according this template: " +
                "[input file path] [output file path]\n");

        try (Scanner scanner = new Scanner(System.in);) {
            System.out.print("Enter:");
            inputFromUser = scanner.nextLine().split(" ");
        } catch (Exception e) {
            System.out.println("An error occurred while processing input program arguments. Error:");
            e.printStackTrace();
        }

        return inputFromUser;
    }

    protected static Boolean areValidPaths(String paths[]) {
        try {
            for (int i = 0; i < paths.length; i++) {
                var file = new File(paths[i]);

                if (!file.exists() || file.isDirectory())
                    return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected static double getElapsedTimeInSeconds(long startTime) {
        return (System.nanoTime() - startTime) / Math.pow(10, 9);
    }
}