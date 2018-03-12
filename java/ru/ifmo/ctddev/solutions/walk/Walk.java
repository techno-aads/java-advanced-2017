package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.nio.file.Paths;

public class Walk extends HashWalker {

    public static void main(String[] args) {

        try {
            String inputFilePath = args.length > 0 ? args[0] : "";
            String outputFilePath = args.length > 1 ? args[1] : "";

            Writer outputFileWriter = new FileWriter(outputFilePath, true);

            FileReader reader = new FileReader(inputFilePath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String filePath;

            while ((filePath = bufferedReader.readLine()) != null) {
                String hash = calculateHash(Paths.get(filePath));
                outputFileWriter.write( hash + " " + filePath + "\n");
            }

            reader.close();
            outputFileWriter.close();
        }
        catch (Exception ex) {
            System.out.println("ERROR:");
            System.out.println("\tMethod name: " + ex.getStackTrace()[0].getMethodName());
            System.out.println("\tException type: " + ex.getClass().getName());
            System.out.println("\tMessage: " + ex.getMessage());
        }
    }
}
