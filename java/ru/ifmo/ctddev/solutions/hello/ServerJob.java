package ru.ifmo.ctddev.solutions.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Nikita Sokeran
 */
public class ServerJob implements Runnable {
    private static final Logger log = Logger.getLogger(ServerJob.class.getName());

    private final DatagramSocket socket;
    private final int bufferSize;

    public ServerJob(final DatagramSocket socket, final int bufferSize) {
        this.socket = socket;
        this.bufferSize = bufferSize;
    }

    @Override
    public void run() {
        final DatagramPacket request = new DatagramPacket(new byte[bufferSize], bufferSize);
        while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
            try {
                socket.receive(request);
            } catch (IOException e) {
                if (socket.isClosed()) {
                    return;
                }
                log.log(Level.WARNING, "Can't read request.", e);
                continue;
            }
            final String body = UDPUtils.parseBody(request);

            final DatagramPacket response = UDPUtils.createPacket("Hello, " + body, request.getSocketAddress());
            try {
                socket.send(response);
            } catch (IOException e) {
                log.log(Level.WARNING, "Can't send response.", e);
            }
        }
    }
}
