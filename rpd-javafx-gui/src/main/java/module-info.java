module aviel.rpd.rpdjavafxgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires rpd.game.results;
                            
    opens aviel.rpd.rpdjavafxgui to javafx.fxml;
    exports aviel.rpd.rpdjavafxgui;
}