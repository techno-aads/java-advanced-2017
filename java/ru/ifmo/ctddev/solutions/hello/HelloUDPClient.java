package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;

/**
 * A class, created UDP Client, which send packages to specified host and port
 */
public class HelloUDPClient implements HelloClient {
    private static final int SOCKET_TIMEOUT = 200;

    /**
     * Start client on args[0] host, args[1] port, sent args[2] prefix in args[4] threads args[3] times
     *
     * @param args array of command line args: args[0] host, args[1] port, sent args[2] prefix in args[4] threads args[3] times
     */
    public static void main(String[] args) {
        HelloUDPClient udpClient = new HelloUDPClient();
        udpClient.run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }

    private String readResponse(DatagramPacket receivePacket) {
        return new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(), Charset.forName("UTF-8"));
    }

    /**
     * Start UDP packages sender
     *
     * @param host     host send to
     * @param port     port send to
     * @param prefix   string to send
     * @param requests count times
     * @param threads  in count threads
     */
    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        Thread[] pool = new Thread[threads];
        final InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + host + "\n" + e.getMessage());
            return;
        }
        for (int i = 0; i < pool.length; i++) {
            final int threadNumber = i;
            pool[i] = new Thread(() -> {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    byte[] responseBuffer = new byte[socket.getReceiveBufferSize()];
                    socket.setSoTimeout(SOCKET_TIMEOUT);
                    for (int j = 0; j < requests; j++) {
                        String sendRequest = prefix.concat(String.valueOf(threadNumber))
                                .concat("_").concat(String.valueOf(j));
                        byte[] bytes = sendRequest.getBytes(Charset.forName("UTF-8"));
                        DatagramPacket requestPacket = new DatagramPacket(bytes, bytes.length, serverAddress, port);
                        socket.send(requestPacket);
                        DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                        String response = "";
                        while (true) {
                            try {
                                socket.receive(responsePacket);
                                response = readResponse(responsePacket);
                                if (response.equals("Hello, " + sendRequest)) {
                                    break;
                                }
                            } catch (SocketTimeoutException e) {
                                socket.send(requestPacket);
                                System.out.println("Request " + sendRequest + " time outed, resending");
                            }
                        }
                        System.out.println(">> " + sendRequest + "\n<< " + response + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        for (Thread t : pool) {
            t.start();
        }
        try {
            for (Thread t : pool) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}