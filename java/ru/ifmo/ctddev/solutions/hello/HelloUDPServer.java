package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class HelloUDPServer implements HelloServer {

    public static final String RESPONSE_PREFIX = "Hello, ";
    private DatagramSocket serverSocket;

    @Override
    public void start(int port, int threads) {
        try {
            serverSocket = new DatagramSocket(port);
            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    while (!serverSocket.isClosed()) {
                        byte[] receiveBytes = new byte[4096];
                        DatagramPacket receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);

                        try {
                            serverSocket.receive(receivePacket);
                            String requestString = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            String responseString = RESPONSE_PREFIX + requestString;

                            byte[] responseBytes = responseString.getBytes("UTF-8");
                            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, receivePacket.getSocketAddress());

                            serverSocket.send(responsePacket);
                        } catch (SocketException e) {
                            // already closed
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }).start();
            }
        } catch (SocketException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void close() {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
