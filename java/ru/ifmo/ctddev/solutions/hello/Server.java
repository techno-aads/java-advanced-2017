package ru.ifmo.ctddev.solutions.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server implements Runnable {
    
    private DatagramSocket socket;
    
    public Server(DatagramSocket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        byte[] buffer = new byte[0];
        try {
            buffer = new byte[socket.getReceiveBufferSize()];
        } catch (SocketException e) {
            e.printStackTrace();
        }
        
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (!socket.isClosed()) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            String request = Utils.unpackMessage(packet);
            
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            DatagramPacket responsePacket = Utils.packMessage("Hello, " + request, address, port);
    
            try {
                socket.send(responsePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
