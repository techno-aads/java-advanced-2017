package ru.ifmo.ctddev.solutions.walk;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class RecursiveWalk {

    public static final int ERROR_HASH = 0;

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Invalid input");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[1]), StandardCharsets.UTF_8)) {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    walk(new File(line), writer);
                }
            } catch (IOException e) {
                System.out.println("Can't read line from Input file");
            }

        } catch (NoSuchFileException e) {
            System.out.println("Input file not found");
        } catch (IOException e) {
            System.out.println("IOException was catched ");
        } catch (InvalidPathException e) {
            System.out.println("InvalidPathException was catched ");
        }
    }

    private static void walk(File file, BufferedWriter outputWriter) {
        if (file.isDirectory()) {
            File[] items = file.listFiles();
            if (items == null) {
                try {
                    outputWriter.write(String.format("%08x %s", ERROR_HASH, file.getPath()) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                for (File item : items) {
                    walk(item, outputWriter);
                }
            }
        } else {
            try {
                outputWriter.write(String.format("%08x %s", fastFNV(file), file.getPath()) + "\n");
            } catch (IOException e) {
                System.out.println("Can't write line to Output file");
            }
        }
    }

    private static int fastFNV(File file) {
        int h = 0x811c9dc5;
        try {
            FileInputStream fin = new FileInputStream(file);
            FileChannel channel = fin.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(1024 * 8);
            try {
                int c;
                while (channel.read(buf) != -1) {
                    buf.flip();
                    while (buf.hasRemaining()) {
                        c = buf.get() & 0xff;
                        h = (h * 0x01000193) ^ c;
                    }
                    buf.clear();
                }
            } catch (IOException e) {
                System.out.println("Can't read one more byte from " + file.getPath());
                try {
                    channel.close();
                } catch (IOException e1) {
                    e.printStackTrace();
                }
                try {
                    fin.close();
                } catch (IOException e2) {
                    System.out.println("Can't close " + file.getPath());
                }
                return ERROR_HASH;
            }
        } catch (SecurityException e) {
            return ERROR_HASH;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file.getPath());
            return ERROR_HASH;
        }
        return h;
    }

}
