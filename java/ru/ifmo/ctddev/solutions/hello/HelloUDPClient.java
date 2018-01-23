package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {

        InetAddress inetAddress = null;

        try {
            inetAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.println("UDPReceiver UnknownHostException" + e.getMessage());
            System.out.println(e);
            System.exit(1);
        }

        int corePoolSize = threads;
        int maximumPoolSize = threads;
        long keepAliveTime = Integer.MAX_VALUE;
        TimeUnit unit = TimeUnit.SECONDS;

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>());

        for (int i = 0; i < threads; i++) {
            threadPoolExecutor.execute(new UDPSender(i, inetAddress, port, prefix, requests));
        }



    }

}