package ru.ifmo.ctddev.solutions.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

public class UDPSender implements Runnable{

    final private int m_Port;
    final private int m_ThreadId;
    final private int m_Requests;
    final private String m_Prefix;
    final private InetAddress m_Address;
    final static int TimeOut = 100;

    public UDPSender(int threadId, InetAddress address, int port, String prefix, int requests) {
        m_Port = port;
        m_Prefix = prefix;
        m_Requests = requests;
        m_ThreadId = threadId;
        m_Address = address;
    }

    @Override
    public void run() {

        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TimeOut);
            byte[] inputBuffer = new byte[socket.getReceiveBufferSize()];
            int cur = 0;
            while (cur != m_Requests) {
                String message = (m_Prefix + m_ThreadId + "_" + cur);
                byte[] msg = message.getBytes("UTF-8");
                System.out.println(message);
                socket.send(new DatagramPacket(msg, msg.length, m_Address, m_Port));
                DatagramPacket reply = new DatagramPacket(inputBuffer, inputBuffer.length);
                try {
                    socket.receive(reply);
                    byte[] data = reply.getData();
                    String response = new String(data, 0, reply.getLength(), Charset.forName("UTF-8"));
                    String compareResponce = "Hello, " + message;
                    if (!response.equals(compareResponce)) {
                        throw new NumberFormatException();
                    }
                    System.out.println(response);
                    cur++;
                } catch (SocketTimeoutException e) {
                    System.out.println("UDPSender SocketTimeoutException" + e.getMessage());
                    System.out.println(e);
                    cur++;
                }  catch (NumberFormatException e) {
                    System.out.println("UDPSender NumberFormatException" + e.getMessage());
                    System.out.println(e);
                    cur++;
                }
            }
        } catch (IOException e) {
            System.out.println("UDPSender IOException" + e.getMessage());
            System.out.println(e);
        }

        System.err.println("Client #" + m_ThreadId + " exiting");
    }

}