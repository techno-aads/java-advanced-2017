package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {

    private final List<DatagramSocket> sockets = new ArrayList<>();
    private final Map<Integer, ExecutorService> executors = new ConcurrentHashMap<>();

    @Override
    public void start(int port, int threadCount) {
        executors.putIfAbsent(port, Executors.newFixedThreadPool(threadCount));

        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new IllegalArgumentException("Can not create socket");
        }
        sockets.add(socket);

        executors.get(port).submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    socket.setSoTimeout(100);
                    DatagramPacket datagramPacketRequest = new DatagramPacket(
                            new byte[socket.getReceiveBufferSize()],
                            socket.getReceiveBufferSize());
                    socket.receive(datagramPacketRequest);
                    String response = "Hello, " + new String(
                            datagramPacketRequest.getData(),
                            0,
                            datagramPacketRequest.getLength(),
                            StandardCharsets.UTF_8);
                    InetAddress IPAddress = datagramPacketRequest.getAddress();
                    int receivePort = datagramPacketRequest.getPort();
                    DatagramPacket datagramPacketResponse = new DatagramPacket(response.getBytes(
                            StandardCharsets.UTF_8), response.getBytes().length, IPAddress, receivePort);
                    socket.send(datagramPacketResponse);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
    }

    @Override
    public void close() {
        executors.values().forEach(ExecutorService::shutdownNow);
        sockets.forEach(DatagramSocket::close);
    }
}
