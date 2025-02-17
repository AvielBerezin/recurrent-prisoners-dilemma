package rpd.game.results.unscored;

import rpd.game.results.basic.Actions;
import rpd.game.results.basic.Experiences;
import rpd.game.results.basic.Scores;
import rpd.game.results.scored.FirstStepScored;
import rpd.game.results.scored.PlayResultsScored;
import rpd.game.results.scored.StepScored;
import rpd.player.Option;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record PlayResults(FirstStep firstStep,
                          List<Step> steps)
        implements Serializable {
    public PlayResults(FirstStep firstStep, Step... steps) {
        this(firstStep, List.of(steps));
    }

    public PlayResultsScored scored() {
        FirstStepScored firstStepScored = new FirstStepScored(firstStep,
                                                              getScoresEarned(firstStep.actions(),
                                                                              firstStep.currentExperiences()));
        if (steps.size() == 0) {
            return new PlayResultsScored(firstStepScored);
        }
        ArrayList<StepScored> stepsScored = new ArrayList<>(steps.size());
        {
            Scores scoresEarned = getScoresEarned(steps.get(0).actions(),
                                                  steps.get(0).currentExperiences());
            stepsScored.add(new StepScored(steps.get(0),
                                           scoresEarned,
                                           firstStepScored.scoresEarned().plus(scoresEarned)));
        }
        for (int i = 1; i < steps.size(); i++) {
            Scores scoresEarned = getScoresEarned(steps.get(i).actions(),
                                                  steps.get(i).currentExperiences());
            stepsScored.add(new StepScored(steps.get(i),
                                           scoresEarned,
                                           stepsScored.get(i - 1).scoresTotal().plus(scoresEarned)));
        }
        return new PlayResultsScored(firstStepScored, stepsScored);
    }

    private Scores getScoresEarned(Actions actions, Experiences experiences) {
        return new Scores(getScore(actions.first(), experiences.first()),
                          getScore(actions.second(), experiences.second()));
    }

    private int getScore(Option action, Option experience) {
        int actionIndex = switch (action) {
            case COOPERATE -> 0;
            case DEFLECT -> 1;
        };
        int experienceIndex = switch (experience) {
            case COOPERATE -> 0;
            case DEFLECT -> 1;
        };
        List<List<Integer>> scoreMatrix = List.of(List.of(1, 0),
                                                  List.of(2, 0));
        return scoreMatrix.get(actionIndex)
                          .get(experienceIndex);
    }
}
