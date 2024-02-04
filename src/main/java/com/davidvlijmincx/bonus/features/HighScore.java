package com.davidvlijmincx.bonus.features;

import java.util.concurrent.StructuredTaskScope;

public class HighScore {

    public void submitScore(Double score){
        ScoreValidator scoreValidator = new ScoreValidator();

        ScopedValue.runWhere(GlobalScoreVariable.SCORE, score, ()-> {
            try (var scope = new StructuredTaskScope<>()) {
                scope.fork(()-> {
                    scoreValidator.validateAndSubmit();
                    return null;
                });
                scope.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}

class ScoreValidator{

    public void validateAndSubmit(){
        ScoreSubmitter scoreSubmitter = new ScoreSubmitter();

        try (var scope = new StructuredTaskScope<>()) {
            scope.fork(()-> {
                scoreSubmitter.submitScore();
                return null;

            });
            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

class ScoreSubmitter{

    public void submitScore(){
        System.out.println("The score is: " + GlobalScoreVariable.SCORE.get());
    }
}

class GlobalScoreVariable{
    final static ScopedValue<Double> SCORE = ScopedValue.newInstance();
}








