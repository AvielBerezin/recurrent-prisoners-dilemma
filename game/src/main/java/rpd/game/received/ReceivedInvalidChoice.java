package rpd.game.received;

public record ReceivedInvalidChoice(String line)
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
