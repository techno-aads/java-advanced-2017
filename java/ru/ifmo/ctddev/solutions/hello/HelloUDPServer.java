package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    
    private DatagramSocket socket;
    private ExecutorService service;
    
    @Override
    public void start(int port, int threads) {
        service = Executors.newFixedThreadPool(threads);
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        
        for (int i = 0; i < threads; i++) {
            service.submit(new Server(socket));
        }
    }
    
    @Override
    public void close() {
        socket.close();
        
        try {
            service.shutdown();
            service.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        } finally {
            service.shutdownNow();
        }
    }
}