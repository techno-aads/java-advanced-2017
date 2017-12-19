package ru.ifmo.ctddev.solutions.walk;


import java.io.*;
        import java.nio.file.*;
        import java.util.ArrayList;

public class Walk {

    private String m_inputFileName;
    private String m_outputFileName;

    private ArrayList<String> m_ObserveFileNames;
    private static int FNV_32_PRIME = 0x01000193;
    private static int FNV0 = 0x811c9dc5;

    public void start(String inputFileName, String OutputFileName) {
        m_inputFileName = inputFileName;
        m_outputFileName = OutputFileName;
        m_ObserveFileNames = readFile(m_inputFileName);

        Observe();
    }

    private void Observe() {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(m_outputFileName), "UTF-8");
        ) {
            for (String file : m_ObserveFileNames) {
                Observe(file, writer);
            }

            writer.close();

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void Observe(String fileName, OutputStreamWriter writer) {

        try {
            File currentFile = new File(fileName);

            if (currentFile.isDirectory()) {
                try (DirectoryStream<Path> directory = Files.newDirectoryStream(Paths.get(currentFile.getPath()))) {
                    for (Path path : directory) {
                        Observe(path.toString(), writer);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    writer.write(String.format("%08x", 0) + " " + fileName + "\n");
                }
            } else if (currentFile.isFile()) {
                writer.write(String.format("%08x", getHash(fileName)) + " " + currentFile.toString() + "\n");
            } else {
                try {
                    writer.write(String.format("%08x", 0) + " " + fileName + "\n");
                } catch (IOException e1) {
                    System.out.println(e1.getMessage());
                }
            }

        } catch (NullPointerException e) {
            try {
                writer.write(String.format("%08x", 0) + " " + fileName + "\n");
            } catch (IOException e1) {
                System.out.println(e1.getMessage());
            }

        } catch (IOException e) {
            try {
                writer.write(String.format("%08x", 0) + " " + fileName + "\n");
            } catch (IOException e1) {
                System.out.println(e.getMessage());
            }
        }


    }

    private ArrayList<String> readFile(String FileName) {
        ArrayList<String> localFileNamesList = new ArrayList<String>();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(FileName), "UTF-8");
             BufferedReader buff = new BufferedReader(reader)) {
            String line;
            while ((line = buff.readLine()) != null) {
                localFileNamesList.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return localFileNamesList;
    }


    private int getHash(String fileName) {

        int hash = FNV0;
        try (InputStream is = new FileInputStream(fileName)) {
            hash = FNV0;

            int c;
            while ((c = is.read()) >= 0) {
                hash = (hash * FNV_32_PRIME) ^ (c & 0xff);
                ;
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return 0;
        }
        return hash;
    }


}
