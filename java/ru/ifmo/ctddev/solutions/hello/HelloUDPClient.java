package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String hostName, int port, String requestPrefix, int requestsPerThread, int threadsNum) {
        List<Thread> threads = new ArrayList<>(threadsNum);

        for (int i = 0; i < threadsNum; ++i) {
            int currentThreadNum = i;

            Thread thread = new Thread(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {

                    int requestNum = 0;
                    while (requestNum < requestsPerThread) {
                        String message = requestPrefix + currentThreadNum + "_" + requestNum;

                        try {
                            socket.setSoTimeout(50);
                            // send message to server
                            socket.send(new DatagramPacket(message.getBytes(StandardCharsets.UTF_8),
                                    message.getBytes().length, new InetSocketAddress(hostName, port)));


                            // receive the server's answer!
                            byte[] buffer = new byte[message.length() + 20]; // in order to leave place for "Hello,  "...
                            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                            socket.receive(responsePacket);

                            String response = new String(responsePacket.getData(), 0, responsePacket.getLength(),
                                    StandardCharsets.UTF_8);

                            if (response.equals("Hello, " + message)) {
                                ++requestNum;
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
            threads.add(thread);
        }

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
