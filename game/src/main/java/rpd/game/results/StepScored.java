package rpd.game.results;

import rpd.game.json.JSONObject;
import rpd.game.json.JSONValue;
import rpd.game.results.without_scores.Step;

import java.util.Map;

public record StepScored(Step step,
                         Scores scoresEarned,
                         Scores scoresTotal) {
    public JSONValue toJson() {
        return JSONObject.of(Map.of("step", step.toJson(),
                                    "scoresEarned", scoresEarned.toJson(),
                                    "scoresTotal", scoresTotal.toJson()));
    }
}
