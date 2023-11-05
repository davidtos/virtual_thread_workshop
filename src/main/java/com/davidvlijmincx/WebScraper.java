package com.davidvlijmincx;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class WebScraper {

    public static final int PAGES_TO_CRAWL = 100;

    public static void main(String[] args) {
        final var queue = new LinkedBlockingQueue<String>(2000);
        Set<String> visited = ConcurrentHashMap.newKeySet(3000);

        queue.add("http://localhost:8080/v1/crawl/330/57");

        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
//        try (var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
//        try (var executor = Executors.newFixedThreadPool(1);) {
            for (int i = 0; i < PAGES_TO_CRAWL; i++) {
                executor.submit(new Scrape(queue, visited));
            }
        }

        measureTime(startTime, visited);

    }

    private static void measureTime(long startTime, Set<String> visited) {
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        double totalTimeInSeconds = totalTime / 1000.0;

        System.out.printf("Crawled %s web page(s)", visited.size());
        System.out.println("Total execution time: " + totalTime + "ms");

        double throughput = visited.size() / totalTimeInSeconds;
        System.out.println("Throughput: " + throughput + " pages/sec");
    }

}

class Scrape implements Runnable {

    private final LinkedBlockingQueue<String> pageQueue;

    private final Set<String> visited;

    public Scrape(LinkedBlockingQueue<String> pageQueue, Set<String> visited) {
        this.pageQueue = pageQueue;
        this.visited = visited;
    }

    @Override
    public void run() {

        try {
            String url = pageQueue.take();

            Document document = Jsoup.connect(url).get();
            Elements linksOnPage = document.select("a[href]");

            visited.add(url);
            for (Element link : linksOnPage) {
                String nextUrl = link.attr("abs:href");
                if (nextUrl.contains("http")) {
                    pageQueue.add(nextUrl);
                }
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}