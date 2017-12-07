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
        Queue<String> queue = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input)))) {
            String path;
            while ((path = reader.readLine()) != null) {
                queue.add(path);
            }
        }

        try (PrintWriter writer = new PrintWriter(output)) {
            while (!queue.isEmpty()) {
                String filename = queue.poll();
                File p = new File(filename);

                if (p.isDirectory()) {
                    File[] listFiles = p.listFiles();
                    if (listFiles != null) {
                        queue.addAll(Arrays.stream(listFiles).map(File::getPath).collect(Collectors.toList()));
                    } else {
                        printHash(writer, p.getPath(), 0);
                    }
                } else {
                    printHash(writer, filename);
                }
            }
        }
    }

    private static void printHash(PrintWriter writer, String path) {
        int hash = 0;
        try {
            hash = calc(path);
        } catch (IOException ignored) {
        }
        printHash(writer, path, hash);
    }

    private static void printHash(PrintWriter writer, String path, int hash) {
        writer.print(String.format("%08x", hash));
        writer.print(" ");
        writer.println(path);
    }

    private static int calc(String path) throws IOException {

        int hval = 0x811c9dc5;
        try (InputStream fis = new BufferedInputStream(new FileInputStream(path))) {
            int b;
            while ((b = fis.read()) != -1) {
                hval = (hval * FNV_32_PRIME) ^ (b & 0xff);
            }
        }

        return hval;
    }
}
