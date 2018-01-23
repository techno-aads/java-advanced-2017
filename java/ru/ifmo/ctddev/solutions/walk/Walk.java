package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

import static ru.ifmo.ctddev.solutions.walk.RecursiveWalk.getPath;

public class Walk {

    private String m_inputFileName;
    private String m_outputFileName;

    public void start(String inputFileName, String OutputFileName) {
        m_inputFileName = inputFileName;
        m_outputFileName = OutputFileName;

        observe();
    }

    public void observe(){
        try (BufferedWriter writer = Files.newBufferedWriter(getPath(m_outputFileName))) {
            try (BufferedReader reader = Files.newBufferedReader(getPath(m_inputFileName))) {
                String s;
                while ((s = reader.readLine()) != null) {
                    observe(writer, s);
                }
            } catch (NoSuchFileException e) {
                System.err.println("Input file :" + m_inputFileName + " does not exists");
            } catch (SecurityException e) {
                System.err.println("Input file :" + m_inputFileName + " security violation");
            } catch (IOException e) {
                System.err.println("Input error: " + e.getMessage());
            }
        } catch (SecurityException e) {
            System.err.println("Output file :" + m_outputFileName + " security violation");
        } catch (IOException e) {
            System.err.println("Output error: " + e.getMessage());
        }
    }

    public void observe(BufferedWriter writer, String file) throws IOException {
        Path path = getPath(file);
        if (path == null) {
            System.err.println("Invalid path to file: " + file);
            writer.write("00000000 " + file + "\n");
            return;
        }
        try {
            Files.walkFileTree(path, new FileVisitor(writer));
        } catch (IOException e) {
            System.err.println("Output error: " + e.getMessage());
        }
    }

}