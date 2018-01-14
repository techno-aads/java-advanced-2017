package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {

    private final Integer TIMEOUT = 1000;

    public static void main(String[] args) {
        HelloUDPClient helloUDPClient = new HelloUDPClient();
        helloUDPClient.run("localhost", 8080, "prefix", 10, 10);
    }

    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        List<CompletableFuture<?>> completableFutureList = IntStream.range(0, threads).boxed()
                .map(thread -> CompletableFuture.runAsync(() -> process(host, port, thread, requests, prefix)))
                .collect(Collectors.<CompletableFuture<?>>toList());
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()])).join();
        completableFutureList.forEach(CompletableFuture::join);
    }

    private void process(String host, int port, Integer thread, int requests, String prefix) {
        for (Integer request = 0; request < requests; request++) {
            try (DatagramSocket clientSocket = new DatagramSocket()) {
                clientSocket.setSoTimeout(TIMEOUT);
                InetAddress inetAddress = InetAddress.getByName(host);
                String data = String.join("", prefix, thread.toString(), "_", request.toString());
                System.out.println(data);
                while (true) {
                    String response = send(inetAddress, port, clientSocket, data.getBytes());
                    System.out.println(response);
                    if (response.contains(data)) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String send(InetAddress inetAddress, int port, DatagramSocket clientSocket, byte[] sendData) throws IOException {
        try {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, port);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
            clientSocket.receive(receivePacket);
            byte[] bytes = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());
            return new String(bytes);
        } catch (SocketTimeoutException e) {
            System.out.println("WARN: SocketTimeoutException -  " + e.getMessage());
            return "";
        }
    }
}
