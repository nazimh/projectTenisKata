package com.kata.tennis.application.service;

import com.kata.tennis.domain.model.GameState;
import com.kata.tennis.domain.model.Player;
import com.kata.tennis.domain.model.Score;
import com.kata.tennis.domain.port.in.PlayGameUseCase;
import com.kata.tennis.domain.port.out.ScorePresenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TennisGameApplicationService implements PlayGameUseCase {

    private final ScorePresenter scorePresenter;

    public TennisGameApplicationService(ScorePresenter scorePresenter) {
        this.scorePresenter = scorePresenter;
    }

    @Override
    public void play(String balls) {
        log.info("Starting game with ball sequence: {}", balls);
        String normalizedBalls = balls == null ? null : balls.toUpperCase();
        validateInput(normalizedBalls);

        GameState state = GameState.initial();
        int ballIndex = 0;

        for (char ball : normalizedBalls.toCharArray()) {
            ballIndex++;
            Player scorer = parsePlayer(ball);
            log.debug("Ball #{} scored by Player {}", ballIndex, ball);
            state = computeNextState(scorer, state);
            String score = formatScore(state);
            scorePresenter.displayScore(score);
            log.debug("Score after ball #{}: {}", ballIndex, score);

            if (state.isOver()) {
                log.info("Game over after {} balls. Winner: {}", ballIndex, state.winner().getDisplayName());
                break;
            }
        }
    }

    private GameState computeNextState(Player scorer, GameState current) {
        if (current.isDeuce()) {
            return new GameState(Score.FORTY, Score.FORTY, false, scorer, null);
        }

        if (current.getAdvantage().isPresent()) {
            return current.advantage() == scorer
                    ? new GameState(Score.FORTY, Score.FORTY, false, null, scorer)
                    : new GameState(Score.FORTY, Score.FORTY, true, null, null);
        }

        return resolveNormalPlay(scorer, current);
    }

    private GameState resolveNormalPlay(Player scorer, GameState current) {
        Score currentScorerScore = scorer == Player.A ? current.scoreA() : current.scoreB();
        Score currentOpponentScore = scorer == Player.A ? current.scoreB() : current.scoreA();

        if (currentScorerScore == Score.FORTY && currentOpponentScore == Score.FORTY) {
            return new GameState(Score.FORTY, Score.FORTY, true, null, null);
        }

        if (currentScorerScore == Score.FORTY) {
            return new GameState(current.scoreA(), current.scoreB(), false, null, scorer);
        }

        Score newScoreA = scorer == Player.A ? current.scoreA().next() : current.scoreA();
        Score newScoreB = scorer == Player.B ? current.scoreB().next() : current.scoreB();
        boolean deuce = newScoreA == Score.FORTY && newScoreB == Score.FORTY;

        return new GameState(newScoreA, newScoreB, deuce, null, null);
    }

    private void validateInput(String balls) {
        if (balls == null || balls.isBlank()) {
            log.error("Invalid input: balls string is null or blank");
            throw new IllegalArgumentException("Input balls string must not be null or blank");
        }
        for (char c : balls.toCharArray()) {
            if (c != 'A' && c != 'B') {
                log.error("Invalid character '{}' found in input: only 'A' or 'B' are allowed", c);
                throw new IllegalArgumentException("Invalid character '%c': only 'A' or 'B' are allowed".formatted(c));
            }
        }
    }

    private Player parsePlayer(char ball) {
        return ball == 'A' ? Player.A : Player.B;
    }

    private String formatScore(GameState state) {
        if (state.getWinner().isPresent()) {
            return "%s wins the game".formatted(state.winner().getDisplayName());
        }
        if (state.isDeuce()) {
            return "Deuce";
        }
        if (state.getAdvantage().isPresent()) {
            return "Advantage %s".formatted(state.advantage().getDisplayName());
        }
        return "Player A : %s / Player B : %s".formatted(
                state.scoreA().getLabel(),
                state.scoreB().getLabel()
        );
    }
}
