package rpd.json.values;

public interface JSONValue {
    interface Dispatcher<T> {
        T apply(JSONObject jsonObject);
        T apply(JSONArray jsonArray);
        T apply(JSONNumber jsonNumber);
        T apply(JSONString jsonString);
        T apply(JSONBoolean jsonBoolean);
        T apply(JSONNull jsonNull);
    }

    <T> T dispatch(Dispatcher<T> dispatcher);
}
