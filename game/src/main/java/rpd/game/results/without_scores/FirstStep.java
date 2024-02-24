package rpd.game.results.without_scores;

import rpd.game.results.Actions;
import rpd.game.results.Attempts;
import rpd.game.results.Experiences;

public record FirstStep(Attempts attempts,
                        Actions actions) {
    public Experiences currentExperiences() {
        return actions.intoExperiences();
    }
}
