package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HelloUDPServer implements HelloServer {

    private Boolean started;
    private int port;
    private int threads;
    private ExecutorService executor;


    @Override
    public void start(int port, int threads) {
        started = true;

        this.port = port;
        this.threads = threads;

        executor = Executors.newWorkStealingPool(threads);

        Thread thread = new Thread(() -> run());
        thread.start();
    }

    protected void run() {
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            while (started) {
                try {
                    serverSocket.setSoTimeout(300);
                    byte[] receiveData = new byte[serverSocket.getReceiveBufferSize()];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    Future<Boolean> future = executor.submit(() -> sendData(serverSocket, receivePacket));


                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SocketException ex) {
            System.out.println(ex.getMessage());
        }
    }

    protected boolean sendData(DatagramSocket serverSocket, DatagramPacket receivePacket) {
        try {
            String sentence = "Hello, " + new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
            InetAddress IPAddress = receivePacket.getAddress();
            int receivePort = receivePacket.getPort();
            DatagramPacket sendPacket = new DatagramPacket(sentence.getBytes(StandardCharsets.UTF_8), sentence.getBytes().length, IPAddress, receivePort);
            serverSocket.send(sendPacket);

            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public void close() {
        started = false;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: HelloUDPServer <port> <number threads>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int numberThreads = Integer.parseInt(args[1]);

        new HelloUDPServer().start(port, numberThreads);
    }
}
