package aviel.rpd.rpdjavafxgui;

import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import rpd.game.results.Utils;
import rpd.game.results.basic.Actions;
import rpd.game.results.basic.Attempts;
import rpd.game.results.basic.HPair;
import rpd.game.results.basic.RoundCompetitors;
import rpd.game.results.scored.PlayResultsScored;
import rpd.game.results.scored.TournamentResultScored;
import rpd.player.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GameDisplay extends GridPane {
    private int moveSize;
    private final TournamentResultScored tournamentResult;
    private List<HPair<Rectangle>> rectangleSteps;

    public GameDisplay(TournamentResultScored tournamentResult, int moveSize) {
        this.tournamentResult = tournamentResult;
        this.moveSize = moveSize;
    }

    public void displayCompetitors(RoundCompetitors competitors) {
        List<HPair<Rectangle>> allSteps = new ArrayList<>(tournamentResult.iterations());
        allSteps.add(getFirstStepRectangles(tournamentResult.rounds().get(competitors)));
        getStepRectangles(tournamentResult.rounds().get(competitors)).forEach(allSteps::add);
        rectangleSteps = allSteps;
        for (int col = 0; col < rectangleSteps.size(); col++) {
            HPair<Rectangle> stepRectangles = rectangleSteps.get(col);
            for (int row = 0; row < stepRectangles.size(); row++) {
                add(stepRectangles.get(row), col, row);
            }
        }
    }

    public void setMoveSize(int moveSize) {
        this.moveSize = moveSize;
        for (HPair<Rectangle> rectangles : rectangleSteps) {
            for (Rectangle rectangle : rectangles) {
                rectangle.setWidth(moveSize);
                rectangle.setHeight(moveSize);
            }
        }
    }

    private HPair<Rectangle> getFirstStepRectangles(PlayResultsScored playResultsScored) {
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

    private Stream<HPair<Rectangle>> getStepRectangles(PlayResultsScored playResultsScored) {
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

    private HPair<Rectangle> getStepRectangles(Actions actions, Attempts attempts) {
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

    private static <Val1, Val2> BiFunction<Val1, Val2, Consumer<BiConsumer<Val1, Val2>>> values2IntoContinuation() {
        return (val1, val2) -> ((Consumer<BiConsumer<Val1, Val2>>) consumer -> consumer.accept(val1, val2));
    }
}
