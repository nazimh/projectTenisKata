package com.kata.tennis.application.service;

import com.kata.tennis.domain.port.out.ScorePresenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TennisGameApplicationServiceTest {

    private TennisGameApplicationService service;
    private List<String> output;

    @BeforeEach
    void setUp() {
        output = new ArrayList<>();
        ScorePresenter presenter = output::add;
        service = new TennisGameApplicationService(presenter);
    }

    @Nested
    @DisplayName("Normal scoring")
    class NormalScoring {

        @Test
        @DisplayName("Player A scores first ball → 15 / 0")
        void playerAScoresFirst_shouldHaveFifteen() {
            service.play("A");
            assertThat(output).containsExactly("Player A : 15 / Player B : 0");
        }

        @Test
        @DisplayName("Player B scores first ball → 0 / 15")
        void playerBScoresFirst_shouldHaveFifteen() {
            service.play("B");
            assertThat(output).containsExactly("Player A : 0 / Player B : 15");
        }

        @Test
        @DisplayName("Player A scores twice → 30 / 0")
        void playerAScoresTwice_shouldHaveThirty() {
            service.play("AA");
            assertThat(output).last().isEqualTo("Player A : 30 / Player B : 0");
        }

        @Test
        @DisplayName("Player A scores three times → 40 / 0")
        void playerAScoresThreeTimes_shouldHaveForty() {
            service.play("AAA");
            assertThat(output).last().isEqualTo("Player A : 40 / Player B : 0");
        }

        @Test
        @DisplayName("Player A wins after 40 / 30 → Player A wins the game")
        void playerAWins_afterFortyThirty() {
            service.play("AAABA");
            assertThat(output).last().isEqualTo("Player A wins the game");
        }

        @Test
        @DisplayName("Player B wins after 30 / 40 → Player B wins the game")
        void playerBWins_afterThirtyForty() {
            service.play("BBBAB");
            assertThat(output).last().isEqualTo("Player B wins the game");
        }

        @Test
        @DisplayName("Example ABABAA → Player A wins with exact score sequence")
        void exampleSequence_ABABAA() {
            service.play("ABABAA");
            assertThat(output).containsExactly(
                    "Player A : 15 / Player B : 0",
                    "Player A : 15 / Player B : 15",
                    "Player A : 30 / Player B : 15",
                    "Player A : 30 / Player B : 30",
                    "Player A : 40 / Player B : 30",
                    "Player A wins the game"
            );
        }
    }

    @Nested
    @DisplayName("Deuce rules")
    class DeuceRules {

        @Test
        @DisplayName("Both reach 40 → Deuce is displayed")
        void bothAtForty_shouldDisplayDeuce() {
            service.play("AAABBB");
            assertThat(output).contains("Deuce");
        }

        @Test
        @DisplayName("Deuce → Player A scores → Advantage Player A")
        void afterDeuce_playerAScores_shouldShowAdvantageA() {
            service.play("AAABBBA");
            assertThat(output).last().isEqualTo("Advantage Player A");
        }

        @Test
        @DisplayName("Deuce → Player B scores → Advantage Player B")
        void afterDeuce_playerBScores_shouldShowAdvantageB() {
            service.play("AAABBBB");
            assertThat(output).last().isEqualTo("Advantage Player B");
        }

        @Test
        @DisplayName("Advantage A → Player A scores again → Player A wins")
        void advantageA_thenAScores_shouldWin() {
            service.play("AAABBBAA");
            assertThat(output).contains("Advantage Player A");
            assertThat(output).last().isEqualTo("Player A wins the game");
        }

        @Test
        @DisplayName("Advantage A → Player B scores → back to Deuce")
        void advantageA_thenBScores_shouldReturnToDeuce() {
            service.play("AAABBBAB");
            assertThat(output).last().isEqualTo("Deuce");
        }
    }

    @Nested
    @DisplayName("Input validation")
    class InputValidation {

        @Test
        @DisplayName("Null input throws IllegalArgumentException")
        void nullInput_shouldThrow() {
            assertThatThrownBy(() -> service.play(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("Blank input throws IllegalArgumentException")
        void blankInput_shouldThrow() {
            assertThatThrownBy(() -> service.play("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("Invalid character throws IllegalArgumentException")
        void invalidCharacter_shouldThrow() {
            assertThatThrownBy(() -> service.play("AXBA"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("X");
        }

        @Test
        @DisplayName("Lowercase 'a' and 'b' are accepted (case-insensitive)")
        void lowercase_shouldBeAccepted() {
            service.play("ababaa");
            assertThat(output).last().isEqualTo("Player A wins the game");
        }

        @Test
        @DisplayName("Mixed case input is accepted (case-insensitive)")
        void mixedCase_shouldBeAccepted() {
            service.play("AbAbAA");
            assertThat(output).last().isEqualTo("Player A wins the game");
        }
    }

    @Nested
    @DisplayName("Score state transitions — computeNextState")
    class ScoreStateTransitions {

        @Test
        @DisplayName("From LOVE/LOVE → Player A scores → FIFTEEN/LOVE")
        void fromLove_playerAScores_shouldMoveTo15() {
            service.play("A");
            assertThat(output).containsExactly("Player A : 15 / Player B : 0");
        }

        @Test
        @DisplayName("From FIFTEEN/LOVE → Player A scores → THIRTY/LOVE")
        void fromFifteen_playerAScores_shouldMoveTo30() {
            service.play("AA");
            assertThat(output).last().isEqualTo("Player A : 30 / Player B : 0");
        }

        @Test
        @DisplayName("From THIRTY/LOVE → Player A scores → FORTY/LOVE")
        void fromThirty_playerAScores_shouldMoveTo40() {
            service.play("AAA");
            assertThat(output).last().isEqualTo("Player A : 40 / Player B : 0");
        }

        @Test
        @DisplayName("From FORTY/LOVE → Player A scores → Player A wins (no deuce)")
        void fromFortyLove_playerAScores_shouldWinDirectly() {
            service.play("AAAA");
            assertThat(output).last().isEqualTo("Player A wins the game");
        }

        @Test
        @DisplayName("From FORTY/THIRTY → Player A scores → Player A wins (no deuce)")
        void fromFortyThirty_playerAScores_shouldWin() {
            service.play("AAABA");
            assertThat(output).last().isEqualTo("Player A wins the game");
        }

        @Test
        @DisplayName("From THIRTY/FORTY → Player A scores → FORTY/FORTY → Deuce")
        void fromThirtyForty_playerAScores_shouldTriggerDeuce() {
            service.play("AABBBA");
            assertThat(output).last().isEqualTo("Deuce");
        }

        @Test
        @DisplayName("From Deuce → Player A scores → Advantage Player A")
        void fromDeuce_playerAScores_shouldGiveAdvantageA() {
            service.play("AAABBBA");
            assertThat(output).last().isEqualTo("Advantage Player A");
        }

        @Test
        @DisplayName("From Deuce → Player B scores → Advantage Player B")
        void fromDeuce_playerBScores_shouldGiveAdvantageB() {
            service.play("AAABBBB");
            assertThat(output).last().isEqualTo("Advantage Player B");
        }

        @Test
        @DisplayName("From Advantage A → Player A scores → Player A wins")
        void fromAdvantageA_playerAScores_shouldWin() {
            service.play("AAABBBAA");
            assertThat(output).last().isEqualTo("Player A wins the game");
        }

        @Test
        @DisplayName("From Advantage A → Player B scores → back to Deuce")
        void fromAdvantageA_playerBScores_shouldReturnToDeuce() {
            service.play("AAABBBAB");
            assertThat(output).last().isEqualTo("Deuce");
        }

        @Test
        @DisplayName("From Advantage B → Player B scores → Player B wins")
        void fromAdvantageB_playerBScores_shouldWin() {
            service.play("AAABBBBB");
            assertThat(output).last().isEqualTo("Player B wins the game");
        }

        @Test
        @DisplayName("From Advantage B → Player A scores → back to Deuce")
        void fromAdvantageB_playerAScores_shouldReturnToDeuce() {
            service.play("AAABBBA");
            assertThat(output).last().isEqualTo("Advantage Player A");
        }
    }

    @Nested
    @DisplayName("Score formatting — formatScore")
    class ScoreFormatting {

        @Test
        @DisplayName("Normal score is formatted as 'Player A : X / Player B : Y'")
        void normalScore_shouldBeFormatted() {
            service.play("AB");
            assertThat(output).contains("Player A : 15 / Player B : 15");
        }

        @Test
        @DisplayName("Deuce is formatted as 'Deuce'")
        void deuce_shouldBeFormattedAsDeuce() {
            service.play("AAABBB");
            assertThat(output).last().isEqualTo("Deuce");
        }

        @Test
        @DisplayName("Advantage is formatted as 'Advantage Player X'")
        void advantage_shouldBeFormattedWithPlayerName() {
            service.play("AAABBBA");
            assertThat(output).last().isEqualTo("Advantage Player A");
        }

        @Test
        @DisplayName("Win is formatted as 'Player X wins the game'")
        void win_shouldBeFormattedWithPlayerName() {
            service.play("AAAA");
            assertThat(output).last().isEqualTo("Player A wins the game");
        }
    }
}
