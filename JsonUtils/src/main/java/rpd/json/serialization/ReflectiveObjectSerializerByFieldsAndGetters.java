package rpd.json.serialization;

import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;
import rpd.reflective.Utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ReflectiveObjectSerializerByFieldsAndGetters<JavaClass> implements Serializer<JavaClass, JSONObject> {
    private final Function<Field, Optional<? extends Serializer<Object, ? extends JSONValue>>> fieldSerializer;
    private final Map<String, Utils.ValueFetcher> valueFetchers;

    public ReflectiveObjectSerializerByFieldsAndGetters(Class<? extends JavaClass> classToReflectUpon,
                                                        Function<Field, Optional<? extends Serializer<Object, ? extends JSONValue>>> fieldSerializer) {
        this.fieldSerializer = fieldSerializer;
        valueFetchers = Utils.getValueFetchers(classToReflectUpon);
    }

    @Override
    public JSONObject serialize(JavaClass javaClassToBeSerialized) throws JsonSerializationException {
        return JSONObject.of(serializeFields(javaClassToBeSerialized));
    }

    private HashMap<String, JSONValue> serializeFields(JavaClass javaClassToBeSerialized) throws JsonSerializationException {
        HashMap<String, JSONValue> map = new HashMap<>();
        for (String fieldName : valueFetchers.keySet()) {
            Utils.ValueFetcher valueFetcher = valueFetchers.get(fieldName);
            Optional<? extends Serializer<Object, ? extends JSONValue>> serializer = fieldSerializer.apply(valueFetcher.field());
            Object value;
            try {
                value = valueFetcher.fetch(javaClassToBeSerialized);
            } catch (Exception e) {
                throw new JsonSerializationException("fetching field failed", e);
            }
            if (serializer.isEmpty()) {
                continue;
            }
            map.put(fieldName, serializer.get().serialize(value));
        }
        return map;
    }
}
