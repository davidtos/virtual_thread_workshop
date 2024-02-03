# Web Scraper Workshop
Welcome to the workshop where you are going to build a web scraper that is going to use:

- Platform threads
- Virtual threads
- Structured task scope
- Scoped values

The workshop starts with a simple single-threaded web scraper that only scrapes a single page. You are going to improve this web scraper by first making it multithreaded using platform threads and later by using virtual threads. This new type of thread is a great
new addition to the Java language but doesn't work the same as platform threads in every situation. During this workshop you are going to experience when virtual threads work best, and when they work just oke-ish.

To follow along with the workshop you need to also check out [this repository](https://github.com/davidtos/workshop_server).
The repo contains a Spring application that is going to act as the web server that you are going to scrape.

## How to follow along with the workshop
Below you will find the steps of the workshop. The best way to follow along is to start with step 1 and to keep developing inside this branch. If you want to start every step of the workshop with a clean branch you can also check out the branch belonging to that step. Each step has a branch inside this git repo with the same name. If you are falling behind please say so, then we can adjust the speed of the workshop :-) or you can check out at the branch of the next step.

# TL;DR
- Let's build a web scraper!
- Run the Spring project inside [this repository](https://github.com/davidtos/workshop_server), it has the web server we scrape inside it
- Follow along with the steps below (Ron and David give some theory, hints, and background info between steps)
- Are you already done and want to start with the next step? go head! :-)
- Any questions? Feel free to ask! We are happy to answer them

# Requirements
To follow along with this workshop you need the following things:

- Java 21
- Check out and run the project in [this repository](https://github.com/davidtos/workshop_server)
- Check out this repository if you haven't done so already

# The steps of the workshop:
Just follow along with the following steps. If you have any questions feel free to ask Ron and I are there to answer them.
We will you give some needed information between steps, so you can focus on solving one type of problem at a time. :-)

## (Step 1) - check out the code
You need to check out these two repositories:

### The scraper basis https://github.com/davidtos/virtual_thread_workshop
This is the repository you are looking at right now. It contains all the steps/branches and starting information that you will need to build the scraper.

### The web server https://github.com/davidtos/workshop_server.
This is the webserver that the scraper is going to scrape. The repository contains a Spring boot application that you can run in the background while you build the Scraper. You don't have to make any changes to this project.

When you have both projects checked out and the Spring boot application running you can verify if everything works as it should. To check that everything works you can run the WebScraper class from this repository; it should scrape a single page.

## (Step 2) - Add platform threads
Check out the following branch "add platform threads" if you haven't done so already. This branch is the basis of the web scraper.
you can already run it, and it will scrape a single page from the web server/ Spring boot application.

The first step is to make the **Scrape** class run concurrently using platform threads. The goal is to be able to create any number of
Scrape instances that each scrape a single page.

<details>
<summary>Hint</summary>
One way to achieve this is by using the Executors services. 
</details>

## (Step 3) - Start using virtual threads
You can now scrape webpages using multiple Scrape instances that each run on a Platform Thread. The next step is to change it in such a way that it uses Virtual threads instead.  To do this you can use the Thread class or an Executor.

Before you make the change take a good look at the performance so you can make a fair comparison between Virtual threads and Platform threads.

> Make it easy to switch between Virtual and Platform threads, so you can switch between the two to see the difference in performance.
> Doesn't need to be anything fancy commenting out a line of code is fine.

## (Step 4) - Difference between virtual and platform threads
The scraper is now able to run on either Virtual Threads or Platform threads. To see the impact these Threads have on the Scraper
you can play with the following two variables:

1. The URL of the scraper
2. The number of threads/tasks you create.

The URLs you can use are:
- http://localhost:8080/v1/crawl/delay/330/57
- http://localhost:8080/v1/crawl/330/57

The first endpoint has a delay between 10 and 200 milliseconds, forcing the threads to block and wait for several milliseconds.
The URL without the delay returns immediately without the extra waiting;
meaning that the responses from this endpoint are very quick and the thread is not blocked for very long.

The second thing you can change is the number of scrape tasks you start. Try out the difference it makes when you submit
200 or 400 or even a 1000+ scrape task to a pool of platform threads or in the case of virtual threads create as many virtual threads as you have jobs.

Some of the results will surprise you :-)

> Note: To get a good idea of the impact. I recommend trying out lots of tasks with the delay endpoint on both virtual and platform threads. And the same thing but without the endpoint with the delay.

> **_!!Bonus!!_** Let's enable virtual thread on the (Spring) back-end, Spring now support virtual threads. Let's enable them
> to see how the scraper is impacted and what it does with the back-end application.

## (Step 5) - Find the pinned virtual thread
Virtual threads are unmounted when they are blocked for example, when they are waiting on the response of a web server. Unmounting is a powerful feature but doesn't always work (yet)...
When the unmounting doesn't happen we say that the virtual is pinned.
A pinned virtual thread causes not only the virtual thread but also the carrier thread it's running on to be blocked. As you may expect this causes performance issues.

Now it's up to you to fix the scraper and replace the functionality that causes virtual threads to be pinned.

To help you find the method causing issues you can use one of the following VM options:
```text
-Djdk.tracePinnedThreads=short

-Djdk.tracePinnedThreads=full
```
Run the web scraper with one of these two options and replace the functionality with one that does not cause the virtual threads
to be pinned. Try them both out and see what the difference is between the both of them and which one helps you the most to fix the issue.

