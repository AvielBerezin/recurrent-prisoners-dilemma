package rpd.player;

public interface Player {
    Option opening();
    Option onGameStep(Option previousOpponentAction);
}
