package rpd.player.impl;

import rpd.player.Option;
import rpd.player.Player;

public class PlayerImpl implements Player {
    @Override
    public Option opening() {
        return Option.COLLABORATE;
    }

    @Override
    public Option onGameStep(Option previousOponentAction) {
        return previousOponentAction;
    }
}
