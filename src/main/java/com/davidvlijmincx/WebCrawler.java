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
import java.util.concurrent.*;

public class WebCrawler {

    public static final int PAGES_TO_CRAWL = 200;

    public static void main(String[] args) {
        final var queue = new LinkedBlockingQueue<String>(2000);
        Set<String> visited = ConcurrentHashMap.newKeySet(3000);

        queue.add("http://localhost:8080/v1/crawl/delay/330/57");

        long startTime = System.currentTimeMillis();

        HttpClient client = createHttpClient();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
//        try (var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
//        try (var executor = Executors.newFixedThreadPool(1);) {
            for (int i = 0; i < PAGES_TO_CRAWL; i++) {
                executor.submit(new Spider(queue, visited, client));
            }
        }

        System.out.println("just chilling here");

        measureTime(startTime, visited);

    }

    private static void measureTime(long startTime, Set<String> visited) {
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // convert totalTime to seconds
        double totalTimeInSeconds = totalTime / 1000.0;

        System.out.printf("Crawled %s web page(s)", visited.size());
        System.out.println("Total execution time: " + totalTime + "ms");

        // calculate throughput
        double throughput = visited.size() / totalTimeInSeconds;
        System.out.println("Throughput: " + throughput + " pages/sec");

        // System.out.println(visited);
    }

    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }
}

class Spider implements Runnable {

    private final LinkedBlockingQueue<String> pageQueue;

    private final Set<String> visited;

    private final HttpClient client;

    public Spider(LinkedBlockingQueue<String> pageQueue, Set<String> visited, HttpClient client) {
        this.pageQueue = pageQueue;
        this.visited = visited;
        this.client = client;
    }

    @Override
    public void run() {

        try {
            String url = pageQueue.take();
            Document document = Jsoup.parse(getBody(url));
            Elements linksOnPage = document.select("a[href]");

            // do everything separately
            visited.add(url);
            for (Element link : linksOnPage) {
                String nextUrl = link.attr("abs:href");
                if (nextUrl.contains("http")) {
                    pageQueue.add(nextUrl);
                }
            }

            // do stuff as a group
//                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
//
//                    scope.fork(() -> visited.add(url));
//                    scope.fork(() -> {
//                                for (Element link : linksOnPage) {
//                                    String nextUrl = link.attr("abs:href");
//                                    if (nextUrl.contains("http")) {
//                                        pageQueue.add(nextUrl);
//                                    }
//                                }
//                                return null;
//                            }
//                    );
//                }

            // doing everything separate;
//                    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
//
//                        executor.submit(() -> visited.add(url));
//                        executor.submit(() -> {
//                            for (Element link : linksOnPage) {
//                                String nextUrl = link.attr("abs:href");
//                                if (nextUrl.contains("http")) {
//                                    pageQueue.add(nextUrl);
//                                }
//                            }
//                        });
//                    }


            // assignment to just store it somewhere but not caring where to store it.
            try (var scope = new StructuredTaskScope.ShutdownOnSuccess<>()) {

                scope.fork(() -> post("http://localhost:8080/v1/VisitedService/1", url));
                scope.fork(() -> post("http://localhost:8080/v1/VisitedService/2", url));
                scope.fork(() -> post("http://localhost:8080/v1/VisitedService/3", url));

                scope.join();

            }


        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private Object post(String serviceUrl, String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(url)).uri(URI.create(serviceUrl)).build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            //   System.out.println("e = " + e);
        }
        return null;
    }

    private String getBody(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}

