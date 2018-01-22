package ru.ifmo.ctddev.solutions.walk;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class FileUtils {
    static void writeOutputFile(String outputFilePath, List<String> outputs) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(outputFilePath);
        outputs.forEach(writer::println);
        writer.close();
    }

    static List<File> readInputFile(String inputFilePath) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(inputFilePath), "UTF-8");
        BufferedReader reader = new BufferedReader(streamReader);
        String line;
        List<File> files = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            files.add(new File(line));
        }
        streamReader.close();
        reader.close();
        return files;
    }

    static int hash(File file) {
        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            int h = 0x811c9dc5;
            byte[] bytes = new byte[4096];
            int len;
            while ((len = in.read(bytes)) != -1) {
                for (int i = 0; i < len; i++) {
                    h = (h * 0x01000193) ^ (bytes[i] & 0xff);
                }
            }
            return h;
        } catch (Exception e) {
            return 0;
        }
    }

}
