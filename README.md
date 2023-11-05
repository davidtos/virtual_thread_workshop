# virtual_thread_workshop

## assigment 1
check out code

## assigment 2
make it work with platform threads

## assigment 3
add the option to use virtual threads instead

- show that it doesn't make a difference in performance (you need to measure it, to improve it)
- tell about the VT and their carrier threads

## assigment 4
- Let them use a webserver that returns without delay
- Show that VT are not faster than PT
- Switch to "normal" behaving end-points  that check the performance improvement

## Assignment 5
Find the sync method and fix it

## Assignment 6
Set the amount of carrier threads

## Assignment 7
Add the first performance improvement and run add url and for loop to add urls concurrently  

## Assignment 8
introduce structured concurrency and improve it by adding the StructuredTaskScope

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



