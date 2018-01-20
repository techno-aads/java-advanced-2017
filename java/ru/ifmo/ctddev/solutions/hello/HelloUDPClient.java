package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HelloUDPClient implements HelloClient {

    private static final int HELLO_LENGTH = 8;

    @Override
    public void run(String host, int port, String prefRequest, int requestCountInThread, int threadCount) {
        List<Thread> threadList = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            int threadCounter = i;

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (DatagramSocket datagramSocket = new DatagramSocket()) {
                        int requestCounter = 0;
                        while (requestCounter < requestCountInThread) {
                            try {
                                datagramSocket.setSoTimeout(100);
                                String request = prefRequest + threadCounter + "_" + requestCounter;
                                DatagramPacket datagramPacketRequest = new DatagramPacket(request.getBytes(StandardCharsets.UTF_8),
                                        request.getBytes().length, new InetSocketAddress(host, port));
                                datagramSocket.send(datagramPacketRequest);

                                DatagramPacket datagramPacketResponse = new DatagramPacket(
                                        new byte[request.getBytes().length + HELLO_LENGTH],
                                        0,
                                        request.getBytes().length + HELLO_LENGTH);

                                datagramSocket.receive(datagramPacketResponse);

                                String response = new String(datagramPacketResponse.getData(), 0,
                                        datagramPacketResponse.getLength(), StandardCharsets.UTF_8);

                                if (("Hello, " + request).equals(response)) {
                                    requestCounter++;
                                }
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    } catch (SocketException e) {
                        System.out.println(e.getMessage());
                    }
                }
            });

            thread.start();
            threadList.add(thread);
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
