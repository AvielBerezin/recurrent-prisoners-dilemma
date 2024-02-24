package rpd.game;

import rpd.game.json.JSONArray;
import rpd.game.json.JSONNumber;
import rpd.game.json.JSONObject;
import rpd.game.json.JSONValue;
import rpd.game.json.writer.JSONWriter;
import rpd.game.results.HPair;
import rpd.game.results.PlayResultsScored;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.exit;
import static java.util.Collections.shuffle;
import static rpd.game.SinglePlay.runPlay;

public class Tournament {
    private static class RoundCompetitors extends HPair<Integer> {
        public RoundCompetitors(int index0, int index1) {
            super(index0, index1);
        }

        public JSONValue toJson() {
            return super.toJson(JSONNumber::new);
        }
    }

    private record RoundResult(RoundCompetitors play,
                               PlayResultsScored playResultsScored) {
        public JSONValue toJson() {
            return JSONObject.of(Map.of("play", play.toJson(),
                                        "playResultsScored", playResultsScored.toJson()));
        }

    }


    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("expected 1 argument <competitors file> but received " + args.length);
            System.err.println(Arrays.toString(args));
            exit(1);
        }
        List<String> competitors = Files.readAllLines(Path.of(args[0]));
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
        int iterations = 30;
        List<RoundResult> results = rounds.stream()
                                          .map(roundCompetitors -> new RoundResult(roundCompetitors,
                                                                                   runPlay(random, iterations,
                                                                                           competitors.get(roundCompetitors.get(0)),
                                                                                           competitors.get(roundCompetitors.get(1)))
                                                                                           .scored()))
                                          .toList();
        JSONObject resultAsJson = JSONObject.of(Map.of("iterations", new JSONNumber(iterations),
                                                       "rounds", JSONArray.of(results.stream()
                                                                                     .map(RoundResult::toJson)
                                                                                     .toList())));
        JSONWriter.compactWrite(resultAsJson)
                  .accept(System.out);
    }
}
