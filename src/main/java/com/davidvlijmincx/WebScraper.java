package com.davidvlijmincx;

import com.davidvlijmincx.bonus.features.FindBestStartingSource;
import com.davidvlijmincx.bonus.features.HighScore;
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

public class WebScraper {

    public static final int PAGES_TO_CRAWL = 100;

    public static void main(String[] args) {
        final var queue = new LinkedBlockingQueue<String>(2000);
        Set<String> visited = ConcurrentHashMap.newKeySet(3000);


        FindBestStartingSource findBestStartingSource = new FindBestStartingSource();
        String bestStart = findBestStartingSource.FindTheBestStart();


        queue.add("http://localhost:8080/v1/crawl/330/57");

        long startTime = System.currentTimeMillis();
        HttpClient client = createHttpClient();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
//        try (var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
//        try (var executor = Executors.newFixedThreadPool(1);) {
            for (int i = 0; i < PAGES_TO_CRAWL; i++) {
                executor.submit(() -> ScopedValue.runWhere(Scrape.CLIENT,
                        client,
                        new Scrape(queue, visited)));
            }
        }

        double score = measureTime(startTime, visited);

        HighScore highScore = new HighScore();
        highScore.submitScore(score);

    }

    private static double measureTime(long startTime, Set<String> visited) {
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        double totalTimeInSeconds = totalTime / 1000.0;

        System.out.printf("Crawled %s web page(s)", visited.size());
        System.out.println("Total execution time: " + totalTime + "ms");

        double throughput = visited.size() / totalTimeInSeconds;
        System.out.println("Throughput: " + throughput + " pages/sec");
        return throughput;
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

    final static ScopedValue<HttpClient> CLIENT = ScopedValue.newInstance();

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
                scope.join();
            }

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

    private Object post(String serviceUrl, String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(url)).uri(URI.create(serviceUrl)).build();
        Scrape.CLIENT.get().send(request, HttpResponse.BodyHandlers.ofString());
        return null;
    }

    private String getBody(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = Scrape.CLIENT.get().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}