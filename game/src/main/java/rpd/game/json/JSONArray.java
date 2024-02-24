package rpd.game.json;

import java.util.*;
import java.util.function.UnaryOperator;

public interface JSONArray extends JSONValue, List<JSONValue> {
    @Override
    default <T> T dispatch(Dispatcher<T> dispatcher) {
        return dispatcher.apply(this);
    }

    static JSONArray of(JSONValue... values) {
        return of(List.of(values));
    }

    static JSONArray of(List<JSONValue> values) {
        return new JSONArray() {
            @Override
            public int size() {return values.size();}

            @Override
            public boolean isEmpty() {return values.isEmpty();}

            @Override
            public boolean contains(Object o) {return values.contains(o);}

            @Override
            public Iterator<JSONValue> iterator() {return values.iterator();}

            @Override
            public Object[] toArray() {return values.toArray();}

            @Override
            public <T> T[] toArray(T[] a) {return values.toArray(a);}

            @Override
            public boolean add(JSONValue jsonValue) {return values.add(jsonValue);}

            @Override
            public boolean remove(Object o) {return values.remove(o);}

            @Override
            public boolean containsAll(Collection<?> c) {return values.containsAll(c);}

            @Override
            public boolean addAll(Collection<? extends JSONValue> c) {return values.addAll(c);}

            @Override
            public boolean addAll(int index, Collection<? extends JSONValue> c) {return values.addAll(index, c);}

            @Override
            public boolean removeAll(Collection<?> c) {return values.removeAll(c);}

            @Override
            public boolean retainAll(Collection<?> c) {return values.retainAll(c);}

            @Override
            public void replaceAll(UnaryOperator<JSONValue> operator) {values.replaceAll(operator);}

            @Override
            public void sort(Comparator<? super JSONValue> c) {values.sort(c);}

            @Override
            public void clear() {values.clear();}

            @Override
            public boolean equals(Object o) {
                return values.equals(o);
            }

            @Override
            public int hashCode() {return values.hashCode();}

            @Override
            public JSONValue get(int index) {return values.get(index);}

            @Override
            public JSONValue set(int index, JSONValue element) {return values.set(index, element);}

            @Override
            public void add(int index, JSONValue element) {values.add(index, element);}

            @Override
            public JSONValue remove(int index) {return values.remove(index);}

            @Override
            public int indexOf(Object o) {return values.indexOf(o);}

            @Override
            public int lastIndexOf(Object o) {return values.lastIndexOf(o);}

            @Override
            public ListIterator<JSONValue> listIterator() {return values.listIterator();}

            @Override
            public ListIterator<JSONValue> listIterator(int index) {return values.listIterator(index);}

            @Override
            public List<JSONValue> subList(int fromIndex, int toIndex) {return values.subList(fromIndex, toIndex);}
        };
    }
}
