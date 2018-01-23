package ru.ifmo.ctddev.solutions.hello;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {

    ThreadPoolExecutor m_Executor;

    @Override
    public void start(int port, int threads) {

        int corePoolSize = threads;
        int maximumPoolSize = threads;
        long keepAliveTime = Integer.MAX_VALUE;
        TimeUnit unit = TimeUnit.SECONDS;
        DatagramSocket socket = null;

        m_Executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>());

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("UDPReceiver SocketException" + e.getMessage());
            System.out.println(e);
        }

        for (int i = 0; i < threads; ++i) {
            m_Executor.execute(new UDPReceiver(socket));
        }

    }

    @Override
    public void close() {
        m_Executor.shutdownNow();
    }
}