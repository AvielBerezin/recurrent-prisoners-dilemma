package rpd.reflective;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static List<Class<?>> classHierarchy(Class<?> concreteClass) {
        LinkedList<Class<?>> classes = new LinkedList<>();
        do {
            classes.add(concreteClass);
            concreteClass = concreteClass.getSuperclass();
        } while (concreteClass != Object.class);
        return classes.reversed();
    }

    public static List<Field> getDynamicFieldsExpectDistinct(Class<?> aClass) {
        Set<String> fieldNamesSet = new HashSet<>();
        List<Field> fields = new LinkedList<>();
        List<Class<?>> classes = classHierarchy(aClass);
        for (Class<?> classInHierarchy : classes) {
            for (Field declaredField : classInHierarchy.getDeclaredFields()) {
                if (Modifier.isStatic(declaredField.getModifiers())) {
                    continue;
                }
                if (!fieldNamesSet.add(declaredField.getName())) {
                    throw new IllegalArgumentException("field is declared twice in an inherent class and in a super class: " + declaredField.getName());
                }
                fields.add(declaredField);
            }
        }
        return fields;
    }

    private static String setSetterNameFrom(String fieldName) {
        return "set" + trimCapitalize(fieldName);
    }

    private static String getGetterNameFrom(String fieldName) {
        return "get" + trimCapitalize(fieldName);
    }

    public static Map<String, Method> getSetters(Class<?> aClass) {
        List<String> fieldNames = getDynamicFieldsExpectDistinct(aClass).stream().map(Field::getName).toList();
        Set<String> potentialSetterNames = fieldNames.stream().flatMap(fieldName -> Stream.of(fieldName, setSetterNameFrom(fieldName))).collect(Collectors.toSet());
        List<Class<?>> classes = classHierarchy(aClass);
        List<Map<String, Method>> availableSettersTower =
                classes.stream()
                       .map(classInHierarchy -> Arrays.stream(classInHierarchy.getDeclaredMethods())
                                                      .filter(declaredMethod -> !Modifier.isStatic(declaredMethod.getModifiers()))
                                                      .filter(method -> method.getParameterCount() == 1)
                                                      .filter(method -> !method.getName().equals("equals"))
                                                      .filter(method -> {
                                                          try {
                                                              // final methods in Object to be avoided
                                                              return method != Object.class.getDeclaredMethod("wait", long.class) &&
                                                                     method != Object.class.getDeclaredMethod("wait0", long.class);
                                                          } catch (
                                                                  NoSuchMethodException e) {
                                                              throw new AssertionError("Object should have \"void wait(long)\" and \"void wait0(long)\" methods");
                                                          }
                                                      })
                                                      .filter(method -> potentialSetterNames.contains(method.getName()))
                                                      .filter(AccessibleObject::trySetAccessible)
                                                      .collect(Collectors.toMap(Method::getName, Function.identity())))
                       .toList();

        Map<String, Method> setters = new HashMap<>();
        for (String fieldName : fieldNames) {
            for (Map<String, Method> methodByName : availableSettersTower) {
                Method simpleSetter = methodByName.get(fieldName);
                if (simpleSetter != null) {
                    setters.put(fieldName, simpleSetter);
                }
                Method setSetter = methodByName.get(setSetterNameFrom(fieldName));
                if (setSetter != null) {
                    setters.put(fieldName, setSetter);
                }
            }
        }
        if (!setters.keySet().containsAll(fieldNames)) {
            LinkedList<String> fieldNamesWithNoSetters = new LinkedList<>(fieldNames);
            fieldNamesWithNoSetters.removeAll(setters.keySet());
            throw new IllegalArgumentException("could not find setters for the following fields: " + fieldNamesWithNoSetters);
        }
        return setters;
    }

    public interface ValueFetcher {
        Field field();

        Object fetch(Object object) throws InvocationTargetException, IllegalAccessException;
    }

    public static class Getter implements ValueFetcher {
        private final Field field;
        private final Method method;

        public Getter(Field field, Method method) {
            if (method.getParameterCount() != 0) {
                throw new IllegalArgumentException("method should accept no parameters");
            }
            if (!field.getType().isAssignableFrom(method.getReturnType())) {
                throw new IllegalArgumentException("method field type " + field.getType() + " should be assignable from method return type " + method.getReturnType());
            }
            if (!method.trySetAccessible()) {
                throw new IllegalArgumentException("method is not accessible");
            }
            this.field = field;
            this.method = method;
        }

        @Override
        public Field field() {
            return field;
        }

        @Override
        public Object fetch(Object object) throws InvocationTargetException, IllegalAccessException {
            return method.invoke(object);
        }
    }

    public record DirectField(Field field) implements ValueFetcher {
        public DirectField {
            if (!field.trySetAccessible()) {
                throw new IllegalArgumentException("field is not accessible");
            }
        }

        @Override
        public Object fetch(Object object) throws IllegalAccessException {
            return field.get(object);
        }
    }

    public static Map<String, ValueFetcher> getValueFetchers(Class<?> aClass) {
        List<Field> fields = getDynamicFieldsExpectDistinct(aClass);
        List<String> fieldNames = fields.stream().map(Field::getName).toList();
        List<Field> accessibleFields = fields.stream().filter(AccessibleObject::trySetAccessible).toList();
        Set<Field> inaccessibleFields = fields.stream().filter(field -> !field.trySetAccessible()).collect(Collectors.toSet());
        Set<String> inaccessibleFieldNames = inaccessibleFields.stream().map(Field::getName).collect(Collectors.toSet());
        Map<String, Field> inaccessibleFieldsMap = inaccessibleFields.stream().collect(Collectors.toMap(Field::getName, Function.identity()));
        Set<String> potentialGetterNames = inaccessibleFieldNames.stream().flatMap(fieldName -> Stream.of(fieldName, getGetterNameFrom(fieldName))).collect(Collectors.toSet());
        List<Class<?>> classes = classHierarchy(aClass);
        List<Map<String, Method>> availableGettersTower =
                classes.stream()
                       .map(classInHierarchy -> Arrays.stream(classInHierarchy.getDeclaredMethods())
                                                      .filter(declaredMethod -> !Modifier.isStatic(declaredMethod.getModifiers()))
                                                      .filter(method -> potentialGetterNames.contains(method.getName()))
                                                      .filter(method -> method.getParameterCount() == 0)
                                                      .filter(method -> hasCorrespondingFieldType(inaccessibleFieldsMap, method))
                                                      .filter(method -> {
                                                          try {
                                                              // methods in Object to be avoided
                                                              Set<Method> avoided = Set.of(
                                                                      Object.class.getDeclaredMethod("toString"),
                                                                      Object.class.getDeclaredMethod("hashCode"),
                                                                      Object.class.getDeclaredMethod("clone"),
                                                                      Object.class.getDeclaredMethod("getClass")
                                                              );
                                                              return !avoided.contains(method);
                                                          } catch (NoSuchMethodException e) {
                                                              throw new AssertionError("Object class should have [ \"toString\", \"hashCode\", \"clone\", \"getClass\" ] methods");
                                                          }
                                                      })
                                                      .filter(AccessibleObject::trySetAccessible)
                                                      .collect(Collectors.toMap(Method::getName, Function.identity())))
                       .toList();
        Map<String, ValueFetcher> getters = new HashMap<>();
        Map<String, Method> gettersMethods = new HashMap<>();
        for (Field accessibleField : accessibleFields) {
            getters.put(accessibleField.getName(), new DirectField(accessibleField));
        }
        for (Field inaccessibleField : inaccessibleFields) {
            for (Map<String, Method> methodByName : availableGettersTower) {
                Method simpleGetter = methodByName.get(inaccessibleField.getName());
                if (simpleGetter != null && inaccessibleField.getType().isAssignableFrom(simpleGetter.getReturnType())) {
                    getters.put(inaccessibleField.getName(), new Getter(inaccessibleField, simpleGetter));
                    gettersMethods.put(inaccessibleField.getName(), simpleGetter);
                }
                Method getGetter = methodByName.get(getGetterNameFrom(inaccessibleField.getName()));
                if (getGetter != null && inaccessibleField.getType().isAssignableFrom(getGetter.getReturnType())) {
                    getters.put(inaccessibleField.getName(), new Getter(inaccessibleField, getGetter));
                    gettersMethods.put(inaccessibleField.getName(), getGetter);
                }
            }
        }
        if (!getters.keySet().containsAll(fieldNames)) {
            LinkedList<String> unfetchableFieldNames = new LinkedList<>(inaccessibleFieldNames);
            unfetchableFieldNames.removeAll(getters.keySet());
            throw new IllegalArgumentException("some fields are inaccessible and have no getters: " + unfetchableFieldNames);
        }
        Map<Method, List<String>> collidingMethods = gettersMethods.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue, HashMap::new, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        collidingMethods.values().removeIf(names -> names.size() <= 1);
        if (!collidingMethods.isEmpty()) {
            throw new IllegalArgumentException("final getter methods collide upon fields: " + collidingMethods);
        }
        return getters;
    }

    private static boolean hasCorrespondingFieldType(Map<String, Field> inaccessibleFieldsMap, Method method) {
        String getterName = method.getName();
        if (!getterName.startsWith("get")) {
            Field field = inaccessibleFieldsMap.get(getterName);
            if (field == null) {
                return false;
            }
            return field.getType().isAssignableFrom(method.getReturnType());
        }
        Field fieldNoGet = inaccessibleFieldsMap.get(trimDeCapitalize(getterName.substring(3)));
        Field fieldWithGet = inaccessibleFieldsMap.get(getterName);
        if (fieldWithGet == null && fieldNoGet == null) {
            return false;
        }
        if (fieldWithGet == null) {
            return fieldNoGet.getType().isAssignableFrom(method.getReturnType());
        }
        if (fieldNoGet == null) {
            return fieldWithGet.getType().isAssignableFrom(method.getReturnType());
        }
        return fieldNoGet.getType().isAssignableFrom(method.getReturnType())
               || fieldWithGet.getType().isAssignableFrom(method.getReturnType());
    }

    private static String trimCapitalize(String string) {
        string = string.trim();
        if (string.isEmpty()) {
            return "";
        }
        char c = string.charAt(0);
        if (Character.isAlphabetic(c)) {
            return Character.toUpperCase(c) + string.substring(1);
        }
        return string;
    }

    private static String trimDeCapitalize(String string) {
        string = string.trim();
        if (string.isEmpty()) {
            return "";
        }
        char c = string.charAt(0);
        if (Character.isAlphabetic(c)) {
            return Character.toLowerCase(c) + string.substring(1);
        }
        return string;
    }

    public static Object escape(String string) {
        if (!string.contains("\"") && !string.contains("\n") && !string.contains("\\")) {
            return string;
        }
        return '"' + string.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("\\\\"))
                           .replaceAll(Pattern.quote("\""), Matcher.quoteReplacement("\\\""))
                           .replaceAll(Pattern.quote("\n"), Matcher.quoteReplacement("\\n"))
               + '"';
    }
}
