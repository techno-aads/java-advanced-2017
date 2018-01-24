package ru.ifmo.ctddev.solutions.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Nikita Sokeran
 */
public class ClientJob implements Runnable {
    private static final int RECEIVE_TIMEOUT = 500;

    private static final Logger log = Logger.getLogger(ClientJob.class.getName());

    private final int threadId;
    private final int requests;
    private final String prefix;
    private final InetSocketAddress address;

    public ClientJob(final int threadId, final int requests, final String prefix, final InetSocketAddress address) {
        this.threadId = threadId;
        this.requests = requests;
        this.prefix = prefix;
        this.address = address;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(RECEIVE_TIMEOUT); // Prevent socket from infinite blocking on .receive()
            final int bufferSize = socket.getReceiveBufferSize();
            final DatagramPacket response = new DatagramPacket(new byte[bufferSize], 0, bufferSize);

            int currentRequestId = 0;
            while (currentRequestId < requests) {
                final String body = MessageFormat.format("{0}{1}_{2}", prefix, threadId, currentRequestId);
                final DatagramPacket request = UDPUtils.createPacket(body, address);

                try {
                    socket.send(request);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Couldn't send request to server", e);
                    continue;
                }

                try {
                    socket.receive(response);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Couldn't receive response.", e);
                    continue;
                }

                final String message = UDPUtils.parseBody(response);
                if (message.equals("Hello, " + body)) { // Process next request only if response is correct, otherwise try again
                    currentRequestId++;
                }
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Error occurred during initial socket configuration and connection.", e);
        }
    }
}