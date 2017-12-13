package ru.ifmo.ctddev.solutions.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPReceiver implements Runnable{


    private DatagramSocket socket;
    private DatagramPacket in;
    private DatagramPacket out;

    public UDPReceiver(DatagramSocket socket) {

        try {
            this.socket = socket;
            byte[] inputBuffer = new byte[socket.getReceiveBufferSize()];
            in = new DatagramPacket(inputBuffer, inputBuffer.length);

        } catch (SocketException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {

        while (true) {
            try {
                socket.receive(in);
                String inputLine, outputLine;
                byte[] bytesReceived = new byte[in.getLength()];
                System.arraycopy(in.getData(), 0, bytesReceived, 0, in.getLength());

                inputLine = new String(bytesReceived);

                if (inputLine.equals("")) {
                    System.out.println("Client processing FAILED: null or \"\" received");
                    continue;
                }

                outputLine = "Hello, " + inputLine;
                byte[] bytesSent = outputLine.getBytes();
                out = new DatagramPacket(bytesSent, bytesSent.length, in.getAddress(), in.getPort()); // sending
                socket.send(out);
            } catch (IOException e) {
                System.out.println("FAILED: " + e.getMessage());
                continue;
            }
        }

    }
}
