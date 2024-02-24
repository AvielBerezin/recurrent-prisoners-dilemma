package rpd.game.results.without_scores;

import rpd.game.json.JSONObject;
import rpd.game.json.JSONValue;
import rpd.game.results.Actions;
import rpd.game.results.Attempts;
import rpd.game.results.Experiences;

import java.util.Map;

public record Step(Experiences previousExperiences,
                   Attempts attempts,
                   Actions actions) {
    public Experiences currentExperiences() {
        return new Experiences(actions.second(), actions.first());
    }

    public JSONValue toJson() {
        return JSONObject.of(Map.of("previousExperiences", previousExperiences.toJson(),
                                    "attempts", attempts.toJson(),
                                    "actions", actions.toJson()));
    }
}
