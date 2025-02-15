package rpd.json.serialization;

import rpd.json.values.JSONObject;
import rpd.json.values.JSONValue;
import rpd.reflective.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

public class ReflectiveObjectDeserializerByConstructor<JavaClass> implements Deserializer<JSONObject, JavaClass> {
    private final Class<JavaClass> classToReflectUpon;
    private final Constructor<?> constructor;
    private final Function<Field, Deserializer<JSONValue, Object>> fieldDeserializer;
    private final List<Field> fields;

    public ReflectiveObjectDeserializerByConstructor(Class<JavaClass> aClass,
                                                     Function<Field, Deserializer<JSONValue, Object>> fieldDeserializer) {
        classToReflectUpon = aClass;
        fields = Utils.getDynamicFieldsExpectDistinctGrantAccess(classToReflectUpon);
        constructor = findSuitingConstructor(aClass, fields);
        try {
            constructor.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalArgumentException("constructor is inaccessible", e);
        }
        this.fieldDeserializer = fieldDeserializer;
    }

    private Constructor<?> findSuitingConstructor(Class<?> aClass, List<Field> fields) {
        constructors:
        for (Constructor<?> declaredConstructor : aClass.getDeclaredConstructors()) {
            if (declaredConstructor.getParameterTypes().length != fields.size()) {
                continue;
            }
            Iterator<Field> fieldIterator = fields.iterator();
            Iterator<Class<?>> parameterTypes = Arrays.asList(declaredConstructor.getParameterTypes()).iterator();
            while (fieldIterator.hasNext()) {
                Field field = fieldIterator.next();
                Class<?> parameterType = parameterTypes.next();
                if (!field.getType().isAssignableFrom(parameterType)) {
                    continue constructors;
                }
            }
            return declaredConstructor;
        }
        throw new IllegalArgumentException("could not find constructor that suiting all fields: " + fields);
    }

    @Override
    public JavaClass deserialize(JSONObject jsonToBeDeserialized) throws JsonDeserializationException {
        List<String> fieldNames = fields.stream().map(Field::getName).toList();
        if (!jsonToBeDeserialized.keySet().containsAll(fieldNames)) {
            LinkedList<String> missingFieldNames = new LinkedList<>(fieldNames);
            missingFieldNames.removeAll(jsonToBeDeserialized.keySet());
            throw new JsonDeserializationException("json is missing some fields: " + missingFieldNames);
        }
        LinkedList<Object> arguments = new LinkedList<>();
        for (Field field : fields) {
            arguments.add(fieldDeserializer.apply(field).deserialize(jsonToBeDeserialized.get(field.getName())));
        }
        try {
            return classToReflectUpon.cast(constructor.newInstance(arguments.toArray()));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JsonDeserializationException("could not construct instance", e);
        }
    }
}
