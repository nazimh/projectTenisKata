package com.kata.tennis.domain.model;

import lombok.Getter;

@Getter
public enum Score {
    LOVE("0"),
    FIFTEEN("15"),
    THIRTY("30"),
    FORTY("40");

    private final String label;

    Score(String label) {
        this.label = label;
    }

    public Score next() {
        return switch (this) {
            case LOVE -> FIFTEEN;
            case FIFTEEN -> THIRTY;
            case THIRTY -> FORTY;
            case FORTY -> FORTY;
        };
    }
}
