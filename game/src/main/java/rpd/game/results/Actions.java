package rpd.game.results;

import rpd.game.json.JSONString;
import rpd.game.json.JSONValue;
import rpd.player.Option;

public class Actions extends HPair<Option> {
    public Actions(Option action0, Option action1) {
        super(action0, action1);
    }

    public Experiences intoExperiences() {
        return new Experiences(second(), first());
    }

    public JSONValue toJson() {
        return super.toJson(option -> new JSONString(option.name()));
    }
}
