package rpd.player;

public interface StatefulPlayer {
    Option opening();
    Option onGameStep(Option previousOpponentAction);
}
