package rpd.game.results.without_scores;

import rpd.game.json.JSONObject;
import rpd.game.json.JSONValue;
import rpd.game.results.Actions;
import rpd.game.results.Attempts;
import rpd.game.results.Experiences;

import java.util.Map;

public record FirstStep(Attempts attempts,
                        Actions actions) {
    public Experiences currentExperiences() {
        return actions.intoExperiences();
    }

    public JSONValue toJson() {
        return JSONObject.of(Map.of("attempts", attempts.toJson(),
                                    "actions", actions.toJson()));
    }

}
