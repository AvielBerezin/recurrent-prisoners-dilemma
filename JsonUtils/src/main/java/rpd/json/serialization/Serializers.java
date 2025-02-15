package rpd.json.serialization;

import rpd.json.values.*;
import rpd.reflective.Utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Serializers {
    public static <JavaClass> Serializer<JavaClass, JSONObject> reflectiveByFields(Class<? extends JavaClass> classToReflectUpon,
                                                                                   Function<Field, Optional<? extends Serializer<Object, ? extends JSONValue>>> fieldSerializer) {
        return new ReflectiveObjectSerializerByFieldsAndGetters<>(classToReflectUpon, fieldSerializer);
    }

    public static Serializer<Number, JSONNumber> numberSerializer() {
        return JSONNumber::new;
    }

    public static Serializer<Boolean, JSONBoolean> booleanSerializer() {
        return JSONBoolean::new;
    }

    public static Serializer<String, JSONString> stringSerializer() {
        return JSONString::new;
    }

    public static Serializer<List<?>, JSONArray> listSerializer(Serializer<Object, ? extends JSONValue> entrySerializer) {
        return typeToBeSerialized -> {
            LinkedList<JSONValue> serializedEntries = new LinkedList<>();
            for (Object entryToBeSerialized : typeToBeSerialized) {
                serializedEntries.add(entrySerializer.serialize(entryToBeSerialized));
            }
            return JSONArray.of(serializedEntries);
        };
    }

    public static Serializer<Map<String, ?>, JSONObject> mapSerializer(Function<String, Optional<Serializer<Object, ? extends JSONValue>>> entrySerializer) {
        return typeToBeSerialized -> {
            Map<String, JSONValue> serializedEntries = new HashMap<>();
            for (String key : typeToBeSerialized.keySet()) {
                Optional<Serializer<Object, ? extends JSONValue>> serializer = entrySerializer.apply(key);
                if (serializer.isEmpty()) {
                    continue;
                }
                serializedEntries.put(key, serializer.get().serialize(typeToBeSerialized.get(key)));
            }
            return JSONObject.of(serializedEntries);
        };
    }

    public static <JavaClass, Json extends JSONValue>
    Serializer<Object, Json> generalize(Class<? extends JavaClass> javaClass,
                                        Serializer<? super JavaClass, ? extends Json> serializer) {
        return typeToBeSerialized -> {
            JavaClass casted;
            try {
                casted = javaClass.cast(typeToBeSerialized);
            } catch (Exception e) {
                throw new JsonSerializationException("could not cast to " + javaClass.getName(), e);
            }
            return serializer.serialize(casted);
        };
    }

    @SuppressWarnings("unchecked")
    public static <Json extends JSONValue>
    Serializer<Map<?, ?>, Json> generalizeMap(Serializer<? super Map<String, ?>, ? extends Json> serializer) {
        return typeToBeSerialized -> {
            for (Object key : typeToBeSerialized.keySet()) {
                if (!(key instanceof String)) {
                    throw new JsonSerializationException("map key " + Utils.escape(key.toString()) + " is not a string but a " + key.getClass().getName());
                }
            }
            return serializer.serialize((Map<String, ?>) typeToBeSerialized);
        };
    }

    public static <JavaClass, Json extends JSONValue> Serializer<JavaClass, Json> combinedSerializer(Iterable<Serializer<? super JavaClass, ? extends Json>> serializers) {
        return typeToBeSerialized -> {
            JsonSerializationException nonOfTheSerializersWorked = new JsonSerializationException("non of the serializers worked");
            for (Serializer<? super JavaClass, ? extends Json> serializer : serializers) {
                try {
                    return serializer.serialize(typeToBeSerialized);
                } catch (JsonSerializationException e) {
                    nonOfTheSerializersWorked.addSuppressed(e);
                }
            }
            throw nonOfTheSerializersWorked;
        };
    }

    public static Serializer<Object, JSONValue> generalSerializer() {
        return generalSerializer(UnaryOperator.identity());
    }

    @SuppressWarnings("unchecked")
    public static Serializer<Object, JSONValue> generalSerializer(UnaryOperator<Serializer<Object, JSONValue>> serializationModifier) {
        return combinedSerializer(List.of(
                generalize(Boolean.class, booleanSerializer()),
                generalize(Number.class, numberSerializer()),
                generalize(String.class, stringSerializer()),
                generalize((Class<Map<?, ?>>) (Object) Map.class, generalizeMap(mapSerializer(_ -> Optional.of(serializationModifier.apply(generalSerializer(serializationModifier)))))),
                typeToBeSerialized -> generalize((Class<List<?>>) (Object) List.class, listSerializer(serializationModifier.apply(generalSerializer(serializationModifier)))).serialize(typeToBeSerialized),
                typeToBeSerialized -> {
                    Serializer<Object, JSONObject> jsonObjectSerializer;
                    try {
                        jsonObjectSerializer = reflectiveByFields(typeToBeSerialized.getClass(), _ -> Optional.of(serializationModifier.apply(generalSerializer(serializationModifier))));
                    } catch (Exception e) {
                        throw new JsonSerializationException("could not initialize reflective serializer by fields of " + typeToBeSerialized.getClass(), e);
                    }
                    return jsonObjectSerializer.serialize(typeToBeSerialized);
                }
        ));
    }
}
