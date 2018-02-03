package ru.ifmo.ctddev.solutions.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;


public class Client implements Runnable {
    
    private InetAddress address;
    private int port;
    private int requests;
    private String prefix;
    private int thread;
    
    public Client(InetAddress address, int port, int requests, String prefix, int thread) {
        this.address = address;
        this.port = port;
        this.requests = requests;
        this.prefix = prefix;
        this.thread = thread;
    }
    
    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(10);
            
            byte[] buffer = new byte[socket.getReceiveBufferSize()];
            int i = 0;
            while (i < requests) {
                String request = String.format("%s%d_%d", prefix, thread, i);
                DatagramPacket requestPacket = Utils.packMessage(request, address, port);
                socket.send(requestPacket);
    
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                String response = "";
                try {
                    socket.receive(responsePacket);
                    response = Utils.unpackMessage(responsePacket);
                } catch (SocketTimeoutException e) {
                    System.out.println("Request timed out");
                }
    
                if (response.equals("Hello, " + request)) {
                    i++;
                    System.out.printf("%s\n%s\n", request, response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
