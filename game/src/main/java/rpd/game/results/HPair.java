package rpd.game.results;

import rpd.game.json.JSONArray;
import rpd.game.json.JSONValue;

import java.util.ArrayList;
import java.util.function.Function;

public class HPair<Value> extends ArrayList<Value> {
    public HPair(Value value0, Value value1) {
        super(2);
        this.add(value0);
        this.add(value1);
    }

    public static <Value> HPair<Value> of(Value value0, Value value1) {
        return new HPair<>(value0, value1);
    }

    public Value first() {
        return get(0);
    }

    public Value second() {
        return get(1);
    }

    public JSONValue toJson(Function<Value, JSONValue> mapper) {
        return JSONArray.of(mapper.apply(first()),
                            mapper.apply(second()));
    }
}
