package rpd.json.serialization;

import rpd.json.values.JSONValue;

public interface Deserializer<Json extends JSONValue, Type> {
    Type deserialize(Json jsonToBeDeserialized) throws JsonDeserializationException;
}
