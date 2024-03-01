package rpd.game.results.basic;

import rpd.json.values.JSONArray;
import rpd.json.values.JSONNumber;
import rpd.json.values.JSONValue;

import java.io.Serializable;

public class Scores extends HPair<Integer> implements Serializable {
    public Scores(int score0, int score1) {
        super(score0, score1);
    }

    public Scores plus(Scores addition) {
        return new Scores(first() + addition.first(),
                          second() + addition.second());
    }

    public JSONValue toJson() {
        return JSONArray.of(new JSONNumber(first()),
                            new JSONNumber(second()));
    }
}
