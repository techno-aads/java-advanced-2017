package ru.ifmo.ctddev.solutions.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mikhail Yandimirov on 14.01.2018.
 */

public class HelloUDPClient implements HelloClient {

  @Override
  public void run(final String host, final int port, final String prefix, final int requests, final int threads) {
    final ExecutorService threadPool = Executors.newFixedThreadPool(threads);
    try {
      final InetAddress address;
      try {
        address = InetAddress.getByName(host);
      } catch (UnknownHostException e) {
        throw new IllegalStateException("Unknown hostname", e);
      }

      for (int i = 0; i < threads; i++) {
        threadPool.execute(new ClientTask(i, requests, prefix, port, address));
      }

      threadPool.shutdown();
      threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    } catch (InterruptedException ignored) {
    } finally {
      threadPool.shutdownNow();
    }
  }
}