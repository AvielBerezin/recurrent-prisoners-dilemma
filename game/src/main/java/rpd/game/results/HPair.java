package rpd.game.results;

import java.util.ArrayList;

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
}
