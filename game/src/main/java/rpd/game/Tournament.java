package rpd.game;

import rpd.game.received.ReceivedChoice;
import rpd.game.received.ReceivedInvalidChoice;
import rpd.game.received.ReceivedValidChoice;
import rpd.player.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class Tournament {
    private static class APlay extends ArrayList<Integer> {
        public APlay(int index0, int index1) {
            super(2);
            this.add(index0);
            this.add(index1);
        }
    }



//    public static void main(String[] args) throws IOException {
//        if (args.length != 1) {
//            System.err.println("expected 1 argument <competitors file> but received " + args.length);
//            System.err.println(Arrays.toString(args));
//            exit(1);
//        }
//        List<String> competitors = Files.readAllLines(Path.of(args[0]));
//        ArrayList<Integer> indices = new ArrayList<>(competitors.size());
//        for (int i = 0; i < competitors.size(); i++) {
//            indices.add(i);
//        }
//        List<APlay> plays = indices.stream()
//                                   .flatMap(index1 -> indices.stream()
//                                                             .filter(index2 -> index2 >= index1)
//                                                             .map(index2 -> new APlay(index1, index2)))
//                                   .collect(Collectors.toCollection(ArrayList::new));
//        Collections.shuffle(plays);
//        ArrayList<ArrayList<Integer>> scores = new ArrayList<>(competitors.size());
//        for (int i = 0; i < competitors.size(); i++) {
//            scores.add(new ArrayList<>(competitors.size()));
//            for (int j = 0; j < scores.get(i).size(); j++) {
//                scores.get(i).add(0);
//            }
//        }
//        List<List<Integer>> scoringMatrix = List.of(List.of(1, 0),
//                                                    List.of(2, 0));
//        plays.forEach(aPlay -> {
//            List<BlockingDeque<ReceivedChoice>> play;
//            int iterations = 30;
//            try {
//                play = SinglePlay.play(iterations, competitors.get(aPlay.get(0)), competitors.get(aPlay.get(1)));
//            } catch (IOException e) {
//                throw new RuntimeException("could not run players " + aPlay.get(0) + " " + aPlay.get(1), e);
//            }
//            try {
//                Option choice0 = getChoiceOrThrow(aPlay.get(0), play.get(0).take());
//                Option choice1 = getChoiceOrThrow(aPlay.get(1), play.get(1).take());
//                scores.get(aPlay.get(0)).set(aPlay.get(1), scores.get(aPlay.get(0)).get(aPlay.get(1)) +
//                                                           scoringMatrix.get(choice0 == Option.COLLABORATE ? 0 : 1)
//                                                                        .get(choice1 == Option.COLLABORATE ? 0 : 1));
//            } catch (InterruptedException exception) {
//                throw new RuntimeException("interrupted on a player move", exception);
//            }
//        });
//    }

    private static Option getChoiceOrThrow(final int player, ReceivedChoice move) {
        return move.dispatch(new ReceivedChoice.TransformationDispatcher<Option>() {
            @Override
            public Option apply(ReceivedInvalidChoice invalidChoice) {
                throw new RuntimeException("player " + player + " make an invalid choice");
            }

            @Override
            public Option apply(ReceivedValidChoice choice) {
                return choice.choice();
            }
        });
    }
}
