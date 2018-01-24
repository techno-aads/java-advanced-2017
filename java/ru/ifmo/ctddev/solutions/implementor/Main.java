package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.Paths;

public class Main {
    /**
     * Main method to jar file. Compile specified class to specified path
     * @param args specified class (args[0]), specified path (args[1])
     * @throws ClassNotFoundException exception throws when we didn't found class by name
     * @throws ImplerException when implementation cannot be generated.
     */
    public static void main(String[] args) throws ClassNotFoundException, ImplerException {
        JarImplementor implementor = new JarImplementor();
        implementor.implementJar(Class.forName(args[0]), Paths.get(args[1]));
    }
}