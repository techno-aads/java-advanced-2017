package ru.ifmo.ctddev.solutions.udp;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {

    ThreadPoolExecutor executor;

    @Override
    public void start(int port, int threads) {

        executor = new ThreadPoolExecutor(threads, threads, Integer.MAX_VALUE,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());


        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < threads; ++i) {
            executor.execute(new UDPReceiver(socket));
        }

    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
