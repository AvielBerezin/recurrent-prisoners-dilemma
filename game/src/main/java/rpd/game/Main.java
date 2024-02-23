package rpd.game;

import rpd.player.Choice;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("expected 3 arguments <iterations> <player1> <player2>");
            System.err.println("but instead received " + args.length);
            System.out.println(Arrays.toString(args));
            System.exit(1);
        }
        int iterations = Integer.parseInt(args[0]);
        if (iterations < 1) {
            return;
        }
        List<Process> player =
                List.of(new ProcessBuilder().command(args[1]).start(),
                        new ProcessBuilder().command(args[2]).start());
        List<Scanner> playerOutput =
                List.of(new Scanner(player.get(0).getInputStream()),
                        new Scanner(player.get(1).getInputStream()));
        List<PrintStream> playerInput =
                List.of(new PrintStream(player.get(0).getOutputStream()),
                        new PrintStream(player.get(1).getOutputStream()));
        List<BlockingDeque<Choice>> choices =
                List.of(new LinkedBlockingDeque<>(),
                        new LinkedBlockingDeque<>());
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 2; i++) {
                int finalI = i;
                executor.execute(() -> {
                    for (int iterIndex = 0; iterIndex < iterations - 1; iterIndex++) {
                        String line = playerOutput.get(finalI).nextLine();
                        System.out.println((finalI + 1) + "> " + line);
                        Choice choice = Choice.valueOf(line);
                        choices.get(finalI).add(choice);
                        playerInput.get(1 - finalI).println(choice);
                        playerInput.get(1 - finalI).flush();
                    }
                    String line = playerOutput.get(finalI).nextLine();
                    System.out.println((finalI + 1) + "> " + line);
                    choices.get(finalI).add(Choice.valueOf(line));
                    playerInput.get(1 - finalI).close();
                });
            }
        }
    }
}
