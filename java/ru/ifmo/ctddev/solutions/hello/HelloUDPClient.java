package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        final InetSocketAddress serverAddress = new InetSocketAddress(host, port);
        List<Thread> threadsList = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(new Sender(i, serverAddress, prefix, requests));
            thread.start();
            threadsList.add(thread);
        }

        for (Thread thread : threadsList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Sender implements Runnable {

        private final int threadId;
        private final InetSocketAddress serverAddress;
        private final String prefix;
        private final int requests;

        public Sender(int threadId, InetSocketAddress serverAddress, String prefix, int requests) {
            this.threadId = threadId;
            this.serverAddress = serverAddress;
            this.prefix = prefix;
            this.requests = requests;
        }

        @Override
        public void run() {
            for (int requestNumber = 0; requestNumber < requests; requestNumber++) {
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(1000);
                    String data = prefix + threadId + "_" + requestNumber;
                    byte[] requestBytes = data.getBytes("UTF-8");
                    System.out.println(data);

                    while (true) {
                        try {
                            DatagramPacket responsePacket = new DatagramPacket(new byte[4096], 4096);
                            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, serverAddress);

                            socket.send(requestPacket);
                            socket.receive(responsePacket);

                            String requestString = new String(requestPacket.getData(), requestPacket.getOffset(), requestPacket.getLength());
                            String responseString = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength(), "UTF-8");

                            if (responseString.contains(requestString)) {
                                break;
                            }
                        } catch (SocketTimeoutException e) {
                            // try again
                        }
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
