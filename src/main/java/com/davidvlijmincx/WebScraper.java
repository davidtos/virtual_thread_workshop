package com.davidvlijmincx;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;

public class WebScraper {

    public static final int PAGES_TO_CRAWL = 100;

    public static void main(String[] args) {
        final var queue = new LinkedBlockingQueue<String>(2000);
        Set<String> visited = ConcurrentHashMap.newKeySet(3000);

        queue.add("http://localhost:8080/v1/crawl/330/57");

        long startTime = System.currentTimeMillis();
        HttpClient client = createHttpClient();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
//        try (var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
//        try (var executor = Executors.newFixedThreadPool(1);) {
            for (int i = 0; i < PAGES_TO_CRAWL; i++) {
                executor.submit(() -> ScopedValue.runWhere(Scrape.URL,
                        () -> {
                            try {
                                return queue.take();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        new Scrape(queue, visited, client)));
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

    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }
}

class Scrape implements Runnable {

    final static ScopedValue<Supplier<String>> URL = ScopedValue.newInstance();

    private final LinkedBlockingQueue<String> pageQueue;

    private final Set<String> visited;

    private final HttpClient client;

    public Scrape(LinkedBlockingQueue<String> pageQueue, Set<String> visited, HttpClient client) {
        this.pageQueue = pageQueue;
        this.visited = visited;
        this.client = client;
    }

    @Override
    public void run() {

        try {
            String url = Scrape.URL.get().get();

            Document document = Jsoup.parse(getBody(url));
            Elements linksOnPage = document.select("a[href]");

            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                scope.fork(() -> visited.add(url));
                scope.fork(() -> {
                            for (Element link : linksOnPage) {
                                String nextUrl = link.attr("abs:href");
                                if (nextUrl.contains("http")) {
                                    pageQueue.add(nextUrl);
                                }
                            }
                            return null;
                        }
                );
            }

            try (var scope = new StructuredTaskScope.ShutdownOnSuccess<>()) {

                scope.fork(() -> post("http://localhost:8080/v1/VisitedService/1"));
                scope.fork(() -> post("http://localhost:8080/v1/VisitedService/2"));
                scope.fork(() -> post("http://localhost:8080/v1/VisitedService/3"));

                scope.join();

            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private Object post(String serviceUrl) throws IOException, InterruptedException {
        String url = Scrape.URL.get().get();
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(url)).uri(URI.create(serviceUrl)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        return null;
    }

    private String getBody(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}