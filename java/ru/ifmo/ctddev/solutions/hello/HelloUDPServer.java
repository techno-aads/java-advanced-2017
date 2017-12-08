package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {

    /**
     * Each port is observed by several executor threads. This map relates the port number with certain executor
     * Those executors are stored in {@link ConcurrentHashMap} in order to 1. provide an atomicity of port binding
     * using {@link ConcurrentHashMap#putIfAbsent} and 2. It is using for closing all of executors in
     * {@link #close()} method.
     */
    private final Map<Integer, ExecutorService> executors = new ConcurrentHashMap<>();

    private final List<DatagramSocket> sockets = new ArrayList<>();

    @Override
    public void start(int port, int threadsNum) {
        ExecutorService executor = executors.putIfAbsent(port, Executors.newFixedThreadPool(threadsNum));
        if (executor != null) {
            throw new IllegalArgumentException("Port already in use");
        }

        System.out.println("Start listening port : " + port);

        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new IllegalArgumentException("Cannot create socket");
        }
        sockets.add(socket);

        for (int i = 0; i < threadsNum; ++i) {
            executors.get(port).submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        socket.setSoTimeout(100);
                        // receive the server's answer!
                        byte[] buffer = new byte[socket.getReceiveBufferSize()];
                        DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                        socket.receive(received);

                        String response = "Hello, " +
                                new String(received.getData(), 0, received.getLength(), StandardCharsets.UTF_8);

                        // construct message and send It to server
                        socket.send(new DatagramPacket(response.getBytes(StandardCharsets.UTF_8),
                                response.length(), new InetSocketAddress(received.getAddress(), received.getPort())));
                    }
                } catch (IOException ignored) {
                }
            });
        }
    }

    @Override
    public void close() {
        executors.values().forEach(ExecutorService::shutdownNow);
        sockets.forEach(DatagramSocket::close);
    }
}