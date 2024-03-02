package rpd.game.results.basic;

import rpd.json.values.JSONString;
import rpd.json.values.JSONValue;
import rpd.player.Option;

import java.io.Serializable;

public class Actions extends HPair<Option> implements Serializable {
    public Actions(Option action0, Option action1) {
        super(action0, action1);
    }

    public Actions(HPair<? extends Option> options) {
        super(options);
    }

    public Experiences intoExperiences() {
        return new Experiences(second(), first());
    }

    public JSONValue toJson() {
        return super.toJson(option -> new JSONString(option.name()));
    }
}
