package rpd.game.results;

import rpd.game.json.JSONString;
import rpd.game.json.JSONValue;
import rpd.player.Option;

public class Attempts extends HPair<Option> {
    public Attempts(Option choice0, Option option1) {
        super(choice0, option1);
    }

    public JSONValue toJson() {
        return super.toJson(value -> new JSONString(value.name()));
    }
}
