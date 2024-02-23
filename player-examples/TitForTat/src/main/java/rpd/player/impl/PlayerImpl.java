package rpd.player.impl;

import rpd.player.Choice;
import rpd.player.Player;

public class PlayerImpl implements Player {
    @Override
    public Choice opening() {
        return Choice.COLLABORATE;
    }

    @Override
    public Choice onGameStep(Choice previousOponentChoice) {
        return previousOponentChoice;
    }
}
