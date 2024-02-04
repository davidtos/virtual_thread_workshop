package com.davidvlijmincx.bonus.features;

public class HighScore {

    public void submitScore(Double score) {
        ScoreValidator scoreValidator = new ScoreValidator();
        scoreValidator.validateAndSubmit();
    }

}

class ScoreValidator {

    public void validateAndSubmit() {
        ScoreSubmitter scoreSubmitter = new ScoreSubmitter();
        scoreSubmitter.submitScore();
    }

}

class ScoreSubmitter {

    public void submitScore() {
        System.out.println("The score is: " + GlobalScoreVariable.SCORE);
    }
}




class GlobalScoreVariable {
    final static Double SCORE = 0.0;
}








