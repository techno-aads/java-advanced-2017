package ru.ifmo.ctddev.solutions.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

public class WebCrawler implements Crawler {

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {

    }

    @Override
    public Result download(String url, int depth) {
        return null;
    }

    @Override
    public void close() {

    }

    public static void main(String[] args) {
        //WebCrawler url [downloads [extractors [perHost]]]
        System.out.println("WebCrawler");
    }
}
