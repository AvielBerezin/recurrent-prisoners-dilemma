package rpd.json.values;

public record JSONBoolean(boolean value) implements JSONValue {
    @Override
    public <T> T dispatch(Dispatcher<T> dispatcher) {
        return dispatcher.apply(this);
    }
}
