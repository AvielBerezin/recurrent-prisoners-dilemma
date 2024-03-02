package rpd.game.results.basic;

import rpd.json.values.JSONArray;
import rpd.json.values.JSONValue;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HPair<Value> extends ArrayList<Value> {
    public HPair(Value value0, Value value1) {
        super(2);
        this.add(value0);
        this.add(value1);
    }

    public HPair(HPair<? extends Value> values) {
        this(values.first(), values.second());
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

    public HPair<Value> swap() {
        return new HPair<>(second(), first());
    }

    public <MValue> HPair<MValue> map(Function<Value, MValue> mapper) {
        return of(mapper.apply(first()), mapper.apply(second()));
    }

    public static <A, B, C> HPair<C> zip(HPair<A> aPair, HPair<B> bPair, BiFunction<A, B, C> mapper) {
        return new HPair<>(mapper.apply(aPair.first(), bPair.first()),
                           mapper.apply(aPair.second(), bPair.second()));
    }
    
    public JSONValue toJson(Function<Value, JSONValue> mapper) {
        return JSONArray.of(mapper.apply(first()),
                            mapper.apply(second()));
    }
}
