package rpd.json.values;

public record JSONNumber(Number number) implements JSONValue {
    @Override
    public <T> T dispatch(Dispatcher<T> dispatcher) {
        return dispatcher.apply(this);
    }
}
