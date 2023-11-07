# Web Scraper Workshop
Welcome by the workshop where you are going to build a web scraper that is going to use:

- Platform threads
- Virtual threads
- Structured task scope
- Scoped values

The workshop start with just a simple single threaded web scraper that only scrapes a single page. You are going to improve
this web scraper by first making it multithreading followed by using virtual threads. These new kind of threads are a great
new addition to the Java languages but don't work the same as the old threads in every situation. During this workshop you
are going to experience when virtual threads work best, and when they work just oke-ish.

To follow along with the workshop you need to also check out [this repository](https://github.com/davidtos/workshop_server).
The repo contains a Spring application that is going to act as the server where the scraper is going to be talking to.

## How to follow along with the workshop
Below you will find the steps of the workshop. The best way to following along is to start with step 1 and building
inside this branch. If you want to start every step of the workshop with a clean branch you can also check out the branch belonging to
that step. Each step has a branch inside this git repo with the same name. If you are falling behind please say so, then we
can adjust the speed of the workshop :-) or you can also check at the branch of the next step.

## Requirements 
To following along with this workshop you need the following things:

- Java 21
- Check out and run the project in [this repository](https://github.com/davidtos/workshop_server).
- Check out this repository if you haven't done so already

# TL;DR
- Let's build a web scraper!
- Run the Spring project inside [this repository](https://github.com/davidtos/workshop_server), it has the web server we scrape inside it
- Follow along with the steps below (Ron and David give some theory, hints and background info between steps)
- Already done and want to start with the next step? go head! :-)
- Any questions? Feel free to ask! We are happy to answer them

# The steps of the workshop:
Just follow along with the following steps. If you have any questions feel free to ask Ron and I are there to answer them.
We will you give some needed information between steps, so you can focus on solving one problem at a time. :-)

## (Step 1) - check out the code
You need to check out these two repositories:

### The scraper basis https://github.com/davidtos/virtual_thread_workshop
This is the repository you are looking at. It contains all the steps/branches that you will need to build the scraper.

### The web server https://github.com/davidtos/workshop_server.
This is the webserver that the scraper is going to scape. You can run the Spring project and let it run in the background.
You don't have to make any changes inside this project.

To check if everything works you can try and run the WebScraper class; It should scrape a single page. You are going to improve the
speed with Virtual threads; We Promise!

## (Step 2) - add platform threads
If you didn't do it already check out the following branch "add platform threads" this is the basis of the web scraper.
you can already run it, and it will scrape a single page from the web server.

The first step is to make the **Scrape** class run concurrently using platform threads. The goal is to be able to create any number of
Scrape instances that each can scrape a single page. 

<details>
<summary>Hint</summary>
One way to achieve this is by using the Executors services. 
</details>

## (Step 3) - Start using virtual threads
You can now scrape webpages using multiple Scrape instances that each run on a Platform thread. The next step is to implement
the same thing, but with Virtual Threads instead of Platform threads.

Take a good look at how this affect the performance of the scraper.

> Make it easy to switch between Virtual and Platform threads, so you can switch between the two to see the difference in performance.
> Doesn't need to any be fancy commenting out a line of code is fine.


# REMOVE
- show that it doesn't make a difference in performance (you need to measure it, to improve it)
- tell about the VT and their carrier threads

## (Step 4) - Difference between virtual and platform threads
The scraper is now able to run on either Virtual Threads or on Platform threads. To see the impact these Thread have on the Scraper
you can play with the following two variables:

1. The URL of the scraper
2. The number of threads you create.

The URLs you can use are:
- http://localhost:8080/v1/crawl/delay/330/57
- http://localhost:8080/v1/crawl/330/57

The first end point has a delay between 10 and 200 milliseconds, forcing the threads to block and wait for a number of milliseconds.
The other URL without the delay returns immediately without waiting; with this URL the thread doesn't have to wait a long time to get a response.

The second thing you can change is the number of the scrape jobs you start. Try out what differance it makes when you submit
200 or 400 or even a 1000 scrape task to a pool of platform threads or create as many virtual threads as you have jobs.

The results may surprise you :-)

<details>
<summary>Note</summary>
Try out lots of task with the delay endpoint.
</details>


