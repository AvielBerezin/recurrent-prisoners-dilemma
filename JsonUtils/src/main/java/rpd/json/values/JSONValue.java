package rpd.json.values;

import java.util.Optional;
import java.util.function.Consumer;

public interface JSONValue {
    interface Dispatcher<T> {
        T apply(JSONObject jsonObject);
        T apply(JSONArray jsonArray);
        T apply(JSONNumber jsonNumber);
        T apply(JSONString jsonString);
        T apply(JSONBoolean jsonBoolean);
        T apply(JSONNull jsonNull);
    }

    interface VoidDispatcher {
        void apply(JSONObject jsonObject);
        void apply(JSONArray jsonArray);
        void apply(JSONNumber jsonNumber);
        void apply(JSONString jsonString);
        void apply(JSONBoolean jsonBoolean);
        void apply(JSONNull jsonNull);
    }

    default void ifObject(Consumer<JSONObject> onJsonObject) {
        dispatch(new EmptyVoidDispatcher() {
            @Override
            public void apply(JSONObject jsonObject) {
                onJsonObject.accept(jsonObject);
            }
        });
    }

    default void ifArray(Consumer<JSONArray> onJsonArray) {
        dispatch(new EmptyVoidDispatcher() {
            @Override
            public void apply(JSONArray jsonArray) {
                onJsonArray.accept(jsonArray);
            }
        });
    }

    default void ifNumber(Consumer<JSONNumber> onJsonNumber) {
        dispatch(new EmptyVoidDispatcher() {
            @Override
            public void apply(JSONNumber jsonNumber) {
                onJsonNumber.accept(jsonNumber);
            }
        });
    }

    default void ifString(Consumer<JSONString> onJsonString) {
        dispatch(new EmptyVoidDispatcher() {
            @Override
            public void apply(JSONString jsonString) {
                onJsonString.accept(jsonString);
            }
        });
    }

    default void ifBoolean(Consumer<JSONBoolean> onJsonBoolean) {
        dispatch(new EmptyVoidDispatcher() {
            @Override
            public void apply(JSONBoolean jsonBoolean) {
                onJsonBoolean.accept(jsonBoolean);
            }
        });
    }

    default void ifNull(Consumer<JSONNull> onJsonNull) {
        dispatch(new EmptyVoidDispatcher() {
            @Override
            public void apply(JSONNull jsonNull) {
                onJsonNull.accept(jsonNull);
            }
        });
    }

    default Optional<JSONObject> asObject() {
        return dispatch(new EmptyOptionalDispatcher<>(){
            @Override
            public Optional<JSONObject> apply(JSONObject jsonObject) {
                return Optional.of(jsonObject);
            }
        });
    }

    default Optional<JSONArray> asArray() {
        return dispatch(new EmptyOptionalDispatcher<>() {
            @Override
            public Optional<JSONArray> apply(JSONArray jsonArray) {
                return Optional.of(jsonArray);
            }
        });
    }

    default Optional<JSONNumber> asNumber() {
        return dispatch(new EmptyOptionalDispatcher<>() {
            @Override
            public Optional<JSONNumber> apply(JSONNumber jsonNumber) {
                return Optional.of(jsonNumber);
            }
        });
    }

    default Optional<JSONString> asString() {
        return dispatch(new EmptyOptionalDispatcher<>() {
            @Override
            public Optional<JSONString> apply(JSONString jsonString) {
                return Optional.of(jsonString);
            }
        });
    }

    default Optional<JSONBoolean> asBoolean() {
        return dispatch(new EmptyOptionalDispatcher<>() {
            @Override
            public Optional<JSONBoolean> apply(JSONBoolean jsonBoolean) {
                return Optional.of(jsonBoolean);
            }
        });
    }

    default Optional<JSONNull> asNull() {
        return dispatch(new EmptyOptionalDispatcher<>() {
            @Override
            public Optional<JSONNull> apply(JSONNull jsonNull) {
                return Optional.of(jsonNull);
            }
        });
    }


    <T> T dispatch(Dispatcher<T> dispatcher);

    default void dispatch(VoidDispatcher dispatcher) {
        dispatch(new Dispatcher<Runnable>() {
            @Override
            public Runnable apply(JSONObject jsonObject) {
                return () -> dispatcher.apply(jsonObject);
            }

            @Override
            public Runnable apply(JSONArray jsonArray) {
                return () -> dispatcher.apply(jsonArray);
            }

            @Override
            public Runnable apply(JSONNumber jsonNumber) {
                return () -> dispatcher.apply(jsonNumber);
            }

            @Override
            public Runnable apply(JSONString jsonString) {
                return () -> dispatcher.apply(jsonString);
            }

            @Override
            public Runnable apply(JSONBoolean jsonBoolean) {
                return () -> dispatcher.apply(jsonBoolean);
            }

            @Override
            public Runnable apply(JSONNull jsonNull) {
                return () -> dispatcher.apply(jsonNull);
            }
        }).run();
    }

    class EmptyVoidDispatcher implements VoidDispatcher {
        @Override
        public void apply(JSONObject jsonObject) {
        }

        @Override
        public void apply(JSONArray jsonArray) {
        }

        @Override
        public void apply(JSONNumber jsonNumber) {
        }

        @Override
        public void apply(JSONString jsonString) {
        }

        @Override
        public void apply(JSONBoolean jsonBoolean) {
        }

        @Override
        public void apply(JSONNull jsonNull) {
        }
    }

    class EmptyOptionalDispatcher<Type> implements Dispatcher<Optional<Type>> {
        @Override
        public Optional<Type> apply(JSONObject jsonObject) {
            return Optional.empty();
        }

        @Override
        public Optional<Type> apply(JSONArray jsonArray) {
            return Optional.empty();
        }

        @Override
        public Optional<Type> apply(JSONNumber jsonNumber) {
            return Optional.empty();
        }

        @Override
        public Optional<Type> apply(JSONString jsonString) {
            return Optional.empty();
        }

        @Override
        public Optional<Type> apply(JSONBoolean jsonBoolean) {
            return Optional.empty();
        }

        @Override
        public Optional<Type> apply(JSONNull jsonNull) {
            return Optional.empty();
        }
    }
}
