package ru.ifmo.ctddev.solutions.walk;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Walk extends HashWalker {

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Invalid input");
            return;
        }
        //todo: use HashWalker methods for calculateHash
        String currentFile = "";
        List<String> outPutData = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(args[0]))){
            Path outPutFile = Paths.get(args[1]);
            try {
                stream.forEach(line -> outPutData.add(HashWalker.calculateHash(Paths.get(line)) + " " + line));
                Files.write(outPutFile, outPutData);
            }catch(InvalidPathException e){
                try (Stream<String> stream2 = Files.lines(Paths.get(args[0]))){
                        stream2.forEach(line -> outPutData.add(HashWalker.getIncorrectFileHash(line)));
                        Files.write(outPutFile, outPutData);
                    }catch(InvalidPathException e20){

                    }
            }
        }catch (NoSuchFileException e) {
            System.out.println("Input file not found");
        } catch (IOException e) {
            System.out.println("IOException was catched ");
        } catch (InvalidPathException e) {
            System.out.println("InvalidPathException was catched ");
//            try (Stream<String> stream = Files.lines(Paths.get(args[0]))){
//                stream.forEach(line ->outPutData.add(HashWalker.getIncorrectFileHash(line)));
//                Files.write(outPutFile,outPutData);
//            }catch (IOException e1) {
//                System.out.println("IOException was catched ");
//            }

        } catch (SecurityException e){
            System.out.println("SecurityException was catched ");
        }
    }
}
