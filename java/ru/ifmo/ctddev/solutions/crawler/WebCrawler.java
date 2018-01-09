package ru.ifmo.ctddev.solutions.crawler;

import info.kgeorgiy.java.advanced.crawler.*;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import ru.ifmo.ctddev.solutions.mapper.ParallelMapperImpl;

import javax.print.Doc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private int downloaders;
    private int extractors;
    private int perHost;

    protected class DownloadResult {
        protected List<String> extractLinks;
        protected String url;
        protected IOException exception;
    }

    private CopyOnWriteArrayList<String> downloaded;
    private ConcurrentHashMap<String, IOException> errors;


    ParallelMapper downloadMapper;
    ParallelMapper extractMapper;


    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = downloaders;
        this.extractors = extractors;
        this.perHost = perHost;

        errors = new ConcurrentHashMap<>();
        downloaded = new CopyOnWriteArrayList<>();

        downloadMapper = new ParallelMapperImpl(downloaders);
        extractMapper = new ParallelMapperImpl(extractors);
    }

    @Override
    public Result download(String url, int depth) {
        List<String> urls = new ArrayList<>();
        urls.add(url);
        download(urls, depth);

        return new Result(new ArrayList<>(downloaded), errors);
    }


    Function<String, Document> downloadFunc = l -> {
        Document document = null;
        try {
            document = downloader.download(l);
            downloaded.add(l);
        } catch (IOException ex) {
            errors.put(l, ex);
        }
        return document;
    };

    Function<Document, DownloadResult> extractFunc = l -> {
        DownloadResult result = new DownloadResult();
        if (l != null) {
            try {
                result.extractLinks = l.extractLinks();
            } catch (IOException ex) {
                result.url = l.toString();
                result.exception = ex;
            }

        }
        return result;
    };

    private void download(List<String> urls, int depth) {
        try {
            List<Document> documents = downloadMapper.map(downloadFunc, urls);
            if (depth > 1) {
                List<DownloadResult> results = extractMapper.map(extractFunc, documents);
                List<String> extractLink = new ArrayList<>();

                for (DownloadResult result : results) {
                    if (result.extractLinks != null) {
                        for (String s : result.extractLinks) {
                            if (!extractLink.contains(s) && !downloaded.contains(s) && !errors.containsKey(s)) {
                                extractLink.add(s);
                            }
                        }
                    } else if (result.exception != null) {
                        errors.put(result.url, result.exception);
                    }
                }

                download(extractLink, depth - 1);
            }
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            downloadMapper.close();
            extractMapper.close();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }

    }

    public static void main(String[] args) {
        int downloaders = 1;
        int extractors = 1;
        int perHost = 1;

        if (args.length < 1) {
            System.out.println("Usage: WebCrawler url [downloads [extractors [perHost]]]");
            return;
        }

        try {
            if (args.length >= 2) {
                downloaders = Integer.parseInt(args[1]);
            }
            if (args.length >= 3) {
                extractors = Integer.parseInt(args[2]);
            }
            if (args.length >= 4) {
                perHost = Integer.parseInt(args[3]);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid arguments: " + ex.getMessage());
            return;
        }

        try {
            WebCrawler webCrawler = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost);
            webCrawler.download(args[0], 10);
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        }
    }
}