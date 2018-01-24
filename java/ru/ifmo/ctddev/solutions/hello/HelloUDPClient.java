package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Nikita Sokeran
 */
public class HelloUDPClient implements HelloClient {

    @Override
    public void run(final String host, final int port, final String prefix, final int requests, final int threads) {
        final ExecutorService executor = Executors.newFixedThreadPool(threads);
        final InetSocketAddress address = new InetSocketAddress(host, port);

        for (int i = 0; i < threads; i++) {
            executor.submit(new ClientJob(i, requests, prefix, address));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}