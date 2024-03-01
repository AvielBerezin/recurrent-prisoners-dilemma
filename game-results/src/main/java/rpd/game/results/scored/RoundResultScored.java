package rpd.game.results.scored;

import rpd.game.results.basic.RoundCompetitors;
import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;

import java.io.Serializable;
import java.util.Map;

public record RoundResultScored(RoundCompetitors play,
                                PlayResultsScored playResultsScored)
        implements Serializable {
    public JSONValue toJson() {
        return JSONObject.of(Map.of("play", play.toJson(),
                                    "playResultsScored", playResultsScored.toJson()));
    }

}
