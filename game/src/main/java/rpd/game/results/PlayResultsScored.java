package rpd.game.results;

import rpd.game.json.JSONArray;
import rpd.game.json.JSONObject;
import rpd.game.json.JSONValue;

import java.util.List;
import java.util.Map;

public record PlayResultsScored(FirstStepScored firstStep,
                                List<StepScored> steps) {
    public PlayResultsScored(FirstStepScored firstStep, StepScored... steps) {
        this(firstStep, List.of(steps));
    }

    public Scores getFinalScores() {
        if (steps.size() > 0) {
            return steps.get(steps.size() - 1).scoresTotal();
        }
        return firstStep.scoresEarned();
    }

    public JSONValue toJson() {
        return JSONObject.of(Map.of("firstStep", firstStep.toJson(),
                                    "steps", JSONArray.of(steps.stream()
                                                               .map(stepScored -> stepScored.toJson())
                                                               .toList())));
    }
}
