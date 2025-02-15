package rpd.player.translation;

import rpd.player.Option;
import rpd.player.StatefulPlayer;
import rpd.player.recursive.RecursivePlayerChoice;
import rpd.player.recursive.RecursivePlayerStarter;
import rpd.player.recursive.RecursivePlayerStep;

import java.util.Objects;

public class StatefulOfRecursivePlayer implements StatefulPlayer {
    private final RecursivePlayerStarter recursivePlayer;
    private RecursivePlayerStep playerStep;

    public StatefulOfRecursivePlayer(RecursivePlayerStarter recursivePlayer) {
        Objects.requireNonNull(recursivePlayer, "recursivePlayer");
        this.recursivePlayer = recursivePlayer;
    }

    @Override
    public Option opening() {
        RecursivePlayerChoice choice = recursivePlayer.opening();
        playerStep = choice.nextStep();
        return choice.option();
    }

    @Override
    public Option onGameStep(Option previousOpponentAction) {
        RecursivePlayerChoice choice = playerStep.step(previousOpponentAction);
        playerStep = choice.nextStep();
        return choice.option();
    }
}
