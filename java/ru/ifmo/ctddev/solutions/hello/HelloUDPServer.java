package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mikhail Yandimirov on 14.01.2018.
 */
public class HelloUDPServer implements HelloServer {

  private final Collection<DatagramSocket> sockets = new ArrayList<>();
  private final Collection<ExecutorService> executorServices = new ArrayList<>();
  private boolean closed;

  @Override
  public void start(final int port, final int threads) {
    synchronized (this) {
      checkClosed();
    }

    final ExecutorService threadPool = Executors.newFixedThreadPool(threads);

    try {
      final DatagramSocket socket = new DatagramSocket(port);
      final byte[] reqBuf = new byte[socket.getReceiveBufferSize()];

      synchronized (this) {
        checkClosed();
        sockets.add(socket);
        executorServices.add(threadPool);
        final DatagramPacket request = new DatagramPacket(reqBuf,
            reqBuf.length);
        threadPool.execute(new SeverTask(closed, threads, socket, request, threadPool));
      }
    } catch (SocketException e) {
      throw new IllegalStateException("Can't create socket", e);
    }
  }

  @Override
  public synchronized void close() {
      if (closed) {
        throw new IllegalStateException("Server has been already closed!");
      }
      closed = true;
      sockets.forEach(DatagramSocket::close);
      executorServices.forEach(ExecutorService::shutdownNow);
      sockets.clear();
      executorServices.clear();
  }

  private void checkClosed() {
    if (closed) {
      throw new IllegalStateException("Cannot start, because server is closed");
    }
  }
}
