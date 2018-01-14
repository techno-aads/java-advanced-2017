package ru.ifmo.ctddev.solutions.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class Recipient implements Runnable {

    private final DatagramSocket datagramSocket;
    private final DatagramPacket receivePacket;

    public Recipient(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
        byte[] bytes = new byte[1024];
        this.receivePacket = new DatagramPacket(bytes, bytes.length);
    }

    @Override
    public void run() {
        try {
            while(true) {
                receive();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receive() throws IOException {
        datagramSocket.receive(receivePacket);
        byte[] bytes = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());
        String receive = new String(bytes);
        System.out.println(receive);
        send(receive);
    }

    private void send(String receive) throws IOException {
        InetAddress inetAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
        String sendData = "Hello, " + receive;
        DatagramPacket sendPacket = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length, inetAddress, port);
        datagramSocket.send(sendPacket);
    }
}
