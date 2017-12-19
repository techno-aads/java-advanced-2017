package ru.ifmo.ctddev.solutions.walk;

public class RecursiveWalk {
    //todo

    public static void main(String[] args) {

        System.out.println("Start program!");

        ////////LAB 1
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Incorrect args");
        } else {

            String inputFile = args[0];
            String outputFile = args[1];

            Walk myFileObserver = new Walk();
            myFileObserver.start(inputFile, outputFile);
        }

    }
}
