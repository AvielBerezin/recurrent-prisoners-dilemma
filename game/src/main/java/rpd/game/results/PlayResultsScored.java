package rpd.game.results;

import java.util.List;

public record PlayResultsScored(FirstStepScored firstStep,
                                List<StepScored> steps) {
    public PlayResultsScored(FirstStepScored firstStep, StepScored...steps) {
        this(firstStep, List.of(steps));
    }

    public Scores getFinalScores() {
        if (steps.size() > 0) {
            return steps.get(steps.size() - 1).scoresTotal();
        }
        return firstStep.scoresEarned();
    }
}
