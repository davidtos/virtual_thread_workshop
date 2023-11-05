# virtual_thread_workshop

## assigment 1
check out code

## assigment 2
make it work with platform threads

## Start using virtual threads
add the option to use virtual threads instead

- show that it doesn't make a difference in performance (you need to measure it, to improve it)
- tell about the VT and their carrier threads

## Difference between virtual and platform threads
- Let them use a webserver that returns without delay
- Show that VT are not faster than PT
- Switch to "normal" behaving end-points  that check the performance improvement

## find the pinned virtual thread
Find the sync method and fix it

```text
-Djdk.tracePinnedThreads=short

-Djdk.tracePinnedThreads=full
```

## Set carrier threads (Improve performance branch)
Set the amount of carrier threads

## Improve performance
Add the first performance improvement and run add url and for loop to add urls concurrently 

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

                executor.submit(() -> visited.add(url));
                executor.submit(() -> {
                    for (Element link : linksOnPage) {
                        String nextUrl = link.attr("abs:href");
                        if (nextUrl.contains("http")) {
                            pageQueue.add(nextUrl);
                        }
                    }
                });
            }
```


## Use StructuredTaskScope
introduce structured concurrency and improve it by adding the StructuredTaskScope

```java
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
```


## Implement ShutdownOnSuccess
introduce the ShutdownOnSuccess scope

```java
try (var scope = new StructuredTaskScope.ShutdownOnSuccess<>()) {

    scope.fork(() -> post("http://localhost:8080/v1/VisitedService/1", url));
    scope.fork(() -> post("http://localhost:8080/v1/VisitedService/2", url));
    scope.fork(() -> post("http://localhost:8080/v1/VisitedService/3", url));

    scope.join();

}

private Object post(String serviceUrl, String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(url)).uri(URI.create(serviceUrl)).build();
    client.send(request, HttpResponse.BodyHandlers.ofString());
    return null;
}
```

## Use scoped values 
use scoped values for the url lue

```java
    final static ScopedValue<Supplier<String>> URL = ScopedValue.newInstance();

        Scrape.URL.get();

executor.submit(() -> ScopedValue.runWhere(Scrape.URL,
        () -> {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },
        new Scrape(queue, visited, client)));
```

- show the performance impact
- show the inheritance when using structured task scope



