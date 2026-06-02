package com.kata.tennis.domain.model;

public enum Player {
    A("Player A"),
    B("Player B");

    private final String displayName;

    Player(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
