package aviel.rpd.rpdjavafxgui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import rpd.game.results.basic.HPair;
import rpd.game.results.basic.RoundCompetitors;
import rpd.game.results.scored.RoundResultScored;
import rpd.game.results.scored.StepScored;
import rpd.game.results.scored.TournamentResultScored;
import rpd.player.Option;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class RPDApplication extends Application {
    public static final int moveSize = 20;
    static TournamentResultScored tournamentResult;
    List<ToggleButton> nodes = null;
    Consumer<RoundCompetitors> onSelectedCompetitors = null;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600);
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
        HBox gameDisplay = new HBox();
        onSelectedCompetitors = competitors -> {
            gameDisplay.getChildren().clear();
            tournamentResult.rounds()
                            .stream()
                            .filter(roundResultScored -> roundResultScored.play().equals(competitors))
                            .map(RoundResultScored::playResultsScored)
                            .map(playResultsScored -> {
                                HPair<Rectangle> firstStepAsList = playResultsScored.firstStep()
                                                                                    .step()
                                                                                    .attempts()
                                                                                    .map(Option.COLLABORATE::equals)
                                                                                    .map(isGood -> isGood ? new Color(76f / 255f, 230f / 255f, 71f / 255f, 1f)
                                                                                                          : new Color(227f / 255f, 56f / 255f, 14f / 255f, 1f))
                                                                                    .map(color -> new Rectangle(moveSize, moveSize, color));
                                List<HPair<Rectangle>> allSteps = new ArrayList<>(playResultsScored.steps().size() + 1);
                                allSteps.add(firstStepAsList);
                                allSteps.addAll(playResultsScored.steps()
                                                                 .stream()
                                                                 .map(StepScored::step)
                                                                 .map(step -> {
                                                                     HPair<Boolean> actions = step.actions()
                                                                                                  .map(Option.COLLABORATE::equals);
                                                                     HPair<Boolean> attempts = step.attempts()
                                                                                                   .map(Option.COLLABORATE::equals);
                                                                     Color color1 = actions.first()
                                                                                    ? new Color(0.29, 0.90, 0.27, 1f)
                                                                                    : attempts.first()
                                                                                      ? new Color(0.85, 0.72, 0.04, 1f)
                                                                                      : new Color(0.89, 0.21, 0.05, 1f);
                                                                     Color color2 = actions.second()
                                                                                    ? new Color(0.29, 0.90, 0.27, 1f)
                                                                                    : attempts.second()
                                                                                      ? new Color(0.85, 0.72, 0.04, 1f)
                                                                                      : new Color(0.89, 0.21, 0.05, 1f);
                                                                     return new HPair<>(color1, color2).map(color -> new Rectangle(moveSize, moveSize, color));
                                                                 })
                                                                 .toList());
                                return allSteps;
                            })
                            .map(steps -> new HBox(steps.stream()
                                                        .map(rectangles -> {
                                                            Rectangle first = rectangles.first();
                                                            Rectangle second = rectangles.second();
                                                            VBox.setMargin(first, new Insets(moveSize * 0.1));
                                                            VBox.setMargin(second, new Insets(moveSize * 0.1));
                                                            return new VBox(first, second);
                                                        })
                                                        .peek(vBox -> HBox.setMargin(vBox, new Insets(moveSize * 0.2)))
                                                        .toArray(Node[]::new)))
                            .findFirst()
                            .ifPresentOrElse(gameDisplay.getChildren()::add,
                                             () -> {
                                                 throw new RuntimeException("could not find competitors " + competitors + " in tournament results");
                                             });
        };
        root.getChildren().add(new VBox(playersSelection, gameDisplay));
        stage.setTitle("recurrent prisoners dilemma");
        stage.setScene(scene);
        stage.show();
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