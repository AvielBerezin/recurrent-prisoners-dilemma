package rpd.game.results;

import rpd.player.Option;

public class Actions extends HPair<Option> {
    public Actions(Option action0, Option action1) {
        super(action0, action1);
    }

    public Experiences intoExperiences() {
        return new Experiences(second(), first());
    }
}
