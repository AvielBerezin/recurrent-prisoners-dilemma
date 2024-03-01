package rpd.game.results.scored;

import rpd.json.values.JSONArray;
import rpd.json.values.JSONNumber;
import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record TournamentResultScored(int iterations,
                                     List<RoundResultScored> rounds)
        implements Serializable {
    public JSONValue toJson() {
        return JSONObject.of(Map.of("iterations", new JSONNumber(iterations),
                                    "rounds", JSONArray.of(rounds.stream()
                                                                 .map(RoundResultScored::toJson)
                                                                 .toList())));
    }
}
