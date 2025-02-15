package rpd.player.impl;

import rpd.player.Option;
import rpd.player.StatefulPlayer;

public class PlayerImpl implements StatefulPlayer {
    @Override
    public Option opening() {
        return Option.COOPERATE;
    }

    @Override
    public Option onGameStep(Option previousOpponentAction) {
        return previousOpponentAction;
    }
}
