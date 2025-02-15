package rpd.player.impl;

import rpd.player.Option;
import rpd.player.StatefulPlayer;
import rpd.player.recursive.RecursivePlayerChoice;
import rpd.player.recursive.RecursivePlayerStarter;
import rpd.player.recursive.RecursivePlayerStep;

public class RecursivePlayerImpl implements RecursivePlayerStarter {
    @Override
    public RecursivePlayerChoice opening() {
        return new RecursivePlayerChoice(Option.COOPERATE, getNextStep());
    }

    private RecursivePlayerStep getNextStep() {
        return new RecursivePlayerStep() {
            @Override
            public RecursivePlayerChoice step(Option previousOpponentAction) {
                return new RecursivePlayerChoice(previousOpponentAction, this);
            }
        };
    }
}
