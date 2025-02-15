package rpd.player.translation;

import rpd.player.Option;
import rpd.player.StatefulPlayer;
import rpd.player.recursive.RecursivePlayerChoice;
import rpd.player.recursive.RecursivePlayerStarter;
import rpd.player.recursive.RecursivePlayerStep;

public class RecursiveOfStatefulPlayer implements RecursivePlayerStarter {
    private final StatefulPlayer statefulPlayer;

    public RecursiveOfStatefulPlayer(StatefulPlayer statefulPlayer) {
        this.statefulPlayer = statefulPlayer;
    }

    @Override
    public RecursivePlayerChoice opening() {
        Option opening = statefulPlayer.opening();
        return new RecursivePlayerChoice(opening, new RecursivePlayerStep() {
            @Override
            public RecursivePlayerChoice step(Option previousOpponentAction) {
                Option option = statefulPlayer.onGameStep(previousOpponentAction);
                return new RecursivePlayerChoice(option, this);
            }
        });
    }
}
