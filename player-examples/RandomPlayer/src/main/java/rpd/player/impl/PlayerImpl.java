package rpd.player.impl;

import rpd.player.Option;
import rpd.player.Player;

import java.util.Random;

public class PlayerImpl implements Player {
    private static final Random random = new Random();

    private Option nextMove() {
        return random.nextBoolean() ? Option.COLLABORATE : Option.COUNTERACT;
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
