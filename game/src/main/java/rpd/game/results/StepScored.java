package rpd.game.results;

import rpd.game.results.without_scores.Step;

public record StepScored(Step step,
                         Scores scoresEarned,
                         Scores scoresTotal) {}
