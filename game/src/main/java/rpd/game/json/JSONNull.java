package rpd.game.json;

public class JSONNull implements JSONValue {
    @Override
    public <T> T dispatch(Dispatcher<T> dispatcher) {
        return dispatcher.apply(this);
    }
}
