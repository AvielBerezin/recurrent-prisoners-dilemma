package rpd.game.results.scored;

import rpd.game.results.basic.RoundCompetitors;
import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;

import java.io.Serializable;
import java.util.Map;

public record RoundResultScored(RoundCompetitors competitors,
                                PlayResultsScored results)
        implements Serializable {
    public JSONValue toJson() {
        return JSONObject.of(Map.of("competitors", competitors.toJson(),
                                    "results", results.toJson()));
    }
}
