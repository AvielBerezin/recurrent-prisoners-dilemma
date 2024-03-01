package rpd.game.results.scored;

import java.io.Serializable;
import java.util.List;

public record TournamentResultScored(int iterations,
                                     List<RoundResultScored> rounds)
        implements Serializable {}
