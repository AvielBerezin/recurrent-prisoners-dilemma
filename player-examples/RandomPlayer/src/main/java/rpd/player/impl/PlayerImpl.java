package rpd.player.impl;

import rpd.player.Option;
import rpd.player.StatefulPlayer;

import java.util.Random;

public class PlayerImpl implements StatefulPlayer {
    private static final Random random = new Random();

    private Option nextMove() {
        return random.nextBoolean() ? Option.COOPERATE : Option.DEFLECT;
    }

    @Override
    public Option opening() {
        return nextMove();
    }

    @Override
    public Option onGameStep(Option previousOpponentAction) {
        return nextMove();
    }
}
