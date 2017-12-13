package ru.ifmo.ctddev.solutions.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

public class UDPSender implements Runnable{

    final private int threadId;
    final private InetAddress address;
    final private int port;
    final private String prefix;
    final private int requests;

    public UDPSender(int threadId, InetAddress address, int port, String prefix, int requests) {
        this.threadId = threadId;
        this.address = address;
        this.port = port;
        this.prefix = prefix;
        this.requests = requests;
    }

    @Override
    public void run() {

        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(100);
            byte[] inputBuffer = new byte[socket.getReceiveBufferSize()];
            int current = 0;
            while (current != requests) {
                String message = (prefix + threadId + "_" + current);
                byte[] msg = message.getBytes("UTF-8");
                System.out.println(message);
                socket.send(new DatagramPacket(msg, msg.length, address, port));
                DatagramPacket reply = new DatagramPacket(inputBuffer, inputBuffer.length);
                try {
                    socket.receive(reply);
                    byte[] data = reply.getData();
                    String response = new String(data, 0, reply.getLength(), Charset.forName("UTF-8"));
                    if (!response.equals("Hello, " + message)) {
                        throw new NumberFormatException();
                    }
                    System.out.println(response);
                    current++;
                } catch (SocketTimeoutException | NumberFormatException e) {
                    e.printStackTrace();
                    current++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("Client #" + threadId + " exiting");
    }

}
