package rpd.player.recursive;

import rpd.player.Option;

public interface RecursivePlayerStep {
    RecursivePlayerChoice step(Option previousOpponentAction);
}
