package rpd.json.values;

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

    static JSONArray of(List<? extends JSONValue> values) {
        List<JSONValue> valuesCopy = new ArrayList<>(values);
        return new JSONArray() {
            @Override
            public int size() {return valuesCopy.size();}

            @Override
            public boolean isEmpty() {return valuesCopy.isEmpty();}

            @Override
            public boolean contains(Object o) {return valuesCopy.contains(o);}

            @Override
            public Iterator<JSONValue> iterator() {return valuesCopy.iterator();}

            @Override
            public Object[] toArray() {return valuesCopy.toArray();}

            @Override
            public <T> T[] toArray(T[] a) {return valuesCopy.toArray(a);}

            @Override
            public boolean add(JSONValue jsonValue) {return valuesCopy.add(jsonValue);}

            @Override
            public boolean remove(Object o) {return valuesCopy.remove(o);}

            @Override
            public boolean containsAll(Collection<?> c) {return valuesCopy.containsAll(c);}

            @Override
            public boolean addAll(Collection<? extends JSONValue> c) {return valuesCopy.addAll(c);}

            @Override
            public boolean addAll(int index, Collection<? extends JSONValue> c) {return valuesCopy.addAll(index, c);}

            @Override
            public boolean removeAll(Collection<?> c) {return valuesCopy.removeAll(c);}

            @Override
            public boolean retainAll(Collection<?> c) {return valuesCopy.retainAll(c);}

            @Override
            public void replaceAll(UnaryOperator<JSONValue> operator) {valuesCopy.replaceAll(operator);}

            @Override
            public void sort(Comparator<? super JSONValue> c) {valuesCopy.sort(c);}

            @Override
            public void clear() {valuesCopy.clear();}

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof JSONArray jsonArray)) {
                    return false;
                }
                return valuesCopy.equals(jsonArray);
            }

            @Override
            public int hashCode() {return valuesCopy.hashCode();}

            @Override
            public JSONValue get(int index) {return valuesCopy.get(index);}

            @Override
            public JSONValue set(int index, JSONValue element) {return valuesCopy.set(index, element);}

            @Override
            public void add(int index, JSONValue element) {valuesCopy.add(index, element);}

            @Override
            public JSONValue remove(int index) {return valuesCopy.remove(index);}

            @Override
            public int indexOf(Object o) {return valuesCopy.indexOf(o);}

            @Override
            public int lastIndexOf(Object o) {return valuesCopy.lastIndexOf(o);}

            @Override
            public ListIterator<JSONValue> listIterator() {return valuesCopy.listIterator();}

            @Override
            public ListIterator<JSONValue> listIterator(int index) {return valuesCopy.listIterator(index);}

            @Override
            public List<JSONValue> subList(int fromIndex, int toIndex) {return valuesCopy.subList(fromIndex, toIndex);}
        };
    }
}
