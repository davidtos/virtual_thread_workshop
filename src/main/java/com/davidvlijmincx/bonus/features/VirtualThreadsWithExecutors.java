package com.davidvlijmincx.bonus.features;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VirtualThreadsWithExecutors {


    public static void main(String[] args) {


        try(var executor = Executors.newScheduledThreadPool(1)){
            executor.schedule(()-> System.out.println("Hello, World!"), 1, TimeUnit.SECONDS);
        }


        try(var executor = Executors.newSingleThreadExecutor()){
            executor.submit(()-> System.out.println("Hello, World!"));
        }


    }

}
