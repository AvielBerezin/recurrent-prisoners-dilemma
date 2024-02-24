package rpd.game;

import rpd.game.received.ReceivedChoice;
import rpd.game.received.ReceivedInvalidChoice;
import rpd.game.received.ReceivedValidChoice;
import rpd.game.results.*;
import rpd.game.results.without_scores.FirstStep;
import rpd.game.results.without_scores.PlayResults;
import rpd.game.results.without_scores.Step;
import rpd.player.Option;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.exit;
import static java.util.function.Function.identity;
import static rpd.game.Utils.zip;

public class SinglePlay {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("expected 3 arguments <iterations> <player1> <player2>");
            System.err.println("but instead received " + args.length);
            System.err.println(Arrays.toString(args));
            exit(1);
        }
        int iterations = Integer.parseInt(args[0]);
        if (iterations < 1) {
            System.err.println("number of iterations is expected to be strictly positive but provided " + iterations);
            exit(1);
            return;
        }
        PlayResultsScored playResultsScored = runPlay(new Random(), iterations, args[1], args[2]).scored();
        System.out.println(playResultsToString(iterations, playResultsScored));
    }

    private static String playResultsToString(int iterations, PlayResultsScored playResultsScored) {
        List<String> lines0 = rightPad(getLines(iterations, playResultsScored, 0), 3);
        List<String> lines1 = getLines(iterations, playResultsScored, 1);
        return zip(lines0.stream(), lines1.stream(), String::concat)
                .collect(Collectors.joining("\n"));
    }

    private static List<String> getLines(int iterations, PlayResultsScored playResultsScored, int player) {
        List<String> attempted = new ArrayList<>(iterations);
        attempted.add("attempted " + playResultsScored.firstStep().step().attempts().get(player));
        playResultsScored.steps()
                         .stream()
                         .map(StepScored::step)
                         .map(Step::attempts)
                         .map(p -> p.get(player))
                         .map(option -> "attempted " + option)
                         .forEach(attempted::add);
        attempted = rightPad(alignLeft(attempted), 2);

        List<String> happened = new ArrayList<>(iterations);
        happened.add("happened " + playResultsScored.firstStep().step().actions().get(player));
        playResultsScored.steps()
                         .stream()
                         .map(StepScored::step)
                         .map(Step::actions)
                         .map(p -> p.get(player))
                         .map(option -> "happened " + option)
                         .forEach(happened::add);
        happened = rightPad(alignLeft(happened), 2);

        List<String> experienced = new ArrayList<>(iterations);
        experienced.add("experienced " + playResultsScored.firstStep().step().currentExperiences().get(player));
        playResultsScored.steps()
                         .stream()
                         .map(StepScored::step)
                         .map(Step::currentExperiences)
                         .map(p -> p.get(player))
                         .map(option -> "experienced " + option)
                         .forEach(experienced::add);
        experienced = alignLeft(experienced);

        List<String> earned = new ArrayList<>(iterations);
        earned.add("earned " + playResultsScored.firstStep().scoresEarned().get(player));
        playResultsScored.steps()
                         .stream()
                         .map(StepScored::scoresEarned)
                         .map(p -> p.get(player))
                         .map(score -> "earned " + score)
                         .forEach(earned::add);
        earned = rightPad(alignLeft(earned), 2);

        List<String> total = new ArrayList<>(iterations);
        total.add("total " + playResultsScored.firstStep().scoresEarned().get(player));
        playResultsScored.steps()
                         .stream()
                         .map(StepScored::scoresTotal)
                         .map(p -> p.get(player))
                         .map(score -> "total " + score)
                         .forEach(total::add);
        total = alignLeft(total);

        List<String> firstLines = alignLeft(zip(zip(attempted.stream(), happened.stream(), String::concat),
                                                experienced.stream(), String::concat)
                                                    .toList());
        List<String> secondLines = alignLeft(zip(earned.stream(), total.stream(), String::concat)
                                                     .toList());
        List<String> lines = new ArrayList<>(iterations + 1);
        lines.add("player " + player);
        zip(firstLines.stream(), secondLines.stream(), Stream::of)
                .flatMap(identity())
                .forEach(lines::add);
        return alignCenter(lines);
    }

    private static List<String> alignLeft(List<String> column) {
        int size = column.stream()
                         .mapToInt(String::length)
                         .max()
                         .orElse(0);
        Stream<String> stringStream = column.stream()
                                            .map(str -> str + " ".repeat(size - str.length()));
        return stringStream
                .toList();
    }

    private static List<String> alignCenter(List<String> column) {
        int size = column.stream()
                         .mapToInt(String::length)
                         .max()
                         .orElse(0);
        Stream<String> stringStream = column.stream()
                                            .map(str -> " ".repeat((size - str.length()) / 2) +
                                                        str +
                                                        " ".repeat((size - str.length() + 1) / 2));
        return stringStream
                .toList();
    }

    private static List<String> rightPad(List<String> column, int pad) {
        return column.stream()
                     .map(str -> str + " ".repeat(pad))
                     .toList();
    }

    public static PlayResults runPlay(Random random, int iterations, String command0, String command1) {
        Process process1;
        try {
            process1 = new ProcessBuilder().command(command0).start();
        } catch (IOException e) {
            throw new RuntimeException("failed on process 0", e);
        }
        Process process2;
        try {
            process2 = new ProcessBuilder().command(command1).start();
        } catch (IOException e) {
            throw new RuntimeException("failed on process 1", e);
        }
        HPair<Process> playerProcesses =
                HPair.of(process1,
                         process2);
        HPair<Scanner> playerOutput =
                HPair.of(new Scanner(playerProcesses.get(0).getInputStream()),
                         new Scanner(playerProcesses.get(1).getInputStream()));
        HPair<PrintStream> playerInput =
                HPair.of(new PrintStream(playerProcesses.get(0).getOutputStream()),
                         new PrintStream(playerProcesses.get(1).getOutputStream()));
        HPair<List<Option>> attempts =
                HPair.of(new ArrayList<>(iterations),
                         new ArrayList<>(iterations));
        HPair<List<Option>> actions =
                HPair.of(new ArrayList<>(iterations),
                         new ArrayList<>(iterations));
        HPair<List<Boolean>> forceBadActions =
                HPair.of(random.doubles(iterations).mapToObj(chance -> chance < 0.1).toList(),
                         random.doubles(iterations).mapToObj(chance -> chance < 0.1).toList());
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            for (int player_ = 0; player_ < 2; player_++) {
                final int player = player_;
                executor.execute(() -> {
                    for (int iterIndex = 0; iterIndex < iterations - 1; iterIndex++) {
                        Option choice = getReceivedChoice(playerOutput.get(player).nextLine())
                                .intoValid(invalidChoice -> throwForInvalidChoice(player, invalidChoice))
                                .choice();
                        Option action = forceBadActions.get(1 - player).get(iterIndex)
                                        ? Option.COUNTERACT
                                        : choice;
                        actions.get(player).add(action);
                        playerInput.get(1 - player).println(action);
                        playerInput.get(1 - player).flush();
                        attempts.get(player).add(choice);
                    }
                    {
                        Option choice = getReceivedChoice(playerOutput.get(player).nextLine())
                                .intoValid(invalidChoice -> throwForInvalidChoice(player, invalidChoice))
                                .choice();
                        actions.get(player).add(forceBadActions.get(1 - player).get(iterations - 1)
                                                ? Option.COUNTERACT
                                                : choice);
                        playerInput.get(player).close();
                        playerInput.get(1 - player).close();
                        attempts.get(player).add(choice);
                    }
                });
            }
        }
        List<Step> steps = new ArrayList<>(iterations - 1);
        FirstStep firstStep = new FirstStep(new Attempts(attempts.first().get(0),
                                                         attempts.second().get(0)),
                                            new Actions(actions.first().get(0),
                                                        actions.second().get(0)));
        if (iterations == 1) {
            return new PlayResults(firstStep);
        }
        steps.add(new Step(firstStep.currentExperiences(),
                           new Attempts(attempts.first().get(1),
                                        attempts.second().get(1)),
                           new Actions(actions.first().get(1),
                                       actions.second().get(1))));
        for (int i = 1; i < iterations - 1; i++) {
            steps.add(new Step(steps.get(i - 1).currentExperiences(),
                               new Attempts(attempts.first().get(i),
                                            attempts.second().get(i)),
                               new Actions(actions.first().get(i),
                                           actions.second().get(i))));
        }
        return new PlayResults(firstStep, steps);
    }

    @SuppressWarnings("UnusedReturnValue")
    private static <T> T throwForInvalidChoice(int player, ReceivedInvalidChoice invalidChoice) {
        throw new RuntimeException(
                "invalid choice made by player " + player +
                " " +
                invalidChoice.line()
                             .replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("\\\\"))
                             .replaceAll(Pattern.quote("\n"), Matcher.quoteReplacement("\\n"))
                             .replaceAll(Pattern.quote("\""), Matcher.quoteReplacement("\\\"")));
    }

    private static ReceivedChoice getReceivedChoice(String line) {
        try {
            return new ReceivedValidChoice(Option.valueOf(line));
        } catch (IllegalArgumentException e) {
            return new ReceivedInvalidChoice(line);
        }
    }
}
