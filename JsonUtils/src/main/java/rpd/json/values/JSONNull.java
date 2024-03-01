package rpd.json.values;

public class JSONNull implements JSONValue {
    @Override
    public <T> T dispatch(Dispatcher<T> dispatcher) {
        return dispatcher.apply(this);
    }
}
