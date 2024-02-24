package rpd.game.received;

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
}
