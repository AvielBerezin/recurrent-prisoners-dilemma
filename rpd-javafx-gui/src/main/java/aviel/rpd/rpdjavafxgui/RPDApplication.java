package aviel.rpd.rpdjavafxgui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import rpd.game.results.scored.TournamentResultScored;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

public class RPDApplication extends Application {
    public static TournamentResultScored tournamentResult;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 600, 200);
        PlayersSelection playersSelection = new PlayersSelection(tournamentResult);
        int moveSize = 20;
        playersSelection.setPadding(new Insets(moveSize * 0.5));
        GameDisplay gameDisplay = new GameDisplay(tournamentResult, moveSize);
        gameDisplay.setHgap(moveSize * 0.2);
        gameDisplay.setVgap(moveSize * 0.1);
        gameDisplay.setPadding(new Insets(moveSize * 0.5));
        playersSelection.setOnCompetitorsSelected(event -> gameDisplay.displayCompetitors(event.get()));
        playersSelection.setOnSelectionStart(event -> gameDisplay.getChildren().clear());
        root.getChildren().add(new VBox(playersSelection, gameDisplay));
        stage.setTitle("recurrent prisoners dilemma");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //noinspection resource
        try (ObjectInputStream objectInputStream =
                     new ObjectInputStream(new Socket(InetAddress.getLocalHost(), 1000).getInputStream())) {
            Object readObject = objectInputStream.readObject();
            RPDApplication.tournamentResult = (TournamentResultScored) readObject;
        }
        launch();
    }
}