package rpd.player;

public interface Player {
    Choice opening();
    Choice onGameStep(Choice previousOponentChoice);
}
