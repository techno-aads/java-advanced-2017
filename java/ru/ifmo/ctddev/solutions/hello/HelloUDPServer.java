package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * A class, created Server, UDP Sockets listener on localhost and specified ports
 */
public class HelloUDPServer implements HelloServer {

    private ArrayList<Server> servers;

    /**
     * Default constructor
     */
    public HelloUDPServer() {
        servers = new ArrayList<>();
    }

    /**
     * Start server on args[0] ports, using args[1] threads to process UDP packages
     *
     * @param args array of command line args: args[0] - port, args[1] threads count
     */
    public static void main(String[] args) {
        HelloUDPServer udpServer = new HelloUDPServer();
        udpServer.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }

    /**
     * Start listener server
     *
     * @param port    port listen to
     * @param threads number of  threads run in
     */
    @Override
    public void start(int port, int threads) {
        Server server = null;
        try {
            server = new Server(port, threads);
        } catch (SocketException e) {
            System.out.println("Can't open socket on port: " + port + "\n" + e.getMessage());
            return;
        }
        servers.add(server);
        server.start();
    }

    /**
     * Interrupt all started in start() threads
     */
    @Override
    public void close() {
        for (Server server : servers) {
            server.stop();
        }
    }

    private class Server {
        final private DatagramSocket socket;
        private Thread[] pool;

        public Server(int port, int threads) throws SocketException {
            socket = new DatagramSocket(port);
            this.pool = new Thread[threads];
            for (int i = 0; i < threads; i++) {
                pool[i] = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[socket.getReceiveBufferSize()];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        while (true) {
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            try {
                                socket.receive(packet);
                                String message = getResponseMessage(packet);
                                InetAddress address = packet.getAddress();
                                int toPort = packet.getPort();
                                sendRequest(socket, "Hello, " + message, address, toPort);
                            } catch (SocketTimeoutException e) {
                                System.err.println(e.getMessage());
                            }
                        }
                    } catch (IOException ignored) {
                    }
                });
            }
        }

        private void sendRequest(DatagramSocket socket, String request, InetAddress address, int port) throws IOException {
            byte[] bytes = request.getBytes(Charset.forName("UTF-8"));
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
            socket.send(packet);
        }

        private String getResponseMessage(DatagramPacket receivePacket) {
            return new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(), Charset.forName("UTF-8"));
        }

        public void start() {
            for (Thread t : pool) {
                t.start();
            }
        }

        public void stop() {
            for (Thread t : pool) {
                t.interrupt();
            }
            socket.close();
        }
    }
}