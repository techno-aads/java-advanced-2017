package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Nikita Sokeran
 */
public class HelloUDPServer implements HelloServer {
    private final Object lifecycleLock = new Object();

    private ExecutorService executor;
    private DatagramSocket socket;

    private ServerState state;

    @Override
    public void start(final int port, final int threads) {
        synchronized (lifecycleLock) {
            if (state == ServerState.RUNNING) {
                throw new IllegalStateException("Server is already running.");
            }
            if (state == ServerState.CLOSING) {
                throw new IllegalStateException("Server is closing.");
            }
            state = ServerState.RUNNING;
        }

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            close();
            throw new IllegalStateException("Couldn't create socket.", e);
        }

        final int bufferSize;
        try {
            bufferSize = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            close();
            throw new IllegalStateException("Couldn't get receive buffer size", e);
        }

        executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.submit(new ServerJob(socket, bufferSize));
        }
    }

    @Override
    public void close() {
        synchronized (lifecycleLock) {
            if (state != ServerState.RUNNING) {
                throw new IllegalStateException("Couldn't server which is not in running state.");
            }
            state = ServerState.CLOSING;
        }

        if (socket != null) {
            socket.close();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}