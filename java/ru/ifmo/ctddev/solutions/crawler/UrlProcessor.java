package ru.ifmo.ctddev.solutions.crawler;

import static java.util.stream.Collectors.toList;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Result;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UrlProcessor {

  private final ExecutorService extractors;
  private final ExecutorService downloaders;

  private final DownloadHandler downloaderHandler;

  private final Set<String> processedUrls = new HashSet<>();
  private ReadWriteLock rwLock = new ReentrantReadWriteLock();

  public UrlProcessor(
      ExecutorService extractors,
      ExecutorService downloaders,
      DownloadHandler downloadHandler) {

    this.extractors = extractors;
    this.downloaders = downloaders;
    this.downloaderHandler = downloadHandler;
  }

  public Result processUrl(String url, int maxDepth) {
    if (maxDepth < 1) {
      throw new IllegalArgumentException("Max Depth should be greater than 1");
    }

    Node rootNode = new Node(url, 1, maxDepth);
    processNode(rootNode);
    return gatherDownloadedUrls(rootNode);
  }

  private void processNode(Node node) {
    boolean processedAlready;

    rwLock.readLock().lock();
    processedAlready = processedUrls.contains(node.getUrl());
    rwLock.readLock().unlock();

    if (!processedAlready) {
      rwLock.writeLock().lock();
      processedAlready = processedUrls.contains(node.getUrl());
      if (!processedAlready) {
        processedUrls.add(node.getUrl());
      }
      rwLock.writeLock().unlock();
    }

    if (!processedAlready) {
      if (isLeafNode(node)) {
        asyncDownload(node);
      } else {
        asyncDownloadAndExtract(node);
      }
    }

    if (processedAlready) {
      node.processed(true);
    }
  }

  private boolean isLeafNode(Node node) {
    return node.getNodeDepth() == node.getMaxDepth();
  }

  private void asyncDownload(Node node) {
    if (downloaders.isShutdown()) {
      node.interrupted();
      return;
    }

    downloaders.submit(() -> {
      try {
        Document document = downloaderHandler.download(node.getUrl());
        node.setDocument(document);

        node.processed();
      } catch (IOException e) {
        node.processed(e);
      } catch (InterruptedException e) {
        node.interrupted();
      }
    });
  }

  private void asyncDownloadAndExtract(Node node) {
    if (downloaders.isShutdown()) {
      node.interrupted();
      return;
    }

    downloaders.submit(() -> {
      try {
        Document document = downloaderHandler.download(node.getUrl());
        node.setDocument(document);

        asyncExtractAndSendToProcessing(node);
      } catch (IOException e) {
        node.processed(e);
      } catch (InterruptedException e) {
        node.interrupted();
      }
    });
  }

  private void asyncExtractAndSendToProcessing(Node node) {
    if (extractors.isShutdown()) {
      node.interrupted();
      return;
    }

    extractors.submit(() -> {
      try {
        List<Node> childNodes = node.getDocument().extractLinks().stream()
            .map(cl -> new Node(cl, node.getNodeDepth() + 1, node.getMaxDepth()))
            .collect(toList());

        node.setChilds(childNodes);

        childNodes.forEach(this::processNode);

        node.processed();
      } catch (IOException e) {
        node.processed(e);
      }
    });
  }

  // Blocking BFS
  private Result gatherDownloadedUrls(Node rootNode) {
    List<String> downloadedUrls = new ArrayList<>();
    HashMap<String, IOException> errors = new HashMap<>();

    Queue<Node> queue = new LinkedList<>();
    queue.add(rootNode);
    while (!queue.isEmpty()) {
      Node node = queue.poll();

      try {
        node.waitProcessing();
      } catch (InterruptedException e) {
        return new Result(downloadedUrls, errors);
      }

      if (node.getError() != null) {
        errors.put(node.getUrl(), node.getError());
      } else {
        if (node.isRepeated() || node.isInterrupted()) {
          continue;
        }

        downloadedUrls.add(node.getUrl());
        queue.addAll(node.getChilds());
      }
    }

    return new Result(new ArrayList<>(downloadedUrls), errors);
  }
}
