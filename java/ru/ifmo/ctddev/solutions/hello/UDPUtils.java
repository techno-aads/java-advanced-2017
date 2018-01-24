package ru.ifmo.ctddev.solutions.hello;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * @author Nikita Sokeran
 */
public final class UDPUtils {
    private UDPUtils() {
    }

    public static String parseBody(final DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    public static DatagramPacket createPacket(final String body, final SocketAddress address) {
        final byte[] message = body.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(message, message.length, address);
    }
}