<details>
<summary>Hint</summary>
Java 9 added an HTTP client that does not block
</details>

## (Step 6) - Set carrier threads (Improve performance branch)
By default, you get as many carrier threads as there are cores available inside your system. There are two ways to tweak the
number of carrier threads that get created.

Use the following options and see what impact it has on your scraper.
```text
-Djdk.virtualThreadScheduler.parallelism=1 

-Djdk.virtualThreadScheduler.maxPoolSize=1
```

Try out some different numbers and see if it increases or lowers the amount of pages per second you can scrape.

> These options are not needed for the following steps.

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
Run the Scraper a few times with and without the improvement to see the difference in performance it makes.

## (Step 8) - Use StructuredTaskScope
> For this and the following steps it may maybe necessary to run your application with the `--enable-preview` flag.

During the previous step, you started two virtual threads inside another virtual thread. This is a great way to run things concurrently, but it creates an implicit relationship between the threads. What should happen when a thread fails? The desired behavior in this case would be all or nothing, either all threads succeed or we do a rollback.

During this step, we are going to improve the code to make the relationship these threads have more explicit. This help other
developers to better understand the intent of your code, and enables you to use a powerful way of managing the lifetime of threads.

For this step rewrite the code from the previous assignment in a way that it uses `StructuredTaskScope.ShutdownOnFailure()` the idea is
to fork new threads using the StructuredTaskScope.

## (Step 9) - Implement ShutdownOnSuccess
`ShutdownOnFailure` is not the only shutdown policy that you get with Java 21. During this step, you are going to implement the
`ShutdownOnSuccess` shutdown policy. The **ShutdownOnSuccess** policy states that it will shut down the scope after a threads finishes successfully.

For the next step, we are going to let another service know what page we just scraped. To improve the speed of the scraper it doesn't matter
which instance processes the request first. The fastest instance to process the request is the winner as far as the scraper is concerned.

The URLs of the instances are:
- http://localhost:8080/v1/VisitedService/1
- http://localhost:8080/v1/VisitedService/2
- http://localhost:8080/v1/VisitedService/3

The services expect a POST request with a URL as the body.

Now it is up to you to implement the ShutdownOnSuccess scope in a way that a new virtual thread is forked for each one of the service instances.

If you are using the HttpClient you can use the following code to do a POST request to an instance:
```java
private Object post(String serviceUrl, String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(url)).uri(URI.create(serviceUrl)).build();
    client.send(request, HttpResponse.BodyHandlers.ofString());
    return null;
}
```

## (Step 10) - Use scoped values
The name of this step already gave it away, but for the last step, you are going to add scoped values to the scraper.
You need to change the Scraper in such a way that each Scraper instance runs in a scope where the HTTP client is already known.

The goal is to no longer pass the HttpClient as a constructor parameter to the Scraper but you implement it as a ScopedValue. This way the Client
is known inside the scraper and all the subsequent calls.

> Note: During the implementation notice that the child virtual threads can use the same client as you passed to the parent thread.
> When you use the structured task scope all the threads you fork will have the same scoped values as the parent because they
> run in the same scope as the parent.

## Bonus feature 1: Scope inheritance
You may have used scope inheritance in step 10 already, but let's focus on it a bit more with this bonus feature. Scope inheritance
is the mechanism in which a child thread, inherits the scope from the parent thread. Both threads keep running in the same scope.

To show you how inheritance of scoped values works you will implement the HighScore class from the `bonus.feature` package.
The challenge is to pass the score to the submitScore method without using it as a method parameter. You will have to use scope values. 

## Bonus feature 2: Rebinding scoped values
Since we have access to the code, lets cheat a little with our scores. for this bonus feature you are going to increase the score with 1000 during the validation step.
To do this you will need to rebind the Score scoped value. 

## Bonus feature 3: Creating your own shutdown task scope
For this feature, we are going to implement our own task scope. Till now, we have only used the built-in task scopes/ shutdown policies. Let's try something else
and implement our own task scope. on the `bonus.features` package you can find the FindBestStartingSource class file. Inside this class you can find the starting point.

To implement you own scope you need to override the `handleComplete()` method and call the `shutdown()` when you want to stop the remaining threads.k 

## Bonus feature 4: custom Structured task scope: moving the business logic
Having all the domain logic inside the scope class is the king of ugly. It's not a nice place to have business logic. 
You can improve this in any way you want, you can be creative :). The only hint I will give is to use a `Predicate`. 

## Bonus feature 5: Deadlines with structured concurrency
No one likes to wait forever. So let us add some deadlines to the scope we were working with. Create a deadline for any number
of milliseconds, and look at what it does with your code, and virtual threads.

## Bonus feature 6: Virtual thread with existing executors
Virtual threads are great, but how can we use it with existing code? During this assingment you will implement virtual threads with existing executors. In the bonus feature
package you can find the  `VirtualThreadsWithExecutors` class. Currently, it uses platform threads, but it is up to you now to implement it with virtual threads.  

You can try them out on the web scraper or the Spring/back-end application and see how it affects your application.

## bonus feature 7: Limit the number of requests without limiting the virtual thread creation
The importance of virtual threads is that you should use them as threads, but more like task you want to run concurrently.
For the last bonus feature, you are tasked with limiting the number of requests going out the back-end server, without limiting the number 
of virtual threads that get created. You can use anything you want, but I would recommend to use a kind of lock :) 








