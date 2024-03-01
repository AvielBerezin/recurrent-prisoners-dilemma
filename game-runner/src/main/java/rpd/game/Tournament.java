package rpd.game;

import rpd.game.results.basic.RoundCompetitors;
import rpd.game.results.scored.RoundResultScored;
import rpd.game.results.scored.TournamentResultScored;
import rpd.json.values.writer.JSONWriter;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.System.exit;
import static java.util.Collections.shuffle;
import static rpd.game.SinglePlay.runPlay;

public class Tournament {
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
        int iterations = 10;
        List<RoundResultScored> results = rounds.stream()
                                                .map(roundCompetitors -> new RoundResultScored(roundCompetitors,
                                                                                               runPlay(random, iterations,
                                                                                                       competitors.get(roundCompetitors.first()),
                                                                                                       competitors.get(roundCompetitors.second()))
                                                                                                       .scored()))
                                                .toList();
        TournamentResultScored tournamentResult = new TournamentResultScored(iterations, results);
        JSONWriter.compactWrite(tournamentResult.toJson()).accept(System.out);
        //noinspection InfiniteLoopStatement
        while (true) {
            //noinspection resource
            try (ObjectOutputStream inputStream = new ObjectOutputStream(
                    new Socket(InetAddress.getLocalHost(), 1000)
                            .getOutputStream())) {
                inputStream.writeObject(tournamentResult);
            } catch (Exception exception) {
                // TODO: integrate with a logging framework
//                System.err.println("an exception occurred while streaming tournament results to display server");
//                exception.printStackTrace();
            }
        }
    }
}
