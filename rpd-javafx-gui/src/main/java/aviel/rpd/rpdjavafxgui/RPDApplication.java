package aviel.rpd.rpdjavafxgui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import rpd.game.results.Utils;
import rpd.game.results.basic.Actions;
import rpd.game.results.basic.Attempts;
import rpd.game.results.basic.HPair;
import rpd.game.results.basic.RoundCompetitors;
import rpd.game.results.scored.PlayResultsScored;
import rpd.game.results.scored.RoundResultScored;
import rpd.game.results.scored.TournamentResultScored;
import rpd.player.Option;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class RPDApplication extends Application {
    public static final int moveSize = 20;
    static TournamentResultScored tournamentResult;
    List<ToggleButton> nodes = null;
    Runnable onAnySelectionChange = null;
    Consumer<RoundCompetitors> onSelectedCompetitors = null;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 600, 200);
        Set<Integer> players = tournamentResult.rounds()
                                               .stream()
                                               .map(RoundResultScored::play)
                                               .map(HPair::first)
                                               .collect(Collectors.toSet());
        Set<Integer> selected = Collections.synchronizedSet(new HashSet<>());
        nodes = Collections.synchronizedList(players.stream()
                                                    .sorted()
                                                    .map(playerId -> {
                                                        ToggleButton toggleButton = new ToggleButton("player %d".formatted(playerId));
                                                        toggleButton.setOnAction(actionEvent -> {
                                                            onAnySelectionChange.run();
                                                            if (toggleButton.isSelected()) {
                                                                selected.add(playerId);
                                                            } else {
                                                                selected.remove(playerId);
                                                            }
                                                            if (selected.size() > 2) {
                                                                for (Integer id : selected) {
                                                                    if (!playerId.equals(id)) {
                                                                        nodes.get(id).setSelected(false);
                                                                    }
                                                                }
                                                                selected.removeIf(not(playerId::equals));
                                                            }
                                                            if (selected.size() == 2) {
                                                                ArrayList<Integer> competitors = new ArrayList<>(selected);
                                                                competitors.sort(Comparator.naturalOrder());
                                                                onSelectedCompetitors.accept(new RoundCompetitors(competitors.get(0), competitors.get(1)));
                                                            }
                                                        });
                                                        return toggleButton;
                                                    })
                                                    .toList());
        HBox playersSelection = new HBox(nodes.toArray(Node[]::new));
        playersSelection.setPadding(new Insets(moveSize * 0.5));
        GridPane gameDisplay = new GridPane();
        gameDisplay.setHgap(moveSize * 0.2);
        gameDisplay.setVgap(moveSize * 0.1);
        gameDisplay.setPadding(new Insets(moveSize * 0.5));
        onAnySelectionChange = gameDisplay.getChildren()::clear;
        onSelectedCompetitors = competitors -> {
            List<List<HPair<Rectangle>>> rectangleStepsCompetitions =
                    tournamentResult.rounds()
                                    .stream()
                                    .filter(roundResultScored -> roundResultScored.play().equals(competitors))
                                    .map(RoundResultScored::playResultsScored)
                                    .map(playResultsScored -> {
                                        List<HPair<Rectangle>> allSteps = new ArrayList<>(playResultsScored.steps().size() + 1);
                                        allSteps.add(getFirstStepRectangles(playResultsScored));
                                        getStepRectangles(playResultsScored).forEach(allSteps::add);
                                        return allSteps;
                                    })
                                    .toList();
            if (rectangleStepsCompetitions.size() != 1) {
                throw new RuntimeException("invalid tournament results players %s have competed not exactly once".formatted(competitors));
            }
            List<HPair<Rectangle>> rectangleSteps = rectangleStepsCompetitions.get(0);
            for (int col = 0; col < rectangleSteps.size(); col++) {
                HPair<Rectangle> stepRectangles = rectangleSteps.get(col);
                for (int row = 0; row < stepRectangles.size(); row++) {
                    gameDisplay.add(stepRectangles.get(row), col, row);
                }
            }
        };
        root.getChildren().add(new VBox(playersSelection, gameDisplay));
        stage.setTitle("recurrent prisoners dilemma");
        stage.setScene(scene);
        stage.show();
    }

    private static <Val1, Val2> BiFunction<Val1, Val2, Consumer<BiConsumer<Val1, Val2>>> values2IntoContinuation() {
        return (val1, val2) -> ((Consumer<BiConsumer<Val1, Val2>>) consumer -> consumer.accept(val1, val2));
    }

    private static HPair<Rectangle> getFirstStepRectangles(PlayResultsScored playResultsScored) {
        HPair<Rectangle> firstStepRectangles = getStepRectangles(playResultsScored.firstStep().step().actions(),
                                                                 playResultsScored.firstStep().step().attempts());
        HPair<Tooltip> firstStepTooltips = playResultsScored.firstStep()
                                                            .scoresEarned()
                                                            .map("earned %d"::formatted)
                                                            .map(Tooltip::new);
        Utils.zip(firstStepRectangles.stream(), firstStepTooltips.stream(), values2IntoContinuation())
             .forEach(rectangleAndTooltip -> rectangleAndTooltip.accept(Tooltip::install));
        return firstStepRectangles;
    }

    private static Stream<HPair<Rectangle>> getStepRectangles(PlayResultsScored playResultsScored) {
        return playResultsScored.steps()
                                .stream()
                                .map(stepScored -> {
                                    HPair<Rectangle> rectangles = getStepRectangles(stepScored.step().actions(),
                                                                                    stepScored.step().attempts());
                                    HPair<Tooltip> tooltips = HPair.zip(stepScored.scoresEarned(), stepScored.scoresTotal(),
                                                                        "earned %d\ntotal %d"::formatted)
                                                                   .map(Tooltip::new);
                                    Utils.zip(rectangles.stream(), tooltips.stream(), values2IntoContinuation())
                                         .forEach(rectangleAndTooltip -> rectangleAndTooltip.accept(Tooltip::install));
                                    return rectangles;
                                });
    }

    private static HPair<Rectangle> getStepRectangles(Actions actions, Attempts attempts) {
        HPair<Boolean> actionsGoodness = actions.map(Option.COLLABORATE::equals);
        HPair<Boolean> attemptsGoodness = attempts.map(Option.COLLABORATE::equals);
        return HPair.zip(actionsGoodness, attemptsGoodness,
                         (action, attempt) -> action
                                              ? new Color(0.29, 0.90, 0.27, 1f)
                                              : attempt
                                                ? new Color(0.85, 0.72, 0.04, 1f)
                                                : new Color(0.89, 0.21, 0.05, 1f))
                    .map(color -> new Rectangle(moveSize, moveSize, color));
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //noinspection resource
        try (ObjectInputStream objectInputStream =
                     new ObjectInputStream(new ServerSocket(1000).accept().getInputStream())) {
            Object readObject = objectInputStream.readObject();
            RPDApplication.tournamentResult = (TournamentResultScored) readObject;
        }
        launch();
    }
}