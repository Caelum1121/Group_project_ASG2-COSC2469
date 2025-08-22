public class OptimizedSecretCodeGuesser{
    private static final char[] CHARACTERS = {'B', 'A', 'C', 'X', 'I', 'U'};
    private static final int CHAR_COUNT = CHARACTERS.length;

    // Map char → index (ASCII based)
    private static final int[] CHAR_TO_INDEX = new int[256];

    // Eliminated guesses (simple array instead of HashSet)
    private String[] eliminatedCandidates = new String[100000];
    private int eliminatedCount = 0;

    // Candidates per position (boolean flags instead of HashSet)
    private boolean[][] positionCandidates;
    private boolean[][] positionEliminated;

    // Character frequency (parallel arrays instead of HashMap)
    private double[] charFrequency = new double[256];

    // Guess history (fixed array instead of ArrayList)
    private GuessResult[] guessHistory = new GuessResult[10000];
    private int guessHistoryCount = 0;

    static {
        for (int i = 0; i < CHARACTERS.length; i++) {
            CHAR_TO_INDEX[CHARACTERS[i]] = i;
        }
    }

    // Guess result record
    private static class GuessResult {
        String guess;
        int correctPositions;

        GuessResult(String guess, int correctPositions) {
            this.guess = guess;
            this.correctPositions = correctPositions;
        }
    }

    public void start() {
        SecretCode code = new SecretCode();
        long startTime = System.currentTimeMillis();

        int correctLength = findLengthBinary(code);
        System.out.println("Found length: " + correctLength);

        initializeDataStructures(correctLength);

        String secretCode = solveWithDynamicStrategy(code, correctLength);

        long endTime = System.currentTimeMillis();
        System.out.println("I found the secret code. It is " + secretCode);
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
    }

    /**
     * Find length
     */
    private int findLengthBinary(SecretCode code) {
        for (int length = 1; length <= 18; length++) {
            String testGuess = "B".repeat(length);
            int result = code.guess(testGuess);
            if (result != -2) {
                return length;
            }
        }
        return -1;
    }

    /**
     * Init structures
     */
    private void initializeDataStructures(int length) {
        positionCandidates = new boolean[length][CHAR_COUNT];
        positionEliminated = new boolean[length][CHAR_COUNT];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < CHAR_COUNT; j++) {
                positionCandidates[i][j] = true;
                positionEliminated[i][j] = false;
            }
        }

        // initial frequency
        charFrequency['B'] = 0.2;
        charFrequency['A'] = 0.15;
        charFrequency['C'] = 0.15;
        charFrequency['X'] = 0.15;
        charFrequency['I'] = 0.2;
        charFrequency['U'] = 0.15;
    }

    private String solveWithDynamicStrategy(SecretCode code, int length) {
        if (length <= 6) {
            return solveByPositionStrategy(code, length);
        } else if (length <= 12) {
            return solveByPositionStrategy(code, length);
        } else {
            return solveByPositionStrategy(code, length);
        }
    }

    private String solveByPositionStrategy(SecretCode code, int length) {
        String smartGuess = generateSmartInitialGuess(length);
        int initialScore = code.guess(smartGuess);
        addGuessHistory(smartGuess, initialScore);

        if (initialScore == length) {
            return smartGuess;
        }

        char[] current = smartGuess.toCharArray();

        for (int pos = 0; pos < length; pos++) {
            current[pos] = findBestCharForPosition(code, current, pos, length);
        }

        String result = new String(current);
        int finalScore = code.guess(result);
        if (finalScore == length) {
            return result;
        }

        return bruteForceOptimized(code, length);
    }

    private String generateSmartInitialGuess(int length) {
        StringBuilder guess = new StringBuilder();
        for (int i = 0; i < length; i++) {
            guess.append(CHARACTERS[i % CHAR_COUNT]);
        }
        return guess.toString();
    }

    private void updatePruningInfo(String guess, int correctPositions) {
        addEliminated(guess);

        for (int pos = 0; pos < guess.length(); pos++) {
            char c = guess.charAt(pos);
            if (correctPositions < guess.length() / 3) {
                positionEliminated[pos][CHAR_TO_INDEX[c]] = true;
                positionCandidates[pos][CHAR_TO_INDEX[c]] = false;
            }
        }

        updateCharacterFrequency(guess, correctPositions);
    }

    private void updateCharacterFrequency(String guess, int correctPositions) {
        double ratio = (double) correctPositions / guess.length();

        for (char c : guess.toCharArray()) {
            double currentFreq = charFrequency[c];
            charFrequency[c] = currentFreq * (0.8 + 0.4 * ratio);
        }
    }

    private char findBestCharForPosition(SecretCode code, char[] current, int pos, int totalLength) {
        char originalChar = current[pos];
        int bestScore = -1;
        char bestChar = originalChar;

        for (char c : CHARACTERS) {
            current[pos] = c;
            String testGuess = new String(current);

            if (!isEliminated(testGuess)) {
                int score = code.guess(testGuess);
                addGuessHistory(testGuess, score);
                addEliminated(testGuess);

                if (score == totalLength) {
                    return c;
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestChar = c;
                }
            }
        }

        current[pos] = bestChar;
        return bestChar;
    }

    private String bruteForceOptimized(SecretCode code, int length) {
        char[] current = new char[length];
        for (int i = 0; i < length; i++) current[i] = 'B';

        while (true) {
            String candidate = new String(current);
            if (!isEliminated(candidate)) {
                int score = code.guess(candidate);
                if (score == length) {
                    return candidate;
                }
                addEliminated(candidate);
            }

            if (!nextCandidate(current)) {
                break;
            }
        }
        return new String(current);
    }

    private boolean nextCandidate(char[] current) {
        for (int i = current.length - 1; i >= 0; i--) {
            int currentOrder = CHAR_TO_INDEX[current[i]];
            if (currentOrder < CHARACTERS.length - 1) {
                current[i] = CHARACTERS[currentOrder + 1];
                return true;
            }
            current[i] = 'B';
        }
        return false;
    }

    // ==== Helpers ====
    private void addGuessHistory(String guess, int score) {
        guessHistory[guessHistoryCount++] = new GuessResult(guess, score);
    }

    private void addEliminated(String guess) {
        eliminatedCandidates[eliminatedCount++] = guess;
    }

    private boolean isEliminated(String guess) {
        for (int i = 0; i < eliminatedCount; i++) {
            if (eliminatedCandidates[i].equals(guess)) return true;
        }
        return false;
    }

    static int order(char c) {
        return CHAR_TO_INDEX[c];
    }

    static char charOf(int order) {
        return CHARACTERS[order];
    }
}
