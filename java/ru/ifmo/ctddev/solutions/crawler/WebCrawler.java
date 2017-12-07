package ru.ifmo.ctddev.solutions.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloadExecutor;
    private final Semaphore maxDownloads;
    private final Semaphore maxExtractors;
    private final ConcurrentHashMap<String, Semaphore> hosts = new ConcurrentHashMap<>();
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        int threads;
        try {
            threads = Math.addExact(downloaders, extractors);
        }
        catch (ArithmeticException e) {
            threads = Integer.MAX_VALUE;
        }
        this.downloader       = downloader;
        this.downloadExecutor = Executors.newFixedThreadPool(threads);
        this.maxDownloads     = new Semaphore(downloaders);
        this.maxExtractors    = new Semaphore(extractors);
        this.perHost          = perHost;
    }

    protected class Cache {
        protected ConcurrentHashMap<String, String> urls = new ConcurrentHashMap<>();
        protected ConcurrentHashMap<String, IOException> errors = new ConcurrentHashMap<>();

        Cache() {}
    }

    protected class DownloadTask implements
            Callable<List<String>>,
            Function<String, List<String>> {
        private String url;
        private Cache cache;

        DownloadTask(String url, Cache cache) {
            this.url = url;
            this.cache = cache;
        }

        @Override
        public List<String> call() {
            // 'cause ConcurrentHashMap cannot store null
            return Objects.nonNull(cache.urls.putIfAbsent(url, url)) ? null :  this.apply(url);
        }

        @SuppressWarnings("unused")
        private Semaphore retreive(String url, Semaphore semaphore) {
            return semaphore == null
                ? new Semaphore(WebCrawler.this.perHost)
                : semaphore;
        }

        @Override
        public List<String> apply(String s) {
            Document document = null;
            List<String> result = null;
            String host;

            try {
                host = URLUtils.getHost(s);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            Semaphore semaphore = WebCrawler.this.hosts.compute(host, this::retreive);
            semaphore.acquireUninterruptibly();
            WebCrawler.this.maxDownloads.acquireUninterruptibly();
            try {
                document = WebCrawler.this.downloader.download(s);
            }
            catch (IOException e) {
                cache.errors.putIfAbsent(s, e);
            }
            finally {
                WebCrawler.this.maxDownloads.release();
                semaphore.release();
                if (document == null) {
                    return null;
                }
            }

            WebCrawler.this.maxExtractors.acquireUninterruptibly();
            try {
                result = document.extractLinks();
            }
            catch (IOException e) {
                cache.errors.putIfAbsent(s, e);
            }
            finally {
                WebCrawler.this.maxExtractors.release();
            }
            return result;
        }
    }


    private static <T> T extractFuture(Future<T> future) {
        try {
            return future.get();
        }
        catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Result download(String s, int i) {
        List<String> allUrls = new ArrayList<>(2 << i);
        List<String> currentUrls = Collections.singletonList(s);
        Cache cache = new Cache();

        for (int depth = 0; depth < i; depth++) {
            if (currentUrls.size() == 0)
                break;

            allUrls.addAll(currentUrls);
            List<Callable<List<String>>> downloads = currentUrls.stream()
                    .map(v -> new DownloadTask(v, cache) )
                    .collect(Collectors.toCollection(LinkedList::new));
            try {
                currentUrls = downloadExecutor.invokeAll(downloads).stream()
                        .filter(Objects::nonNull)
                        .map(WebCrawler::extractFuture)
                        .filter(Objects::nonNull)
                        .reduce(new ArrayList<>(i), (all, one) -> { all.addAll(one); return all; });
                currentUrls.removeAll(allUrls);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        allUrls.removeAll(cache.errors.keySet());

        return new Result(allUrls, cache.errors);
    }

    @Override
    public void close() {
        try {
            downloadExecutor.shutdownNow();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String... args) throws Exception {
        int depth = 1, downloadors = Integer.MAX_VALUE,
            extractors = Integer.MAX_VALUE, perHost = Integer.MAX_VALUE;

        if (args.length < 1) {
            throw new RuntimeException("Url is missing in command-line arguments.");
        }

        String url = args[0];
        for (int i = 1; i < args.length && i < 5; i++) {
            int n = Integer.valueOf(args[i]);
            switch (i) {
                case 1: depth       = n; break;
                case 2: downloadors = n; break;
                case 3: extractors  = n; break;
                case 4: perHost     = n; break;
            }
        }

        Result result;
        try (Crawler crawler = new WebCrawler(new CachingDownloader(), downloadors, extractors, perHost)) {
            result = crawler.download(url, depth);
        }
        if (Objects.nonNull(result)) {
            result.getDownloaded().forEach(System.out::println);
            result.getErrors().forEach((k, v) -> System.err.printf("Error on url %s: %s\n", k, String.valueOf(v)));
        }
    }
}
