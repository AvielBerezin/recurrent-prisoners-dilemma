package rpd.game.results;

import rpd.game.json.JSONObject;
import rpd.game.json.JSONValue;
import rpd.game.results.without_scores.FirstStep;

import java.util.Map;

public record FirstStepScored(FirstStep step,
                              Scores scoresEarned) {
    public JSONValue toJson() {
        return JSONObject.of(Map.of("firstStep", step.toJson(),
                                    "scoresEarned", scoresEarned.toJson()));
    }
}
