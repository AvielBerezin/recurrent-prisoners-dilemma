module rpd.game.results {
    requires transitive rpd.player.interfaces;
    requires transitive rpd.JsonUtils;

    exports rpd.game.results.scored;
    exports rpd.game.results.basic;
    exports rpd.game.results.unscored;
    exports rpd.game.results;
}