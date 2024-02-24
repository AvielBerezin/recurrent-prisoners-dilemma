package rpd.game.received;

import rpd.player.Option;

public record ReceivedValidChoice(Option choice)
        implements ReceivedChoice {
    @Override
    public void dispatch(ActionDispatcher dispatcher) {
        dispatcher.run(this);
    }

    @Override
    public <T> T dispatch(TransformationDispatcher<T> dispatcher) {
        return dispatcher.apply(this);
    }
}
