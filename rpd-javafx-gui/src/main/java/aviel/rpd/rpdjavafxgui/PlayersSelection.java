package aviel.rpd.rpdjavafxgui;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import rpd.game.results.basic.HPair;
import rpd.game.results.basic.RoundCompetitors;
import rpd.game.results.scored.RoundResultScored;
import rpd.game.results.scored.TournamentResultScored;

import java.util.*;
import java.util.stream.Collectors;

public class PlayersSelection extends HBox {
    public static class CompetitorsSelectionEvent extends Event {
        public static final EventType<Event> COMPETITORS_SELECTION = new EventType<>("competitors selection");
        private final RoundCompetitors competitors;

        private CompetitorsSelectionEvent(RoundCompetitors competitors) {
            super(COMPETITORS_SELECTION);
            this.competitors = competitors;
        }

        public RoundCompetitors get() {
            return competitors;
        }
    }

    private final ObservableSet<Integer> selected;

    private final List<ToggleButton> typedChildren;

    private EventHandler<? super Event> onSelectionStart;
    private EventHandler<? super CompetitorsSelectionEvent> onCompetitorsSelected;

    public PlayersSelection(TournamentResultScored tournamentResult) {
        typedChildren = getTypedChildren(tournamentResult);
        getChildren().addAll(typedChildren);
        selected = FXCollections.observableSet();
        for (int i = 0; i < typedChildren.size(); i++) {
            typedChildren.get(i).selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (onSelectionStart != null) {
                    onSelectionStart.handle(new Event(EventType.ROOT));
                }
            });
            int player = i;
            typedChildren.get(i).selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    selected.add(player);
                } else {
                    selected.remove(player);
                }
            });
            selected.addListener((SetChangeListener<Integer>) change -> {
                if (change.wasAdded()) {
                    typedChildren.get(change.getElementAdded()).setSelected(true);
                } else if (change.wasRemoved()) {
                    typedChildren.get(change.getElementRemoved()).setSelected(false);
                }
            });

        }
        Bindings.createBooleanBinding(() -> selected.size() > 2, selected)
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        selected.clear();
                    }
                });
        Bindings.createBooleanBinding(() -> selected.size() == 2, selected)
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        ArrayList<Integer> selectedValues = new ArrayList<>(selected);
                        selectedValues.sort(Comparator.naturalOrder());
                        RoundCompetitors competitors = new RoundCompetitors(selectedValues.get(0), selectedValues.get(1));
                        if (onCompetitorsSelected != null) {
                            onCompetitorsSelected.handle(new CompetitorsSelectionEvent(competitors));
                        }
                    }
                });
    }

    private List<ToggleButton> getTypedChildren(TournamentResultScored tournamentResult) {
        return tournamentResult.competitorsNames()
                               .stream()
                               .map(ToggleButton::new)
                               .toList();
    }

    private List<Label> getScoreLabels(TournamentResultScored tournamentResult) {
        return tournamentResult.competitorsNames()
                               .stream()
                               .map(Label::new)
                               .toList();
    }

    public void setOnCompetitorsSelected(EventHandler<? super CompetitorsSelectionEvent> onCompetitorsSelected) {
        this.onCompetitorsSelected = onCompetitorsSelected;
    }

    public void setOnSelectionStart(EventHandler<? super Event> onSelectionStart) {
        this.onSelectionStart = onSelectionStart;
    }
}
