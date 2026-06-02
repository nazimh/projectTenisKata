package com.kata.tennis.domain.model;

public enum Score {
    LOVE("0"),
    FIFTEEN("15"),
    THIRTY("30"),
    FORTY("40");

    private final String label;

    Score(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
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
