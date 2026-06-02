# Tennis Kata — Java 21 + Spring Boot 3

## What does this project do?

This project implements a **Tennis scoring computer**.

Given a sequence of characters `'A'` or `'B'` (representing which player won each ball),
the application prints the score after every ball and announces the winner at the end.

### Tennis scoring rules

| Balls won | Score displayed |
|-----------|----------------|
| 0         | 0              |
| 1         | 15             |
| 2         | 30             |
| 3         | 40             |

**Special rules:**
- If both players reach **40**, it is called **Deuce**
- After a Deuce, the next player to score gets the **Advantage**
- If the player with the **Advantage** scores again → they **win the game**
- If the player **without** the Advantage scores → back to **Deuce**

### Example

Input: `"ABABAA"`

```
Player A : 15 / Player B : 0
Player A : 15 / Player B : 15
Player A : 30 / Player B : 15
Player A : 30 / Player B : 30
Player A : 40 / Player B : 30
Player A wins the game
```

---

## Technical architecture — Hexagonal (Ports & Adapters)

The project follows the **Hexagonal Architecture** pattern, also known as Ports & Adapters.
The core idea is that the **business logic is isolated** from technical concerns (Spring, console, etc.).

```
┌─────────────────────────────────────────────────────────┐
│                        DOMAIN                           │
│   model/ (Player, Score, GameState)                     │
│   port/in  → PlayGameUseCase (interface)                │
│   port/out → ScorePresenter  (interface)                │
└────────────────────┬────────────────────────────────────┘
                     │ depends on
┌────────────────────▼────────────────────────────────────┐
│                     APPLICATION                         │
│   TennisGameApplicationService (@Service)               │
│   → implements PlayGameUseCase                          │
│   → uses ScorePresenter                                 │
└────────────────────┬────────────────────────────────────┘
                     │ depends on
┌────────────────────▼────────────────────────────────────┐
│                   INFRASTRUCTURE                        │
│   ConsoleScorePresenter (@Component)                    │
│   → implements ScorePresenter → System.out.println      │
└─────────────────────────────────────────────────────────┘
```

**Dependency rule:** Domain ← Application ← Infrastructure. Never the other way around.

---

## Class descriptions

### `TennisApplication`
Spring Boot entry point. Declares a `CommandLineRunner` bean that launches an **interactive console loop** on startup.

On each iteration:
- The user is prompted to enter a ball sequence (e.g. `ABABAA` or `ababaa`)
- The application prints the score after each ball and announces the winner
- The user can type `quit` to exit the application

The input is **case-insensitive**: `a` and `A`, `b` and `B` are treated identically.
Invalid input (blank, unknown characters) is caught and the user is prompted again without crashing.

---

### `domain/model/Player`
Enum representing the two players: `A` and `B`.
Each value holds a display name (`"Player A"`, `"Player B"`).

---

### `domain/model/Score`
Enum representing the four possible score values in tennis:
`LOVE (0)`, `FIFTEEN (15)`, `THIRTY (30)`, `FORTY (40)`.
Each value carries a label used for display.
Provides a `next()` method to advance to the next score level.

---

### `domain/model/GameState`
Immutable `record` representing the full state of the game at any moment:
- `scoreA` / `scoreB` — current score for each player
- `isDeuce` — whether the game is in deuce
- `advantage` — the player who has the advantage (or `null`)
- `winner` — the player who won the game (or `null`)

Provides `initial()` factory method and helper methods `isOver()`, `getAdvantage()`, `getWinner()`.

---

### `domain/port/in/PlayGameUseCase`
**Inbound port** (interface). Defines the contract for the main use case:
`void play(String balls)`. The application service implements this interface.
`TennisApplication` and tests depend only on this interface, never on the implementation directly.

---

### `domain/port/out/ScorePresenter`
**Outbound port** (interface). Defines the contract for displaying a score message:
`void displayScore(String message)`. The application service calls this interface.
The infrastructure layer provides the concrete implementation.

---

### `application/service/TennisGameApplicationService`
The **single `@Service`** of the application. Implements `PlayGameUseCase`.

Responsibilities:
- Normalises the input to uppercase (case-insensitive handling)
- Validates the input string — throws `IllegalArgumentException` on invalid characters
- Iterates over each ball character and computes the new `GameState`
- Contains **all the business logic**: normal scoring, deuce detection, advantage, win conditions
- Formats the score message for each state
- Delegates display to `ScorePresenter`
- Logs every step with `@Slf4j`

---

### `infrastructure/out/console/ConsoleScorePresenter`
**Outbound adapter**. Implements `ScorePresenter`.
Its only responsibility is to print the score message to the standard output via `System.out.println`.
Annotated with `@Component` so Spring injects it into the application service automatically.

---


## Project structure

```
src/main/java/com/kata/tennis/
├── TennisApplication.java                        ← interactive console loop
├── domain/
│   ├── model/
│   │   ├── Player.java
│   │   ├── Score.java
│   │   └── GameState.java
│   └── port/
│       ├── in/  PlayGameUseCase.java             ← inbound port
│       └── out/ ScorePresenter.java              ← outbound port
├── application/
│   └── service/
│       └── TennisGameApplicationService.java     ← business logic + @Service
└── infrastructure/
    └── out/console/
        └── ConsoleScorePresenter.java            ← System.out.println adapter

src/test/java/com/kata/tennis/
└── application/service/
    └── TennisGameApplicationServiceTest.java     ← unit tests (no Spring)
```

---

## Tech stack

| Technology       | Version |
|------------------|---------|
| Java             | 21      |
| Spring Boot      | 3.3.0   |
| JUnit Jupiter    | 5.10.2  |
| AssertJ          | 3.25.3  |
| Lombok (`@Slf4j`)| latest  |
| Maven            | 3.x     |

---

## How to run the application

### Prerequisites
- Java 21 installed
- Maven 3.x installed

### Build the project
```bash
mvn clean package
```

### Run the application
```bash
mvn spring-boot:run
```
or with the generated JAR:
```bash
java -jar target/tennis-1.0.0-SNAPSHOT.jar
```

### Interactive usage

Once launched, the application displays a prompt in the console:

```
=== Tennis Score Computer ===
Enter a ball sequence (e.g. ABABAA) or type 'quit' to exit.

Enter ball sequence:
```

**Rules for input:**
- Type a sequence of `A` and `B` characters — each character represents a ball won by that player
- Input is **case-insensitive**: `ababaa`, `ABABAA` and `AbAbAa` all produce the same result
- Type `quit` to exit the application

**Example session:**
```
Enter ball sequence: ababaa
--- Result ---
Player A : 15 / Player B : 0
Player A : 15 / Player B : 15
Player A : 30 / Player B : 15
Player A : 30 / Player B : 30
Player A : 40 / Player B : 30
Player A wins the game
--- End of game ---

Enter ball sequence: AAABBBAA
--- Result ---
Player A : 15 / Player B : 0
Player A : 30 / Player B : 0
Player A : 40 / Player B : 0
Player A : 40 / Player B : 15
Player A : 40 / Player B : 30
Deuce
Advantage Player A
Player A wins the game
--- End of game ---

Enter ball sequence: quit
Goodbye!
```

**Error handling:**
- If the sequence contains invalid characters (not `A` or `B`), an error message is displayed and a new sequence can be entered
- If the input is blank, the user is prompted again

### Run the tests
```bash
mvn test
```

