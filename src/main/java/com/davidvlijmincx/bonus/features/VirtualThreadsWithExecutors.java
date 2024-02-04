package com.davidvlijmincx.bonus.features;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VirtualThreadsWithExecutors {


    public static void main(String[] args) {


        var executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> System.out.println("Hello, World!"), 1, TimeUnit.SECONDS);
        executor.shutdown();


        var executor1 = Executors.newSingleThreadExecutor();
        executor1.submit(() -> System.out.println("Hello, World!"));
        executor1.shutdown();

    }

}
