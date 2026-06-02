package com.kata.tennis.domain.model;

import lombok.Getter;

@Getter
public enum Player {
    A("Player A"),
    B("Player B");

    private final String displayName;

    Player(String displayName) {
        this.displayName = displayName;
    }

}
