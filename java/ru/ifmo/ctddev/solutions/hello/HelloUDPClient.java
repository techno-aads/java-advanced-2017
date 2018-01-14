package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        List<Thread> threadsList = new ArrayList<>(threads);

        for (int i = 0; i < threads; i++ ){
            int localI = i;
            Thread thread = new Thread( () ->{
                try(DatagramSocket clientSocket = new DatagramSocket()){
                    int requestNumber = 0;
                    while (requestNumber < requests){
                        try {

                            String sentence = prefix + localI + "_" + requestNumber;
                            DatagramPacket sendPacket = new DatagramPacket(sentence.getBytes(StandardCharsets.UTF_8), sentence.getBytes().length, new InetSocketAddress(host, port));
                            clientSocket.setSoTimeout(50);
                            clientSocket.send(sendPacket);

                            byte[] receiveData = new byte[sentence.length() + 20];
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            clientSocket.receive(receivePacket);
                            String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);

                            System.out.println(modifiedSentence);

                            if (modifiedSentence.equals("Hello, " + sentence)) {
                                requestNumber++;
                            }
                        }
                        catch (IOException ex){
                            System.out.println(ex.getMessage());
                        }
                    }
                }
                catch (SocketException ex){
                    System.out.println(ex.getMessage());
                }
            });
            thread.start();
            threadsList.add(thread);
        }

        for (int i = 0; i < threads; i++){
            try {
                threadsList.get(i).join();
            }
            catch (InterruptedException ex){
                Thread.currentThread().interrupt();
            }
        }
    }


    /*@Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        List<Thread> threadsList = new ArrayList<>(threads);

        for (int i = 0; i < threads; ++i) {
            int localI = i;

            Thread thread = new Thread(() -> {
                try (DatagramSocket clientSocket = new DatagramSocket()) {

                    int requestNumber = 0;
                    while (requestNumber < requests) {
                        try {

                            clientSocket.setSoTimeout(50);

                            String sentence = prefix + localI + "_" + requestNumber;
                            DatagramPacket sendPacket = new DatagramPacket(sentence.getBytes(StandardCharsets.UTF_8), sentence.getBytes().length, new InetSocketAddress(host, port));
                            clientSocket.send(sendPacket);


                            // receive the server's answer!
                            byte[] buffer = new byte[sentence.length() + 20]; // in order to leave place for "Hello,  "...
                            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                            clientSocket.receive(responsePacket);

                            String response = new String(responsePacket.getData(), 0, responsePacket.getLength(),
                                    StandardCharsets.UTF_8);

                            if (response.equals("Hello, " + sentence)) {
                                requestNumber++;
                            }
                        } catch (IOException ignore) {
                            // retry to perform request
                        }
                    }
                } catch (IOException ignore) {
                    // cannot open socket
                }
            });
            thread.start();
            threadsList.add(thread);
        }

        for (int i = 0; i < threads; i++){
            try {
                threadsList.get(i).join();
            }
            catch (InterruptedException ex){
                Thread.currentThread().interrupt();
            }
        }
    }
*/
    public static void main(String[] args) throws IOException {
        if (args.length < 5) {
            System.out.println("Usage: HelloClient <host> <port> <prefix> <count threads> <count requests>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String prefix = args[2];
        int countThreads = Integer.parseInt(args[3]);
        int countRequests = Integer.parseInt(args[4]);

        new HelloUDPClient().run (host,port,prefix,countRequests, countThreads);
    }
}
