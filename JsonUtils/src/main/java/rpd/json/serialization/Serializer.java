package rpd.json.serialization;

import rpd.json.values.JSONValue;

public interface Serializer<Type, Json extends JSONValue> {
    Json serialize(Type typeToBeSerialized) throws JsonSerializationException;
}
