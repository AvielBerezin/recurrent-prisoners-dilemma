package rpd.game.json;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface JSONObject extends Map<String, JSONValue>, JSONValue {
    @Override
    default <T> T dispatch(Dispatcher<T> dispatcher) {
        return dispatcher.apply(this);
    }

    static JSONObject of(Map<String, JSONValue> values) {
        return new JSONObject() {
            @Override
            public int size() {return values.size();}

            @Override
            public boolean isEmpty() {return values.isEmpty();}

            @Override
            public boolean containsKey(Object key) {return values.containsKey(key);}

            @Override
            public boolean containsValue(Object value) {return values.containsValue(value);}

            @Override
            public JSONValue get(Object key) {return values.get(key);}

            @Override
            public JSONValue put(String key, JSONValue value) {return values.put(key, value);}

            @Override
            public JSONValue remove(Object key) {return values.remove(key);}

            @Override
            public void putAll(Map<? extends String, ? extends JSONValue> m) {values.putAll(m);}

            @Override
            public void clear() {values.clear();}

            @Override
            public Set<String> keySet() {return values.keySet();}

            @Override
            public Collection<JSONValue> values() {return values.values();}

            @Override
            public Set<Entry<String, JSONValue>> entrySet() {return values.entrySet();}
        };
    }
}
