package rpd.game.results.scored;

import rpd.game.results.basic.Scores;
import rpd.json.values.JSONArray;
import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record PlayResultsScored(FirstStepScored firstStep,
                                List<StepScored> steps)
        implements Serializable {
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
                                                               .map(StepScored::toJson)
                                                               .toList())));
    }
}
