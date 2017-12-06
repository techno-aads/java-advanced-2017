package ru.ifmo.ctddev.solutions.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

public class DownloadHandler {

  private final ConcurrentMap<String, Semaphore> perHostDownloadSemaphors = new ConcurrentHashMap<>();

  private final Downloader downloader;
  private final int perHostMax;

  public DownloadHandler(Downloader downloader, int perHostMax) {
    this.downloader = downloader;
    this.perHostMax = perHostMax;
  }

  public Document download(String url) throws IOException, InterruptedException {
    String host = URLUtils.getHost(url);

    perHostDownloadSemaphors.computeIfAbsent(host, h -> new Semaphore(perHostMax));

    Document document;
    try {
      perHostDownloadSemaphors.get(host).acquire();

      document = downloader.download(url);
    } finally {
      perHostDownloadSemaphors.get(host).release();
    }

    return document;
  }
}
