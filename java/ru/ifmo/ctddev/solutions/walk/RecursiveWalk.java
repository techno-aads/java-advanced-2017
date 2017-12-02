package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

public class RecursiveWalk {

    private final static int FNV_32_PRIME = 0x01000193;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <input> <output>");
            return;
        }

        try {
            doWalk(args[0], args[1]);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void doWalk(String input, String output) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
        Queue<String> queue = new LinkedList<>();

        String path;
        while ((path = reader.readLine()) != null) {
            queue.add(path);
        }
        reader.close();

        PrintWriter writer = new PrintWriter(output);
        while (!queue.isEmpty()) {
            String filename = queue.poll();
            File p = new File(filename);

            if (p.isDirectory()) {
                File[] listFiles = p.listFiles();
                if (listFiles != null) {
                    queue.addAll(Arrays.stream(listFiles).map(File::getPath).collect(Collectors.toList()));
                } else {
                    printZeroHash(writer, p.getPath());
                }
            } else {
                printHash(writer, filename);
            }
        }

        writer.close();
    }

    private static void printHash(PrintWriter writer, String path) {
        int hash;
        try {
            hash = calc(path);
        } catch (IOException e) {
            hash = 0;
        }
        writer.print(String.format("%08x", hash));
        writer.print(" ");
        writer.println(path);
    }

    private static void printZeroHash(PrintWriter writer, String path) {
        writer.print(String.format("%08x", 0));
        writer.print(" ");
        writer.println(path);
    }

    private static int calc(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        int hval = 0x811c9dc5;

        int b;
        while ((b = fis.read()) != -1) {
            hval = (hval * FNV_32_PRIME) ^ (b & 0xff);
        }
        fis.close();

        return hval;
    }
}
