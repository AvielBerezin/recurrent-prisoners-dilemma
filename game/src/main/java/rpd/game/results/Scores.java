package rpd.game.results;

public class Scores extends HPair<Integer> {
    public Scores(int score0, int score1) {
        super(score0, score1);
    }

    public Scores plus(Scores addition) {
        return new Scores(first() + addition.first(),
                          second() + addition.second());
    }
}
