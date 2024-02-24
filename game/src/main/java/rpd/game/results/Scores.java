package rpd.game.results;

import rpd.game.json.JSONArray;
import rpd.game.json.JSONNumber;
import rpd.game.json.JSONValue;

public class Scores extends HPair<Integer> {
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
