import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Number Guessing Game (console version)
 *
 * Features:
 *  - Random number generated at the start of each round
 *  - Too High! / Too Low! / Correct! hints
 *  - Attempt counter shown on every prompt
 *  - Maximum attempts limit; reveals the number if you lose
 *  - Play Again option
 *  - Score tracking across rounds ("Round X - guessed in Y attempts")
 *  - Difficulty levels: Easy, Medium, Hard
 */
public class NumberGuessingGame {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    // ---------- Difficulty levels ----------
    enum Difficulty {
        EASY("Easy", 50, 10),
        MEDIUM("Medium", 100, 7),
        HARD("Hard", 200, 5);

        final String label;
        final int maxNumber;
        final int maxAttempts;

        Difficulty(String label, int maxNumber, int maxAttempts) {
            this.label = label;
            this.maxNumber = maxNumber;
            this.maxAttempts = maxAttempts;
        }
    }

    // ---------- Stores the result of one round ----------
    static class RoundResult {
        final int roundNumber;
        final Difficulty difficulty;
        final boolean won;
        final int attemptsUsed;
        final int secret;

        RoundResult(int roundNumber, Difficulty difficulty, boolean won, int attemptsUsed, int secret) {
            this.roundNumber = roundNumber;
            this.difficulty = difficulty;
            this.won = won;
            this.attemptsUsed = attemptsUsed;
            this.secret = secret;
        }

        @Override
        public String toString() {
            if (won) {
                return "Round " + roundNumber + " - guessed in " + attemptsUsed
                        + (attemptsUsed == 1 ? " attempt" : " attempts")
                        + " [" + difficulty.label + "]";
            }
            return "Round " + roundNumber + " - lost (number was " + secret + ") ["
                    + difficulty.label + "]";
        }
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        List<RoundResult> history = new ArrayList<>();
        int roundNumber = 0;

        System.out.println("=====================================");
        System.out.println("      WELCOME TO NUMBER GUESSING     ");
        System.out.println("=====================================");

        boolean playAgain;
        do {
            roundNumber++;
            Difficulty difficulty = chooseDifficulty();
            RoundResult result = playRound(roundNumber, difficulty);
            history.add(result);

            printScoreboard(history);
            playAgain = askPlayAgain();
        } while (playAgain);

        System.out.println("\nThanks for playing! Final results:");
        printScoreboard(history);
    }

    // ---------- Difficulty menu ----------
    private static Difficulty chooseDifficulty() {
        System.out.println("\nChoose difficulty:");
        Difficulty[] levels = Difficulty.values();
        for (int i = 0; i < levels.length; i++) {
            Difficulty d = levels[i];
            System.out.printf("  %d) %-6s (1-%d, %d attempts)%n",
                    i + 1, d.label, d.maxNumber, d.maxAttempts);
        }
        int choice = readInt("Enter choice (1-" + levels.length + "): ", 1, levels.length);
        return levels[choice - 1];
    }

    // ---------- One round of the game ----------
    private static RoundResult playRound(int roundNumber, Difficulty difficulty) {
        int secret = random.nextInt(difficulty.maxNumber) + 1; // 1..maxNumber
        int attempts = 0;

        System.out.println("\n--- Round " + roundNumber + " (" + difficulty.label + ") ---");
        System.out.println("I'm thinking of a number between 1 and " + difficulty.maxNumber
                + ". You have " + difficulty.maxAttempts + " attempts.");

        while (attempts < difficulty.maxAttempts) {
            String prompt = "Attempt " + (attempts + 1) + "/" + difficulty.maxAttempts + " - Your guess: ";
            int guess = readInt(prompt, 1, difficulty.maxNumber);
            attempts++;

            if (guess == secret) {
                System.out.println("Correct! You got it in " + attempts
                        + (attempts == 1 ? " attempt." : " attempts."));
                return new RoundResult(roundNumber, difficulty, true, attempts, secret);
            } else if (guess > secret) {
                System.out.println("Too High!");
            } else {
                System.out.println("Too Low!");
            }

            int remaining = difficulty.maxAttempts - attempts;
            if (remaining > 0) {
                System.out.println("Attempts left: " + remaining);
            }
        }

        System.out.println("You Lost! The number was " + secret + ".");
        return new RoundResult(roundNumber, difficulty, false, attempts, secret);
    }

    // ---------- Scoreboard ----------
    private static void printScoreboard(List<RoundResult> history) {
        System.out.println("\n========== SCOREBOARD ==========");
        int wins = 0;
        for (RoundResult r : history) {
            System.out.println(r);
            if (r.won) wins++;
        }
        System.out.println("--------------------------------");
        System.out.println("Won " + wins + " of " + history.size()
                + (history.size() == 1 ? " round" : " rounds"));
        System.out.println("================================");
    }

    // ---------- Play again prompt ----------
    private static boolean askPlayAgain() {
        while (true) {
            System.out.print("\nPlay again? (yes/no): ");
            String answer = readLine().trim().toLowerCase();
            if (answer.equals("yes") || answer.equals("y")) return true;
            if (answer.equals("no") || answer.equals("n")) return false;
            System.out.println("Please type 'yes' or 'no'.");
        }
    }

    // ---------- Input helpers (with validation) ----------
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = readLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("That's not a valid number. Try again.");
            }
        }
    }

    private static String readLine() {
        if (!scanner.hasNextLine()) {
            System.out.println("\nInput closed. Goodbye!");
            System.exit(0);
        }
        return scanner.nextLine();
    }
}

