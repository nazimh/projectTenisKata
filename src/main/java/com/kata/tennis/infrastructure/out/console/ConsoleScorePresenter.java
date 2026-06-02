package com.kata.tennis.infrastructure.out.console;

import com.kata.tennis.domain.port.out.ScorePresenter;
import org.springframework.stereotype.Component;

@Component
public class ConsoleScorePresenter implements ScorePresenter {

    @Override
    public void displayScore(String message) {
        System.out.println(message);
    }
}
