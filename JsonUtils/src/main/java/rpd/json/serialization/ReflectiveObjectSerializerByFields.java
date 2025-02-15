package rpd.json.serialization;

import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;
import rpd.reflective.Utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ReflectiveObjectSerializerByFields<JavaClass> implements Serializer<JavaClass, JSONObject> {
    private final Function<Field, Optional<? extends Serializer<Object, ? extends JSONValue>>> fieldSerializer;
    private final List<Field> fields;

    public ReflectiveObjectSerializerByFields(Class<? extends JavaClass> classToReflectUpon,
                                              Function<Field, Optional<? extends Serializer<Object, ? extends JSONValue>>> fieldSerializer) {
        this.fieldSerializer = fieldSerializer;
        fields = Utils.getDynamicFieldsExpectDistinctGrantAccess(classToReflectUpon);
    }

    @Override
    public JSONObject serialize(JavaClass javaClassToBeSerialized) throws JsonSerializationException {
        return JSONObject.of(serializeFields(javaClassToBeSerialized));
    }

    private HashMap<String, JSONValue> serializeFields(JavaClass javaClassToBeSerialized) throws JsonSerializationException {
        HashMap<String, JSONValue> map = new HashMap<>();
        for (Field field : fields) {
            Optional<? extends Serializer<Object, ? extends JSONValue>> serializer = fieldSerializer.apply(field);
            Object value;
            try {
                value = field.get(javaClassToBeSerialized);
            } catch (Exception e) {
                throw new JsonSerializationException("field is inaccessible", e);
            }
            if (serializer.isEmpty()) {
                continue;
            }
            map.put(field.getName(), serializer.get().serialize(value));
        }
        return map;
    }
}
