package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {

        final InetSocketAddress inetAddress = new InetSocketAddress(host, port);
        List<Thread> threadsList = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(new UDPSender(i, inetAddress, prefix, requests));
            thread.start();
            threadsList.add(thread);
        }

        for (Thread thread : threadsList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }

}