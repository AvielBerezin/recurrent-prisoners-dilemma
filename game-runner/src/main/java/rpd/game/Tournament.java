package rpd.game;

import rpd.game.results.Utils;
import rpd.game.results.basic.HPair;
import rpd.game.results.basic.RoundCompetitors;
import rpd.game.results.scored.RoundResultScored;
import rpd.game.results.scored.TournamentResultScored;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.System.exit;
import static java.util.Collections.shuffle;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static rpd.game.SinglePlay.runPlay;

public class Tournament {
    public static final int iterations = 10;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("expected 1 argument <competitors file> but received " + args.length);
            System.err.println(Arrays.toString(args));
            exit(1);
        }
        List<String> competitors = Files.readAllLines(Path.of(args[0]));
        TournamentResultScored tournamentResult = new TournamentResultScored(iterations, renameCompetitors(competitors), compete(competitors));
        System.out.println("tournament is ready");
        //noinspection InfiniteLoopStatement
        while (true) {
            //noinspection resource
            try (ObjectOutputStream inputStream =
                         new ObjectOutputStream(new ServerSocket(1000).accept().getOutputStream())) {
                inputStream.writeObject(tournamentResult);
            } catch (Exception ignored) {
            }
        }
    }

    private static List<RoundResultScored> compete(List<String> competitors) {
        ArrayList<Integer> indices = new ArrayList<>(competitors.size());
        for (int i = 0; i < competitors.size(); i++) {
            indices.add(i);
        }
        List<RoundCompetitors> rounds = indices.stream()
                                               .flatMap(index1 -> indices.stream()
                                                                         .filter(index2 -> index2 >= index1)
                                                                         .map(index2 -> new RoundCompetitors(index1, index2)))
                                               .collect(Collectors.toCollection(ArrayList::new));
        Random random = new Random();
        shuffle(rounds, random);
        return rounds.stream()
                     .map(roundCompetitors -> new RoundResultScored(roundCompetitors,
                                                                    runPlay(random, iterations, roundCompetitors.map(competitors::get))
                                                                            .scored()))
                     .sorted(comparing(RoundResultScored::competitors, comparing(HPair::first))
                                     .thenComparing(RoundResultScored::competitors, comparing(HPair::second)))
                     .toList();
    }

    private static List<String> renameCompetitors(List<String> competitors) {
        return Utils.zip(naturals(),
                         competitors.stream()
                                    .map(Path::of)
                                    .map(path -> path.getFileName().toString())
                                    .map(str -> str.replaceAll("\\..*", "")),
                         Indexed::new)
                    .collect(groupingBy(Indexed::value,
                                        collectingAndThen(Collectors.toList(),
                                                          indexes -> Utils.zip(naturals(),
                                                                               indexes.stream()
                                                                                      .sorted(comparingInt(Indexed::index)),
                                                                               (ind, indexed) -> indexed.map(val -> "%s:%d".formatted(val, ind))))))
                    .values()
                    .stream()
                    .flatMap(Function.identity())
                    .sorted(comparing(Indexed::index))
                    .map(Indexed::value)
                    .toList();
    }

    private record Indexed<Val>(int index, Val value) {
        public <MVal> Indexed<MVal> map(Function<Val, MVal> mapper) {
            return new Indexed<>(index, mapper.apply(value));
        }
    }

    private static Stream<Integer> naturals() {
        return IntStream.iterate(0, x -> x + 1).boxed();
    }
}
