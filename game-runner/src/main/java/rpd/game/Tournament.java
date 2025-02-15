package rpd.game;

import rpd.game.results.Utils;
import rpd.game.results.basic.HPair;
import rpd.game.results.basic.RoundCompetitors;
import rpd.game.results.scored.RoundResultScored;
import rpd.game.results.scored.TournamentResultScored;
import rpd.json.serialization.Serializers;
import rpd.json.values.writer.JSONWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
        try (ServerSocket serverSocket = new ServerSocket(1000)) {
            //noinspection InfiniteLoopStatement
            while (true) {
                try (Socket socket = serverSocket.accept();
                     OutputStream inputStream = socket.getOutputStream()) {
                    JSONWriter.compactWrite(Serializers.generalSerializer().serialize(tournamentResult))
                              .accept(inputStream);
                } catch (Exception exception) {
                    System.err.println(prettyExceptionDisplay(exception));
                }
            }
        }
    }

    private static String prettyExceptionDisplay(Exception exception) {
        StringBuilder stringBuilder = new StringBuilder();
        prettyExceptionDisplay(exception, 0, stringBuilder);
        return stringBuilder.toString();
    }

    private static void prettyExceptionDisplay(Throwable exception, int indent, StringBuilder stringBuilder) {
        boolean anythingPrinted = false;
        { // first section
            if (!Set.of(
                    Exception.class,
                    RuntimeException.class,
                    Error.class,
                    AssertionError.class,
                    IOException.class
            ).contains(exception.getClass())) {
                stringBuilder.append('(')
                             .append(exception.getClass().getSimpleName())
                             .append(')')
                             .append(' ');
                anythingPrinted = true;
            }
            if (exception.getMessage() != null) {
                multilineStringAppendIndented(indent + 1, exception.getMessage(), stringBuilder);
                anythingPrinted = true;
            }
        }
        if (exception.getCause() != null) {
            if (anythingPrinted) {
                stringBuilder.append(System.lineSeparator());
                appendSpaces(stringBuilder, indent);
            }
            stringBuilder.append("because: ");
            prettyExceptionDisplay(exception.getCause(), indent + 3, stringBuilder);
            anythingPrinted = true;
        }
        if (exception.getSuppressed().length > 0) {
            int padding = String.valueOf(exception.getSuppressed().length + 1).length() + 2;
            if (anythingPrinted) {
                stringBuilder.append(System.lineSeparator());
                appendSpaces(stringBuilder, indent);
            }
            stringBuilder.append("for ")
                         .append(exception.getSuppressed().length)
                         .append(" reasons:");
            for (int i = 0; i < exception.getSuppressed().length; i++) {
                Throwable reason = exception.getSuppressed()[i];
                stringBuilder.append(System.lineSeparator());
                appendSpaces(stringBuilder, indent);
                int number = i + 1;
                stringBuilder.append(number);
                appendSpaces(stringBuilder, padding - String.valueOf(number).length());
                prettyExceptionDisplay(reason, indent + padding, stringBuilder);
            }
            anythingPrinted = true;
        }
        if (!anythingPrinted) {
            stringBuilder.append(exception.getClass().getSimpleName());
        }
    }

    private static void multilineStringAppendIndented(int indent, String message, StringBuilder stringBuilder) {
        Scanner scanner = new Scanner(message);
        if (!scanner.hasNextLine()) return;
        {
            String line = scanner.nextLine();
            stringBuilder.append(line);
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            stringBuilder.append(System.lineSeparator());
            appendSpaces(stringBuilder, indent);
            stringBuilder.append(line);
        }
    }

    private static void appendSpaces(StringBuilder stringBuilder, int indent) {
        for (int i = 0; i < indent; i++) {
            stringBuilder.append(' ');
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
