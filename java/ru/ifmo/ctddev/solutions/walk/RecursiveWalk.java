package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RecursiveWalk {
    
    public static List<String> readFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        return lines;
    }

    public static void writeToFile(String filename, List<String> strings) throws IOException {
        FileWriter writer = new FileWriter(filename);
        for(String line : strings) {
            writer.write(line);
            writer.write(System.lineSeparator());
        }
        writer.flush();
        writer.close();
    }

    public static void main(String[] args){
        List<String> res;
        List<String> outputList = new ArrayList<>();
        try {
            res = readFromFile(args[0]);
            for(String i : res) {
                try {
                    Path p = Paths.get(i);
                    FVRealization fv = new FVRealization();
                    Files.walkFileTree(p, fv);
                    outputList.addAll(fv.getResString());
                }
                catch(InvalidPathException e) {
                    outputList.add("00000000 " + i);
                    System.out.println("00000000 " + i);
                }
            }
        }
        catch(FileNotFoundException  e) {
            System.err.println("No such file!");
        }
        catch(IOException e){
            System.err.println("error occured!");
        }

        try {
            if(args.length > 1)
                writeToFile(args[1], outputList);
        }
        catch(IOException e) {
            System.err.println("Some error on writing result file");
        }
    }
}
