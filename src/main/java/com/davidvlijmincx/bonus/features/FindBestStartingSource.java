package com.davidvlijmincx.bonus.features;

import com.davidvlijmincx.bonus.features.setup.StartingPoint;
import com.davidvlijmincx.bonus.features.setup.StartingPointFinder;

import java.util.concurrent.StructuredTaskScope;
import java.util.function.Predicate;

public class FindBestStartingSource {

    public String FindTheBestStart(){


        Predicate<StructuredTaskScope.Subtask<? extends StartingPoint>> predicate =
                subtask -> subtask.state() ==
                        StructuredTaskScope.Subtask.State.SUCCESS &&
                        subtask.get().getUrlsOnPage() > 150;

        try (var scope = new CriteriaScope(predicate)) {

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
    private final Predicate<StructuredTaskScope.Subtask<? extends StartingPoint>> predicate;

    public CriteriaScope(Predicate<Subtask<? extends StartingPoint>> predicate) {
        this.predicate = predicate;
    }

    private volatile StartingPoint startingPoint;

    @Override
    protected void handleComplete(Subtask<? extends StartingPoint> subtask) {
        if (predicate.test(subtask)) {
            this.startingPoint = subtask.get();
            shutdown();
        }
    }


    public StartingPoint getResult(){return startingPoint;}
}