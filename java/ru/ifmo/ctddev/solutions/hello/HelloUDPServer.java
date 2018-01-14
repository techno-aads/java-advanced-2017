package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {

    private List<Thread> threadList = new ArrayList<>();
    private DatagramSocket datagramSocket;

    public static void main(String[] args) {
        HelloUDPServer helloUDPServer = new HelloUDPServer();
        helloUDPServer.start(8080, 1);
    }

    @Override
    public void start(int port, int threads) {
        try {
            datagramSocket = new DatagramSocket(port);
            threadList = IntStream.range(0, threads).boxed()
                    .map(thread -> new Thread(new Recipient(datagramSocket)))
                    .collect(Collectors.toList());
            threadList.forEach(Thread::start);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        threadList.forEach(Thread::interrupt);
        datagramSocket.close();
    }
}
