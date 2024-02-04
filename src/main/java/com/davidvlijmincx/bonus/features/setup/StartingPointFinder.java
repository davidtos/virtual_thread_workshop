package com.davidvlijmincx.bonus.features.setup;

public class StartingPointFinder {


    public static StartingPoint source1(){
        sleep(100);
        return new StartingPoint("http://localhost:8080/v1/crawl/330/100", 100);
    }


    public static StartingPoint source2(){
        sleep(200);
        return new StartingPoint("http://localhost:8080/v1/crawl/330/200", 200);
    }

    public static StartingPoint source3(){
        sleep(250);
        return new StartingPoint("http://localhost:8080/v1/crawl/330/50", 50);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
