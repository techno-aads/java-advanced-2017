package ru.ifmo.ctddev.solutions.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;


public class UDPSender implements Runnable{

    //    final private int m_Port;
    final private int m_ThreadId;
    final private int m_Requests;
    final private String m_Prefix;
    final private InetSocketAddress m_serverAddress;
    final static int TimeOut = 100;

    public UDPSender(int threadId, InetSocketAddress address, String prefix, int requests) {
        m_Prefix = prefix;
        m_Requests = requests;
        m_ThreadId = threadId;
        m_serverAddress = address;
    }

    @Override
    public void run() {
        for (int cur = 0; cur < m_Requests; cur++) {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(TimeOut);
                String message = (m_Prefix + m_ThreadId + "_" + cur);
                byte[] inputBuffer = message.getBytes("UTF-8");
                System.out.println(message);

                while (true) {
                    try {
                        DatagramPacket responsePacket = new DatagramPacket(new byte[4096], 4096);
                        DatagramPacket requestPacket = new DatagramPacket(inputBuffer, inputBuffer.length, m_serverAddress);

                        socket.send(requestPacket);
                        socket.receive(responsePacket);

                        String requestString = new String(requestPacket.getData(), requestPacket.getOffset(), requestPacket.getLength());
                        String responseString = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength(), "UTF-8");

                        if (responseString.contains(requestString)) {
                            break;
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("UDPSender SocketTimeoutException" + e.getMessage());
                        System.out.println(e);
                    } catch (NumberFormatException e) {
                        System.out.println("UDPSender NumberFormatException" + e.getMessage());
                        System.out.println(e);
                    }
                }
            } catch (IOException e) {
                System.out.println("UDPSender IOException" + e.getMessage());
                System.out.println(e);
            }
        }

        System.err.println("Client #" + m_ThreadId + " exiting");
    }

}