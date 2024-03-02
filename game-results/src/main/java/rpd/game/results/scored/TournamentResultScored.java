package rpd.game.results.scored;

import rpd.game.results.Utils;
import rpd.game.results.basic.RoundCompetitors;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TournamentResultScored implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private final List<String> competitorsNames;
    private final Map<RoundCompetitors, PlayResultsScored> rounds;
    private final Map<Integer, Integer> totalScores;
    private final int iterations;

    public TournamentResultScored(int iterations, List<String> competitorsNames, List<RoundResultScored> roundsListed) {
        this.iterations = iterations;
        this.competitorsNames = Collections.unmodifiableList(competitorsNames);
        this.rounds = roundsAsMap(roundsListed, competitorsNames.size());
        totalScores = calculateTotalScores(competitorsNames, this.rounds);
    }

    private static Map<RoundCompetitors, PlayResultsScored> roundsAsMap(List<RoundResultScored> roundsListed, int competitorsCount) {
        List<List<PlayResultsScored>> roundsData = new ArrayList<>(competitorsCount);
        for (int i = 0; i < competitorsCount; i++) {
            roundsData.add(new ArrayList<>(competitorsCount - i));
        }
        if (roundsListed.size() != (competitorsCount * (competitorsCount + 1)) / 2) {
            throw new IllegalArgumentException("roundsListed");
        }
        for (RoundResultScored roundResultScored : roundsListed) {
            if (roundsData.get(roundResultScored.competitors().first()).size() !=
                roundResultScored.competitors().second() - roundResultScored.competitors().first()) {
                throw new IllegalArgumentException("roundsListed");
            }
            roundsData.get(roundResultScored.competitors().first())
                      .add(roundResultScored.competitors().second() - roundResultScored.competitors().first(),
                           roundResultScored.results());
        }
        Set<RoundCompetitors> keySet =
                IntStream.range(0, competitorsCount).boxed()
                         .flatMap(player1 -> IntStream.range(player1, competitorsCount)
                                                      .mapToObj(player2 -> new RoundCompetitors(player1, player2)))
                         .collect(Collectors.toUnmodifiableSet());
        return new RoundsMap(competitorsCount, roundsData, keySet);
    }

    private static Map<Integer, Integer> calculateTotalScores(List<String> competitorsNames, Map<RoundCompetitors, PlayResultsScored> rounds) {
        HashMap<Integer, Integer> totalScores = new HashMap<>();
        for (int player = 0; player < competitorsNames.size(); player++) {
            totalScores.put(player, 0);
        }
        rounds.forEach((play, round) -> {
            for (int i = 0; i < play.size(); i++) {
                totalScores.put(play.get(i),
                                totalScores.get(play.get(i)) +
                                round.getFinalScores().get(i));
            }
        });
        return Collections.unmodifiableMap(totalScores);
    }

    public Map<Integer, Integer> getTotalScores() {
        return totalScores;
    }

    public int iterations() {
        return iterations;
    }

    public List<String> competitorsNames() {return competitorsNames;}

    public Map<RoundCompetitors, PlayResultsScored> rounds() {return rounds;}

    private static class RoundsMap implements Map<RoundCompetitors, PlayResultsScored>, Serializable {
        private final int competitorsCount;
        private final List<List<PlayResultsScored>> roundsData;
        private final Set<RoundCompetitors> keySet;

        public RoundsMap(int competitorsCount, List<List<PlayResultsScored>> roundsData, Set<RoundCompetitors> keySet) {
            this.competitorsCount = competitorsCount;
            this.roundsData = roundsData;
            this.keySet = keySet;
        }

        @Override
        public int size() {
            return (competitorsCount * competitorsCount + 1) / 2;
        }

        @Override
        public boolean isEmpty() {
            return competitorsCount == 0;
        }

        @Override
        public boolean containsKey(Object key) {
            if (!(key instanceof RoundCompetitors competitors)) {
                return false;
            }
            return competitors.first() < size() && competitors.second() < size();
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PlayResultsScored get(Object key) {
            if (!(key instanceof RoundCompetitors competitors)) {
                throw new IllegalArgumentException();
            }
            if (!containsKey(key)) {
                throw new IndexOutOfBoundsException();
            }
            return roundsData.get(competitors.first())
                             .get(competitors.second() - competitors.first());
        }

        @Override
        public PlayResultsScored put(RoundCompetitors key, PlayResultsScored value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PlayResultsScored remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends RoundCompetitors, ? extends PlayResultsScored> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<RoundCompetitors> keySet() {
            return keySet;
        }

        @Override
        public Collection<PlayResultsScored> values() {
            return roundsData.stream().flatMap(Collection::stream).toList();
        }

        @Override
        public Set<Entry<RoundCompetitors, PlayResultsScored>> entrySet() {
            return Utils.zip(IntStream.range(0, competitorsCount).boxed()
                                      .flatMap(player1 -> IntStream.range(player1, competitorsCount)
                                                                   .mapToObj(player2 -> new RoundCompetitors(player1, player2))),
                             roundsData.stream().flatMap(Collection::stream),
                             (competitors, result) -> new Entry<RoundCompetitors, PlayResultsScored>() {
                                 @Override
                                 public RoundCompetitors getKey() {
                                     return competitors;
                                 }

                                 @Override
                                 public PlayResultsScored getValue() {
                                     return result;
                                 }

                                 @Override
                                 public PlayResultsScored setValue(PlayResultsScored value) {
                                     throw new UnsupportedOperationException();
                                 }
                             })
                        .collect(Collectors.toUnmodifiableSet());
        }
    }
}
