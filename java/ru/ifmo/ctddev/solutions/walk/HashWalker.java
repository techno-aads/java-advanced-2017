package ru.ifmo.ctddev.solutions.walk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class HashWalker {

    protected static final String INCORRECT_FILE_HASH = "00000000";

    //fixme: change method signature if needed
    protected static String calculateHash(Path file) {

        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(file);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("1!!!!!!!!!!");
            return INCORRECT_FILE_HASH;
        }catch(OutOfMemoryError e){
            System.out.println("2!!!!!!!!!!");
            return INCORRECT_FILE_HASH;
        }catch (SecurityException e){
            System.out.println("3!!!!!!!!!!");
            return INCORRECT_FILE_HASH;
        }catch (InvalidPathException e ){
            System.out.println("4!!!!!!!!!!");
            return INCORRECT_FILE_HASH;
        }
        int currentHash = 0x811c9dc5;
        //todo: for all bytes — currentHash = updateHash(currentHash, nextByte);
        for(byte b : data){
            currentHash = updateHash(currentHash,b);
        }
        return String.format("%08x", currentHash);
    }

    protected static String getIncorrectFileHash(String fileName) {
        return INCORRECT_FILE_HASH + " " + fileName;
    }

    private static int updateHash(int currentHash, byte nextByte) {
        return (currentHash * 0x01000193) ^ (nextByte & 0xff);
    }
}
