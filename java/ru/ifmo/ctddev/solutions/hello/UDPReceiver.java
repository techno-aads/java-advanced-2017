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

        while (!m_Socket.isClosed()) {
            byte[] receiveBytes = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);

            try {
                m_Socket.receive(receivePacket);
                String requestString = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                String responseString = "Hello, " + requestString;

                byte[] responseBytes = responseString.getBytes("UTF-8");
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, receivePacket.getSocketAddress());

                m_Socket.send(responsePacket);
            } catch (SocketException e) {
                // already closed
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }


}