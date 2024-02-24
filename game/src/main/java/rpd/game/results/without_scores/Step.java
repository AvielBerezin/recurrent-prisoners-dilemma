package rpd.game.results.without_scores;

import rpd.game.results.Actions;
import rpd.game.results.Experiences;
import rpd.game.results.Attempts;

public record Step(Experiences previousExperiences,
                   Attempts attempts,
                   Actions actions) {
    public Experiences currentExperiences() {
        return new Experiences(actions.second(), actions.first());
    }
}
