package rpd.game.results;

import rpd.game.json.JSONString;
import rpd.game.json.JSONValue;
import rpd.player.Option;

public class Experiences extends HPair<Option> {
    public Experiences(Option experience0, Option experience1) {
        super(experience0, experience1);
    }

    public JSONValue toJson() {
        return super.toJson(option -> new JSONString(option.name()));
    }
}
