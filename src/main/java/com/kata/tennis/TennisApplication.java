package com.kata.tennis;

import com.kata.tennis.domain.port.in.PlayGameUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@Slf4j
@SpringBootApplication
public class TennisApplication {

    private static final String QUIT_COMMAND = "quit";

    public static void main(String[] args) {
        SpringApplication.run(TennisApplication.class, args);
    }

    @Bean
    CommandLineRunner run(PlayGameUseCase playGameUseCase) {
        return args -> {
            Scanner scanner = new Scanner(System.in);

            System.out.println("=== Tennis Score Computer ===");
            System.out.println("Enter a ball sequence (e.g. ABABAA) or type 'quit' to exit.");

            while (true) {
                System.out.print("\nEnter ball sequence: ");
                String input = scanner.nextLine().trim();

                if (QUIT_COMMAND.equalsIgnoreCase(input)) {
                    System.out.println("Goodbye!");
                    log.info("User exited the application.");
                    break;
                }

                if (input.isBlank()) {
                    System.out.println("Input cannot be blank. Please try again.");
                    continue;
                }

                System.out.println("--- Result ---");
                try {
                    playGameUseCase.play(input);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid input: " + e.getMessage());
                    log.warn("Invalid input provided by user: {}", e.getMessage());
                }
                System.out.println("--- End of game ---");
            }

            scanner.close();
        };
    }
}
