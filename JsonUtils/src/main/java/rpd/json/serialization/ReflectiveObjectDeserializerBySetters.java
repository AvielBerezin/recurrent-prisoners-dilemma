package rpd.json.serialization;

import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;
import rpd.reflective.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class ReflectiveObjectDeserializerBySetters<JavaClass> implements Deserializer<JSONObject, JavaClass> {
    private final Function<Field, Deserializer<JSONValue, Object>> fieldDeserializer;
    private final List<Field> fields;
    private final Map<String, Method> setters;
    private final Constructor<JavaClass> emptyConstructor;

    public ReflectiveObjectDeserializerBySetters(Class<JavaClass> aClass,
                                                 Function<Field, Deserializer<JSONValue, Object>> fieldDeserializer) {
        fields = Utils.getDynamicFieldsExpectDistinctGrantAccess(aClass);
        setters = Utils.getSetters(aClass);
        this.fieldDeserializer = fieldDeserializer;
        try {
            emptyConstructor = aClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("could not find empty constructor", e);
        }
        if (!emptyConstructor.trySetAccessible()) {
            throw new IllegalArgumentException("could not access empty constructor");
        }
    }

    @Override
    public JavaClass deserialize(JSONObject jsonToBeDeserialized) throws JsonDeserializationException {
        JavaClass javaClass;
        try {
            javaClass = emptyConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JsonDeserializationException("could not create reflected class instance via the empty constructor", e);
        }
        for (Field field : fields) {
            JSONValue jsonFieldToBeDeserialized = jsonToBeDeserialized.get(field.getName());
            if (jsonFieldToBeDeserialized == null) {
                throw new JsonDeserializationException("json field is absent: " + field.getName());
            }
            Method setter = setters.get(field.getName());
            try {
                Object deserializedField = fieldDeserializer.apply(field).deserialize(jsonFieldToBeDeserialized);
                setter.invoke(javaClass, deserializedField);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JsonDeserializationException("could not use setter: " + setter, e);
            }
        }
        return javaClass;
    }
}
