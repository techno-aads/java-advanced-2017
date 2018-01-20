package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Walk {

    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;
    private static final int BUFFER_SIZE = 65536; // 64 kB for reading from file

    private static int hash(final byte[] bytes) {
        int h = 0x811c9dc5;
        for (final byte b : bytes) {
            h = (h * 0x01000193) ^ (b & 0xff);
        }
        return h;
    }

    public static int hashCodeForFile(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        int hval = FNV_32_INIT;
        int bufferSize = BUFFER_SIZE;
        byte[] buffer = new byte[BUFFER_SIZE];
        while (input.available() > 0) {
            if (input.available() < BUFFER_SIZE)
                bufferSize = input.available();
            input.read(buffer, 0, bufferSize);
            for (int i = 0; i < bufferSize; i++) {
                hval = (hval * 0x01000193) ^ (buffer[i] & 0xff);
            }
        }
        return hval;
    }

    public static int hashCodeForFileTest(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        int hval = FNV_32_INIT;
        int bufferSize = input.available();
        byte[] buffer = new byte[bufferSize];
        input.read(buffer);
            for (int i = 0; i < bufferSize; i++) {
                hval = (hval * 0x01000193) ^ (buffer[i] & 0xff);
            }
        return hval;
    }

    public static int hashCodeForFile(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        int hval = FNV_32_INIT;
        int bufferSize = BUFFER_SIZE;
        byte[] buffer = new byte[BUFFER_SIZE];
        while (input.available() > 0) {
            if (input.available() < BUFFER_SIZE)
                bufferSize = input.available();
            input.read(buffer, 0, bufferSize);
            for (int i = 0; i < bufferSize; i++) {
                hval = (hval * 0x01000193) ^ (buffer[i] & 0xff);
            }
        }
        return hval;
    }

    public static List<String> recursiveHashCode(File directory) throws IOException {
        File[] fileList = directory.listFiles();
        List<String> result = new ArrayList<String>();
        for(File f : fileList) {
            if(f.isFile()) {
                String s = Integer.toHexString(hashCodeForFile(f));
                for (int t = s.length(); t < 8; t++)
                    s = "0" + s;
                result.add(s + " " + f.getPath());
            }
            else {
                result.addAll(recursiveHashCode(f));
            }
        }
        return result;
    }

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
