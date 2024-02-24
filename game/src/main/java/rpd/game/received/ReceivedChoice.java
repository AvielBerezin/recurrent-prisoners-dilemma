package rpd.game.received;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ReceivedChoice {
    interface ActionDispatcher {
        void run(ReceivedInvalidChoice invalidChoice);
        void run(ReceivedValidChoice choice);
    }

    interface TransformationDispatcher<T> {
        T apply(ReceivedInvalidChoice invalidChoice);
        T apply(ReceivedValidChoice choice);
    }

    void dispatch(ActionDispatcher dispatcher);
    <T> T dispatch(TransformationDispatcher<T> dispatcher);

    default void onInvalid(Consumer<ReceivedInvalidChoice> action) {
        dispatch(new ActionDispatcher() {
            @Override
            public void run(ReceivedInvalidChoice invalidChoice) {
                action.accept(invalidChoice);
            }

            @Override
            public void run(ReceivedValidChoice choice) {}
        });
    }

    default void onValid(Consumer<ReceivedValidChoice> action) {
        dispatch(new ActionDispatcher() {
            @Override
            public void run(ReceivedInvalidChoice invalidChoice) {}

            @Override
            public void run(ReceivedValidChoice choice) {
                action.accept(choice);
            }
        });
    }

    default ReceivedValidChoice intoValid(Function<ReceivedInvalidChoice, ReceivedValidChoice> invalidTransform) {
        return dispatch(new TransformationDispatcher<>() {
            @Override
            public ReceivedValidChoice apply(ReceivedInvalidChoice invalidChoice) {
                return invalidTransform.apply(invalidChoice);
            }

            @Override
            public ReceivedValidChoice apply(ReceivedValidChoice choice) {
                return choice;
            }
        });
    }

    default boolean isValid() {
        return dispatch(new TransformationDispatcher<>() {
            @Override
            public Boolean apply(ReceivedInvalidChoice invalidChoice) {
                return false;
            }

            @Override
            public Boolean apply(ReceivedValidChoice choice) {
                return true;
            }
        });
    }
}
