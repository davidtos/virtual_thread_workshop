package com.davidvlijmincx.bonus.features;

import com.davidvlijmincx.bonus.features.setup.StartingPoint;
import com.davidvlijmincx.bonus.features.setup.StartingPointFinder;

import java.util.concurrent.StructuredTaskScope;

public class FindBestStartingSource {

    public String FindTheBestStart(){
        try (var scope = new CriteriaScope()) {

            StructuredTaskScope.Subtask<StartingPoint> fork = scope.fork(StartingPointFinder::source1);
            StructuredTaskScope.Subtask<StartingPoint> fork1 = scope.fork(StartingPointFinder::source2);
            StructuredTaskScope.Subtask<StartingPoint> fork2 = scope.fork(StartingPointFinder::source3);

            scope.join();

            System.out.println("fork.state() = " + fork.state());
            System.out.println("fork.state() = " + fork1.state());
            System.out.println("fork.state() = " + fork2.state());

            StartingPoint result = scope.getResult();
            System.out.println("result: " + result.getUrlsOnPage() + " with URL: " + result.getUrl() );
            return result.getUrl();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}


class CriteriaScope extends StructuredTaskScope<StartingPoint> {

    private volatile StartingPoint startingPoint;

    @Override
    protected void handleComplete(Subtask<? extends StartingPoint> subtask) {
        if (subtask.state() == Subtask.State.SUCCESS && subtask.get().getUrlsOnPage() > 150) {
            this.startingPoint = subtask.get();
            shutdown();
        }
    }


    public StartingPoint getResult(){return startingPoint;}
}