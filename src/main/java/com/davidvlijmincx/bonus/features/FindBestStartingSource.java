package com.davidvlijmincx.bonus.features;

import com.davidvlijmincx.bonus.features.setup.StartingPoint;
import com.davidvlijmincx.bonus.features.setup.StartingPointFinder;

import java.util.concurrent.StructuredTaskScope;

public class FindBestStartingSource {

    public String FindTheBestStart(){

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            StructuredTaskScope.Subtask<StartingPoint> fork = scope.fork(StartingPointFinder::source1);
            StructuredTaskScope.Subtask<StartingPoint> fork1 = scope.fork(StartingPointFinder::source2);
            StructuredTaskScope.Subtask<StartingPoint> fork2 = scope.fork(StartingPointFinder::source3);

            scope.join();

                /// Show the state of the virtual thread for debug purposes
//            System.out.println("fork.state() = " + fork.state());
//            System.out.println("fork.state() = " + fork1.state());
//            System.out.println("fork.state() = " + fork2.state());

            StartingPoint result = new StartingPoint("",0); // Get a result from the scope

            System.out.println("result: " + result.getUrlsOnPage() + " with URL: " + result.getUrl() );
            return result.getUrl();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}


class CriteriaScope {

    private volatile StartingPoint startingPoint;


    public StartingPoint getResult(){return startingPoint;}
}