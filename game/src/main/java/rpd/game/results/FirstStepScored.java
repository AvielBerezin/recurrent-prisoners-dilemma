package rpd.game.results;

import rpd.game.results.without_scores.FirstStep;

public record FirstStepScored(FirstStep step,
                              Scores scoresEarned) {}
