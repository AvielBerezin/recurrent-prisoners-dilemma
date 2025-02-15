package rpd.json.serialization;

import rpd.json.values.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class Deserializers {
    public static <JavaClass> Deserializer<JSONObject, JavaClass> reflectiveByConstructor(Class<JavaClass> aClass,
                                                                                          Function<Field, Deserializer<JSONValue, Object>> fieldDeserializer) {
        return new ReflectiveObjectDeserializerByConstructor<>(aClass, fieldDeserializer);
    }

    public static <JavaClass> Deserializer<JSONObject, JavaClass> reflectiveBySetters(Class<JavaClass> aClass,
                                                                                      Function<Field, Deserializer<JSONValue, Object>> fieldDeserializer) {
        return new ReflectiveObjectDeserializerBySetters<>(aClass, fieldDeserializer);
    }

    public static Deserializer<JSONNumber, Number> numberDeserializer() {
        return JSONNumber::number;
    }

    public static Deserializer<JSONBoolean, Boolean> booleanDeserializer() {
        return JSONBoolean::value;
    }

    public static Deserializer<JSONString, String> stringDeserializer() {
        return JSONString::value;
    }

    public static Deserializer<JSONArray, List<Object>> listDeserializer(Deserializer<JSONValue, ?> entryDeserializer) {
        return jsonToBeDeserialized -> {
            List<Object> deserializedElements = new LinkedList<>();
            for (JSONValue jsonValue : jsonToBeDeserialized) {
                deserializedElements.add(entryDeserializer.deserialize(jsonValue));
            }
            return deserializedElements;
        };
    }

    public static Deserializer<JSONObject, Map<String, Object>> mapDeserializer(Function<String, ? extends Deserializer<JSONValue, ?>> entryDeserializer) {
        return jsonToBeDeserialized -> {
            Map<String, Object> deserializeElements = new HashMap<>();
            for (String fieldName : jsonToBeDeserialized.keySet()) {
                deserializeElements.put(fieldName, entryDeserializer.apply(fieldName).deserialize(jsonToBeDeserialized.get(fieldName)));
            }
            return deserializeElements;
        };
    }

    public static <JavaClass>
    Deserializer<JSONValue, JavaClass> generalizeObject(Deserializer<JSONObject, ? extends JavaClass> deserializer) {
        return jsonToBeDeserialized -> deserializer.deserialize(jsonToBeDeserialized.asObject().orElseThrow(() -> new JsonDeserializationException("json expected to be Object but is " + jsonToBeDeserialized.getClass().getSimpleName() + " instead")));
    }

    public static <JavaClass>
    Deserializer<JSONValue, JavaClass> generalizeArray(Deserializer<JSONArray, ? extends JavaClass> deserializer) {
        return jsonToBeDeserialized -> deserializer.deserialize(jsonToBeDeserialized.asArray().orElseThrow(() -> new JsonDeserializationException("json expected to be Array but is " + jsonToBeDeserialized.getClass().getSimpleName() + " instead")));
    }

    public static <JavaClass>
    Deserializer<JSONValue, JavaClass> generalizeNumber(Deserializer<JSONNumber, ? extends JavaClass> deserializer) {
        return jsonToBeDeserialized -> deserializer.deserialize(jsonToBeDeserialized.asNumber().orElseThrow(() -> new JsonDeserializationException("json expected to be Number but is " + jsonToBeDeserialized.getClass().getSimpleName() + " instead")));
    }

    public static <JavaClass>
    Deserializer<JSONValue, JavaClass> generalizeString(Deserializer<JSONString, ? extends JavaClass> deserializer) {
        return jsonToBeDeserialized -> deserializer.deserialize(jsonToBeDeserialized.asString().orElseThrow(() -> new JsonDeserializationException("json expected to be String but is " + jsonToBeDeserialized.getClass().getSimpleName() + " instead")));
    }

    public static <JavaClass>
    Deserializer<JSONValue, JavaClass> generalizeBoolean(Deserializer<JSONBoolean, ? extends JavaClass> deserializer) {
        return jsonToBeDeserialized -> deserializer.deserialize(jsonToBeDeserialized.asBoolean().orElseThrow(() -> new JsonDeserializationException("json expected to be Boolean but is " + jsonToBeDeserialized.getClass().getSimpleName() + " instead")));
    }

    public static <JavaClass>
    Deserializer<JSONValue, JavaClass> generalizeNull(Deserializer<JSONNull, ? extends JavaClass> deserializer) {
        return jsonToBeDeserialized -> deserializer.deserialize(jsonToBeDeserialized.asNull().orElseThrow(() -> new JsonDeserializationException("json expected to be Null but is " + jsonToBeDeserialized.getClass().getSimpleName() + " instead")));
    }

    public static <JavaClass, Json extends JSONValue> Deserializer<Json, JavaClass> compinedDeserializer(Iterable<Deserializer<? super Json, ? extends JavaClass>> deserializers) {
        return jsonToBeDeserialized -> {
            JsonDeserializationException nonOfTheDeserializersWorked = new JsonDeserializationException("non of the deserializers worked");
            for (Deserializer<? super Json, ? extends JavaClass> deserializer : deserializers) {
                try {
                    return deserializer.deserialize(jsonToBeDeserialized);
                } catch (JsonDeserializationException e) {
                    nonOfTheDeserializersWorked.addSuppressed(e);
                }
            }
            throw nonOfTheDeserializersWorked;
        };
    }

    public static <JavaClass> Deserializer<JSONObject, JavaClass> generalDeserializer(Class<JavaClass> aClass) {
        Function<Field, Deserializer<JSONValue, Object>> fieldDeserializer = field -> compinedDeserializer(List.of(
                generalizeObject(generalDeserializer(field.getType())),
                generalDeserializer()
        ));
        return compinedDeserializer(List.of(
                jsonToBeDeserialized -> {
                    Deserializer<JSONObject, JavaClass> deserializer;
                    try {
                        deserializer = reflectiveByConstructor(aClass, fieldDeserializer);
                    } catch (Exception e) {
                        throw new JsonDeserializationException("could not initialize reflective deserializer by constructor of " + aClass, e);
                    }
                    return generalizeObject(deserializer).deserialize(jsonToBeDeserialized);
                },
                jsonToBeDeserialized -> {
                    Deserializer<JSONObject, JavaClass> deserializer;
                    try {
                        deserializer = reflectiveBySetters(aClass, fieldDeserializer);
                    } catch (Exception e) {
                        throw new JsonDeserializationException("could not initialize reflective deserializer by constructor of " + aClass, e);
                    }
                    return generalizeObject(deserializer).deserialize(jsonToBeDeserialized);
                }));
    }

    public static Deserializer<JSONValue, Object> generalDeserializer() {
        return compinedDeserializer(List.of(
                generalizeBoolean(booleanDeserializer()),
                generalizeNumber(numberDeserializer()),
                generalizeString(stringDeserializer()),
                generalizeObject(mapDeserializer(_ -> generalDeserializer())),
                generalizeArray(listDeserializer(jsonToBeDeserialized -> generalDeserializer().deserialize(jsonToBeDeserialized)))
        ));
    }
}
