package rpd.game.results.basic;

import rpd.json.values.JSONNumber;
import rpd.json.values.JSONValue;

import java.io.Serializable;

public class RoundCompetitors extends HPair<Integer> implements Serializable {
    public RoundCompetitors(int index0, int index1) {
        super(index0, index1);
    }

    public JSONValue toJson() {
        return super.toJson(JSONNumber::new);
    }
}
