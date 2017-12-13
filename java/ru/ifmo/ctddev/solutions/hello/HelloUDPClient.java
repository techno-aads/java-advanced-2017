package ru.ifmo.ctddev.solutions.udp;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {


        InetAddress address = null;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(threads, threads, Integer.MAX_VALUE,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        for (int i = 0; i < threads; i++) {
            tpe.execute(new UDPSender(i, address, port, prefix, requests));
        }



    }

}
