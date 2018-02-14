package ru.ifmo.ctddev.solutions.hello;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Utils {
    
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    
    private Utils() {
    
    }
    
    public static DatagramPacket packMessage(String message, InetAddress address, int port) {
        byte[] bytes = message.getBytes(DEFAULT_CHARSET);
        return new DatagramPacket(bytes, bytes.length, address, port);
    }
    
    public static String unpackMessage(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), DEFAULT_CHARSET);
    }
}