# REMOVE
- Let them use a webserver that returns without delay
- Show that VT are not faster than PT
- Switch to "normal" behaving end-points  that check the performance improvement

## (Step 5) - find the pinned virtual thread
Virtual threads are unmounted when they are blocked like for example, when they are waiting on the response of a web server. Unmouting a nice feature but doesn't (yet) always work...
When the unmouting doesn't happen we say that the virtual is pinnend. The result is that the Virtual thread and Carrier thread its running on are blocked.

It's up to you to fix the scraper and replace the functionality that causes virtual threads to be pinned.

To help you find the method causing issues you can use one the following VM options:
```text
-Djdk.tracePinnedThreads=short

-Djdk.tracePinnedThreads=full
```
Run the web scraper with one of these two options and replace the functionality with one that does not cause the virtual thrads
to be pinned. Try them both out and see what the difference is between the both of them and which one helps you the most to fix the issue.

<details>
<summary>Hint</summary>
Java 9 added a http client that does not block
</details>

## (Step 6) - Set carrier threads (Improve performance branch)
By default, you get as many carrier thread as there are cores available inside you system. There are two ways to tweak the
number of carrier threads that get created. 

Use the following options and see what impact it has on your scraper.
```text
jdk.virtualThreadScheduler.parallelism=5

jdk.virtualThreadScheduler.maxPoolSize=10
```

Try out some diffenent numbers and see if it increases or lowers the amount of pages per second you are able to scrape.

> You can remove these options when you continue to the next step.

## (Step 7) - Improve performance
The next step is to improve the performance of the scraper. Make it so that the following operations run in their own virtual thread.

```java
visited.add(url);
    for (Element link : linksOnPage) {
    String nextUrl = link.attr("abs:href");
    if (nextUrl.contains("http")) {
        pageQueue.add(nextUrl);
    }
}
```
Run the Scraper a few times with and without the improvement to see the differance it makes if any. 

## (Step 8) - Use StructuredTaskScope
> For this and the following steps it is maybe necessary to run your application with the `--enable-preview` flag. 

During the last step you started two virtual threads inside another virtual thread. This is a great way to run things concurrently, but its creates an implicit relationship
between the threads. What should happen when a thread fails? In this case it all or nothing, either all threads succeed or none do. 

During this step we are going to improve the code to make the relationship between the threads more explicit. This help other
developers to better understand the intent of your code, and you can use a powerful way of managing the lifetime of threads.

For this step rewrite the code from the previous assignment in a way that it uses `StructuredTaskScope.ShutdownOnFailure()` the idea is 
to fork a new thread using the StructuredTaskScope.

## (Step 9) - Implement ShutdownOnSuccess
`ShutdownOnFailure` is not the only shutdown policy that you get with Java 21. During this step you are going to implement the 
`ShutdownOnSuccess` shutdown policy. The **ShutdownOnSuccess** states that it will shut down the scope after a threads finishes successful.

For the next step we are going to let another service know what page we just scrolled. To improve the speed of the scraper it doesn't matter
which instance processes the request first. The fastest instance is the winner as far as the scraper is concerned.

The URLs of the instances is:
- http://localhost:8080/v1/VisitedService/1
- http://localhost:8080/v1/VisitedService/2
- http://localhost:8080/v1/VisitedService/3

The services expect a POST request with a URL as body.

Now it is up to you to implement the ShutdownOnSuccess scope in a way that a new virtual thread is forked for each one of the service instances.


If you are using the HttpClient you can use the following code:
```java
private Object post(String serviceUrl, String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(url)).uri(URI.create(serviceUrl)).build();
    client.send(request, HttpResponse.BodyHandlers.ofString());
    return null;
}
```

## (Step 10) - Use scoped values
The name of this step already gave it away, but for the last step you are going to add scoped values to the scraper.
You need to change the Scraper in such a way that each scraper instance run in a scope where the http client is already known.

The goal is to no longer pass the HttpClient as a constructor parameter to the Scraper but that you implement it as a ScopedValue. This way the Client
is known inside the scraper and all the subsequent calls.

> Note: During the implementation notice that the child virtual threads can use the same client as you passed to the parent thread.
> When you use the structured task scope all the threads you fork will have the same scoped values as the parent becasue they
> run in the same scope as the parent.


