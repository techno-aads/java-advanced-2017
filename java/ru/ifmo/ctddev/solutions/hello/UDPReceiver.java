package ru.ifmo.ctddev.solutions.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPReceiver implements Runnable{

    private DatagramSocket m_Socket;
    private DatagramPacket m_InDatagramPacket, m_OutDatagramPacket;

    public UDPReceiver(DatagramSocket socket) {

        try {
            m_Socket = socket;
            byte[] inputBuffer = new byte[socket.getReceiveBufferSize()];
            m_InDatagramPacket = new DatagramPacket(inputBuffer, inputBuffer.length);

        } catch (SocketException e) {
            System.out.println("UDPReceiver SocketException" + e.getMessage());
            System.out.println(e);
        }

    }

    @Override
    public void run() {

        while (Boolean.TRUE) {
            try {
                m_Socket.receive(m_InDatagramPacket);

                byte[] receivedBytes = new byte[m_InDatagramPacket.getLength()];
                System.arraycopy(m_InDatagramPacket.getData(), 0, receivedBytes, 0, m_InDatagramPacket.getLength());
                String inputRequest = new String(receivedBytes);

                if (inputRequest.equals("")) {
                    System.out.println("Error Received bytes for input Request!!!");
                    continue;
                }

                String answer = "Hello, " + inputRequest;
                byte[] sentBytes = answer.getBytes();
                m_OutDatagramPacket = new DatagramPacket(sentBytes, sentBytes.length, m_InDatagramPacket.getAddress(), m_InDatagramPacket.getPort()); // sending
                m_Socket.send(m_OutDatagramPacket);
            } catch (IOException e) {
                System.out.println("UDPReceiver IOException" + e.getMessage());
                System.out.println(e);
                continue;
            }
        }

    }
}