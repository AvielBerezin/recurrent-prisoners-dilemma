package rpd.game.results.scored;

import rpd.game.results.basic.Scores;
import rpd.game.results.unscored.Step;
import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;

import java.io.Serializable;
import java.util.Map;

public record StepScored(Step step,
                         Scores scoresEarned,
                         Scores scoresTotal)
        implements Serializable {
    public JSONValue toJson() {
        return JSONObject.of(Map.of("step", step.toJson(),
                                    "scoresEarned", scoresEarned.toJson(),
                                    "scoresTotal", scoresTotal.toJson()));
    }
}
