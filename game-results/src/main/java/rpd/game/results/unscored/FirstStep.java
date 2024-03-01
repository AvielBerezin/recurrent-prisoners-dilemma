package rpd.game.results.unscored;

import rpd.game.results.basic.Actions;
import rpd.game.results.basic.Attempts;
import rpd.game.results.basic.Experiences;
import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;

import java.io.Serializable;
import java.util.Map;

public record FirstStep(Attempts attempts,
                        Actions actions)
        implements Serializable {
    public Experiences currentExperiences() {
        return actions.intoExperiences();
    }

    public JSONValue toJson() {
        return JSONObject.of(Map.of("attempts", attempts.toJson(),
                                    "actions", actions.toJson()));
    }

}
