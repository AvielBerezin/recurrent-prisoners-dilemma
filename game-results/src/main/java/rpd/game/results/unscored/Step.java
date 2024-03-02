package rpd.game.results.unscored;

import rpd.game.results.basic.Actions;
import rpd.game.results.basic.Attempts;
import rpd.game.results.basic.Experiences;
import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;

import java.io.Serializable;
import java.util.Map;

public record Step(Experiences previousExperiences,
                   Attempts attempts,
                   Actions actions)
        implements Serializable {
    public Experiences currentExperiences() {
        return new Experiences(actions.swap());
    }

    public JSONValue toJson() {
        return JSONObject.of(Map.of("previousExperiences", previousExperiences.toJson(),
                                    "attempts", attempts.toJson(),
                                    "actions", actions.toJson()));
    }
}
