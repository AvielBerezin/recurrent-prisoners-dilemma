module rpd.game.results {
    requires transitive rpd.player.interfaces;
    requires rpd.JsonUtils;

    exports rpd.game.results.scored;
    exports rpd.game.results.basic;
    exports rpd.game.results.unscored;
}