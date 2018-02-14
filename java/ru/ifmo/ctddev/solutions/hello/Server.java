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
        try {
            byte[] buffer = new byte[socket.getReceiveBufferSize()];
            
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (!socket.isClosed()) {
                socket.receive(packet);
        
                String request = Utils.unpackMessage(packet);
        
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                DatagramPacket responsePacket = Utils.packMessage("Hello, " + request, address, port);
                socket.send(responsePacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
