package rpd.game.results.scored;

import rpd.game.results.basic.Scores;
import rpd.game.results.unscored.FirstStep;
import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;

import java.io.Serializable;
import java.util.Map;

public record FirstStepScored(FirstStep step,
                              Scores scoresEarned)
        implements Serializable {
    public JSONValue toJson() {
        return JSONObject.of(Map.of("firstStep", step.toJson(),
                                    "scoresEarned", scoresEarned.toJson()));
    }
}
