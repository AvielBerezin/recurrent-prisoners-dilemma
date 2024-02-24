package rpd.player_std;

import rpd.player.Option;
import rpd.player.impl.PlayerImpl;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Scanner opponentOutput = new Scanner(System.in);
        PlayerImpl player = new PlayerImpl();
        System.out.println(player.opening());
        while (opponentOutput.hasNextLine()) {
            String line = opponentOutput.nextLine();
            Option choice;
            try {
                choice = Option.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.err.println("opponent made an illegal move " +
                                   line.replaceAll(Pattern.quote("\\"),
                                                   Matcher.quoteReplacement("\\\\"))
                                       .replaceAll(Pattern.quote("\n"),
                                                   Matcher.quoteReplacement("\\n"))
                                       .replaceAll(Pattern.quote("\""),
                                                   Matcher.quoteReplacement("\\\"")));
                System.err.println("Choices available: " + Arrays.toString(Option.values()));
                return;
            }
            System.out.println(player.onGameStep(choice));
        }
    }
}
