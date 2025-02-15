package rpd.reflective;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    public static List<Field> getDynamicFieldsExpectDistinctGrantAccess(Class<?> aClass) {
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
                if (!declaredField.trySetAccessible()) {
                    throw new IllegalArgumentException("field could not be set accessible: " + declaredField);
                }
                fields.add(declaredField);
            }
        }
        return fields;
    }

    private static String setSetterNameFrom(String fieldName) {
        return "set" + trimCapitalize(fieldName);
    }

    public static Map<String, Method> getSetters(Class<?> aClass) {
        List<String> fieldNames = getDynamicFieldsExpectDistinctGrantAccess(aClass).stream().map(Field::getName).toList();
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
