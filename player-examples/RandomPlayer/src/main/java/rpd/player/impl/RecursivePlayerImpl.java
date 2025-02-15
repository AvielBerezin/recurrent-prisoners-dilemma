package rpd.player.impl;

import rpd.player.Option;
import rpd.player.StatefulPlayer;
import rpd.player.recursive.RecursivePlayerChoice;
import rpd.player.recursive.RecursivePlayerStarter;
import rpd.player.recursive.RecursivePlayerStep;

import java.util.Random;

public class RecursivePlayerImpl implements RecursivePlayerStarter {
    private static final Random random = new Random();

    private Option nextMove() {
        return random.nextBoolean() ? Option.COOPERATE : Option.DEFLECT;
    }

    @Override
    public RecursivePlayerChoice opening() {
        return new RecursivePlayerChoice(nextMove(), new RecursivePlayerStep() {
            @Override
            public RecursivePlayerChoice step(Option previousOpponentAction) {
                return new RecursivePlayerChoice(RecursivePlayerImpl.this.nextMove(), this);
            }
        });
    }
}
