package rpd.game.results.basic;

import rpd.json.values.JSONString;
import rpd.json.values.JSONValue;
import rpd.player.Option;

import java.io.Serializable;

public class Attempts extends HPair<Option> implements Serializable {
    public Attempts(Option choice0, Option option1) {
        super(choice0, option1);
    }

    public Attempts(HPair<? extends Option> options) {
        super(options);
    }

    public JSONValue toJson() {
        return super.toJson(value -> new JSONString(value.name()));
    }
}
