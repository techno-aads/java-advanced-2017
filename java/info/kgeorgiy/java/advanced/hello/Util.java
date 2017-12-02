package info.kgeorgiy.java.advanced.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;

public class Util {
    public static final Charset CHARSET = Charset.forName("UTF-8");

    private Util() {
    }

    public static String getString(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), CHARSET);
    }

    public static void setString(DatagramPacket packet, String value) {
        packet.setData(value.getBytes(CHARSET));
        packet.setLength(packet.getData().length);
    }

    public static DatagramPacket createPacket(DatagramSocket socket) throws SocketException {
        return new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
    }

    public static String request(String request, DatagramSocket socket, SocketAddress address) throws IOException {
        send(socket, request, address);
        return receive(socket);
    }

    public static String receive(DatagramSocket socket) throws IOException {
        DatagramPacket packet = createPacket(socket);
        socket.receive(packet);
        return getString(packet);
    }

    public static void send(DatagramSocket socket, String request, SocketAddress address) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[0], 0);
        setString(packet, request);
        packet.setSocketAddress(address);
        synchronized (socket) {
            socket.send(packet);
        }
    }

    public static String response(String request) {
        return "Hello, " + request;
    }

    public static AtomicInteger[] server(final String prefix, int threads, double probability, DatagramSocket socket) {
        AtomicInteger[] expected = Stream.generate(AtomicInteger::new).limit((long) threads).toArray(AtomicInteger[]::new);
        (new Thread(() -> {
            Random random = new Random(4357204587045842850L);

            try {
                while (true) {
                    DatagramPacket packet = createPacket(socket);
                    socket.receive(packet);
                    String request = getString(packet);
                    String errorMessage = "Invalid request " + request;
                    Assert.assertTrue(errorMessage, request.startsWith(prefix));
                    String[] params = request.substring(prefix.length()).split("_");
                    Assert.assertTrue(errorMessage, params.length == 2);

                    try {
                        int threadId = Integer.parseInt(params[0]);
                        int requestId = Integer.parseInt(params[1]);
                        Assert.assertTrue(errorMessage, requestId == expected[threadId].get());
                        if (probability >= random.nextDouble()) {
                            expected[threadId].incrementAndGet();
                            setString(packet, response(request));
                            socket.send(packet);
                        } else if (random.nextBoolean()) {
                            setString(packet, corrupt(response(request), random));
                            socket.send(packet);
                        }
                    } catch (NumberFormatException e) {
                        throw new AssertionError(errorMessage);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        })).start();
        return expected;
    }

    private static String corrupt(String response, Random random) {
        switch (random.nextInt(3)) {
            case 0:
                return response + "0";
            case 1:
                return response + "Q";
            case 2:
                return "";
            default:
                throw new AssertionError("Impossible");
        }
    }
}

