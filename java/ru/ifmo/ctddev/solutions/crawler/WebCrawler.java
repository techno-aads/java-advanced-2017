package ru.ifmo.ctddev.solutions.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloading, extracting;
    private final ConcurrentHashMap<String, Semaphore> hosts = new ConcurrentHashMap<>();
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloading = Executors.newFixedThreadPool(downloaders);
        this.extracting = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    @Override
    public Result download(String url, int depth) {
        return (new Aggregator(depth)).apply(url);
    }

    @Override
    public void close() {
        try {
            downloading.shutdownNow();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            extracting.shutdownNow();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String... args) throws Exception {
        int depth = 1, downloadors = Integer.MAX_VALUE,
                extractors = Integer.MAX_VALUE, perHost = Integer.MAX_VALUE;
        if (args.length < 1) throw new RuntimeException("Arguments: <url> [depth [downloaders [extracors [perHost]]]]");
        String url = args[0];
        for (int i = 1; i < args.length && i < 5; i++) {
            int k = Integer.valueOf(args[i]);
            if      (i == 1) depth = k;
            else if (i == 2) downloadors = k;
            else if (i == 3) extractors = k;
            else if (i == 4) perHost = k;
        }

        Result result;
        try (Crawler crawler = new WebCrawler(new CachingDownloader(), downloadors, extractors, perHost)) {
            result = crawler.download(url, depth);
        }
        if (Objects.nonNull(result)) {
            for (String line: result.getDownloaded()) System.out.println(line);
            for (Map.Entry<String, IOException> error: result.getErrors().entrySet())
                System.err.printf("ERROR[%s]: %s\n", error.getKey(), String.valueOf(error.getValue()));
        }
    }

    class Aggregator implements Function<String, Result> {
        ConcurrentMap<String, String> urls = new ConcurrentHashMap<>();
        ConcurrentMap<String, IOException> exceptions = new ConcurrentHashMap<>();
        Queue<Future<?>> tasks = new ConcurrentLinkedQueue<>();
        int depthLimit;

        Aggregator(int depth) { depthLimit = depth; }

        @Override
        public Result apply(String url) {
            urls.put(url,url);
            tasks.add(downloading.submit(new Downloading(this, 0, url)));

            Future<?> future;
            while (Objects.nonNull(future = tasks.poll())) try {
                future.get();
            } catch (ExecutionException | InterruptedException ignored) {}

            List<String> result = new ArrayList<>(urls.keySet());
            result.removeAll(exceptions.keySet());

            return new Result(result, exceptions);
        }
    }

    abstract class Base implements Runnable {
        Aggregator agg;
        String url;
        int depth;

        Base(Aggregator agg, int depth, String url) {
            this.agg = agg;
            this.depth = depth;
            this.url = url;
        }

        public void run() {
            try { execute(); }
            catch (IOException e) {
                agg.exceptions.putIfAbsent(url, e);
            }
        }

        public abstract void execute() throws IOException;
    }

    class Downloading extends Base {
        Downloading(Aggregator agg, int depth, String url) { super(agg, depth, url); }

        @Override
        public void execute() throws IOException {
            Document doc;
            String host = URLUtils.getHost(url);
            Semaphore semaphore = hosts.compute(host, this::semaphore);

            semaphore.acquireUninterruptibly();
            try {
                doc = downloader.download(url);
            }
            finally {
                semaphore.release();
            }
            agg.tasks.add(extracting.submit(new Extracting(agg, depth, url, doc)));
        }

        Semaphore semaphore(String url, Semaphore semaphore) {
            return semaphore == null ? new Semaphore(perHost) : semaphore;
        }
    }


    class Extracting extends Base {
        Document doc;

        Extracting(Aggregator agg, int depth, String url, Document doc) {
            super(agg, depth, url);
            this.doc = doc;
        }

        @Override
        public void execute() throws IOException {
            List<String> result = doc.extractLinks();
            ++depth;

            if (depth >= agg.depthLimit)
                return;

            for (String url: result)
                if (agg.urls.put(url, url) == null)
                    agg.tasks.add(downloading.submit(new Downloading(agg, depth, url)));
        }
    }
}
