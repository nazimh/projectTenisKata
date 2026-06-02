package com.kata.tennis.domain.model;

import java.util.Optional;

public record GameState(
        Score scoreA,
        Score scoreB,
        boolean isDeuce,
        Player advantage,
        Player winner
) {
    public static GameState initial() {
        return new GameState(Score.LOVE, Score.LOVE, false, null, null);
    }

    public boolean isOver() {
        return winner != null;
    }

    public Optional<Player> getAdvantage() {
        return Optional.ofNullable(advantage);
    }

    public Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }
}
