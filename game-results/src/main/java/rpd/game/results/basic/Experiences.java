package rpd.game.results.basic;

import rpd.json.values.JSONString;
import rpd.json.values.JSONValue;
import rpd.player.Option;

import java.io.Serializable;

public class Experiences extends HPair<Option> implements Serializable {
    public Experiences(Option experience0, Option experience1) {
        super(experience0, experience1);
    }

    public Experiences(HPair<? extends Option> options) {
        super(options);
    }

    public JSONValue toJson() {
        return super.toJson(option -> new JSONString(option.name()));
    }
}
