public class OptimizedGuesser { //findLengthBinary, trySingleCharacterPatterns, Smarter Initial Guessing, Heuristic Search, Intelligent Pruning, Dynamic Strategy Selection,
    private static final char[] CHARACTERS = {'B', 'A', 'C', 'X', 'I', 'U'};
    private static final int CHAR_COUNT = CHARACTERS.length;
    private static final int[] CHAR_TO_INDEX = new int[256];

    // Data structures
    private String[] eliminatedCandidates = new String[100000];
    private int eliminatedCount = 0;
    private boolean[][] positionCandidates;
    private boolean[][] positionEliminated;
    private double[] charFrequency = new double[256];
    private GuessResult[] guessHistory = new GuessResult[10000];
    private int guessHistoryCount = 0;

    static {
        for (int i = 0; i < CHARACTERS.length; i++) {
            CHAR_TO_INDEX[CHARACTERS[i]] = i;
        }
    }

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

        int correctLength = findLength(code);
        System.out.println("Found length: " + correctLength);

        initializeDataStructures(correctLength);
        String secretCode = solveWithOptimizedStrategy(code, correctLength);

        System.out.println("I found the secret code. It is " + secretCode);
    }

    private int findLength(SecretCode code) {
        for (int length = 1; length <= 18; length++) {
            String testGuess = "B".repeat(length);
            int result = code.guess(testGuess);
            if (result != -2) return length;
        }
        return -1;
    }

    private void initializeDataStructures(int length) {
        positionCandidates = new boolean[length][CHAR_COUNT];
        positionEliminated = new boolean[length][CHAR_COUNT];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < CHAR_COUNT; j++) {
                positionCandidates[i][j] = true;
                positionEliminated[i][j] = false;
            }
        }

        // Set initial frequencies
        double[] freqs = {0.2, 0.15, 0.15, 0.15, 0.2, 0.15};
        for (int i = 0; i < CHAR_COUNT; i++) {
            charFrequency[CHARACTERS[i]] = freqs[i];
        }
    }

    private String solveWithOptimizedStrategy(SecretCode code, int length) {
        // Strategy 1: Try single character patterns
        System.out.println("Trying single character patterns...");
        for (char c : CHARACTERS) {
            String candidate = String.valueOf(c).repeat(length);
            if (testCandidate(code, candidate, length)) return candidate;
        }

        // Strategy 2: Try common segmented patterns
        String segmentResult = trySegmentedPatterns(code, length);
        if (segmentResult != null) return segmentResult;

        // Strategy 3: Use original position-based strategy
        return solveByPositionStrategy(code, length);
    }

    private String trySegmentedPatterns(SecretCode code, int length) {
        System.out.println("Trying segmented patterns...");

        // Common patterns for length 15
        if (length == 15) {
            String[][] patterns = {
                    {"4,4,4,3", "BAIU"},
                    {"5,5,5", "BAI"},
                    {"3,4,4,4", "BAIU"},
                    {"3,3,3,3,3", "BAICU"}
            };

            for (String[] pattern : patterns) {
                String result = tryPattern(code, length, pattern[0], pattern[1]);
                if (result != null) return result;
            }
        }

        return null;
    }

    private String tryPattern(SecretCode code, int length, String patternStr, String chars) {
        String[] lengths = patternStr.split(",");
        int[] pattern = new int[lengths.length];
        for (int i = 0; i < lengths.length; i++) {
            pattern[i] = Integer.parseInt(lengths[i]);
        }

        // Try with suggested characters first
        StringBuilder candidate = new StringBuilder();
        for (int i = 0; i < pattern.length; i++) {
            char c = i < chars.length() ? chars.charAt(i) : CHARACTERS[i % CHAR_COUNT];
            candidate.append(String.valueOf(c).repeat(pattern[i]));
        }

        if (candidate.length() == length) {
            String candidateStr = candidate.toString();
            if (testCandidate(code, candidateStr, length)) return candidateStr;
        }

        // Try limited combinations if first attempt fails
        return tryLimitedPattern(code, length, pattern, 15);
    }

    private String tryLimitedPattern(SecretCode code, int length, int[] pattern, int maxGuesses) {
        char[] smartOrder = {'B', 'I', 'U', 'A', 'C', 'X'};
        return generateLimitedCombinations(code, length, pattern, new char[pattern.length],
                0, maxGuesses, new int[]{0}, smartOrder);
    }

    private String generateLimitedCombinations(SecretCode code, int length, int[] pattern,
                                               char[] segments, int pos, int maxGuesses,
                                               int[] count, char[] order) {
        if (count[0] >= maxGuesses) return null;

        if (pos == pattern.length) {
            StringBuilder candidate = new StringBuilder();
            for (int i = 0; i < segments.length; i++) {
                candidate.append(String.valueOf(segments[i]).repeat(pattern[i]));
            }

            if (candidate.length() == length) {
                String candidateStr = candidate.toString();
                if (!isEliminated(candidateStr)) {
                    count[0]++;
                    if (testCandidate(code, candidateStr, length)) return candidateStr;
                }
            }
            return null;
        }

        for (char c : order) {
            segments[pos] = c;
            String result = generateLimitedCombinations(code, length, pattern, segments,
                    pos + 1, maxGuesses, count, order);
            if (result != null) return result;
        }
        return null;
    }

    private String solveByPositionStrategy(SecretCode code, int length) {
        String smartGuess = generateSmartInitialGuess(length);
        if (testCandidate(code, smartGuess, length)) return smartGuess;

        char[] current = smartGuess.toCharArray();
        for (int pos = 0; pos < length; pos++) {
            current[pos] = findBestCharForPosition(code, current, pos, length);
        }

        String result = new String(current);
        if (testCandidate(code, result, length)) return result;

        return bruteForceOptimized(code, length);
    }

    private String generateSmartInitialGuess(int length) {
        StringBuilder guess = new StringBuilder();
        for (int i = 0; i < length; i++) {
            guess.append(CHARACTERS[i % CHAR_COUNT]);
        }
        return guess.toString();
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

                if (score == totalLength) return c;
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
                if (testCandidate(code, candidate, length)) return candidate;
            }
            if (!nextCandidate(current)) break;
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

    // Helper methods
    private boolean testCandidate(SecretCode code, String candidate, int targetLength) {
        int score = code.guess(candidate);
        addGuessHistory(candidate, score);
        addEliminated(candidate);
        return score == targetLength;
    }

    private void addGuessHistory(String guess, int score) {
        if (guessHistoryCount < guessHistory.length) {
            guessHistory[guessHistoryCount++] = new GuessResult(guess, score);
        }
    }

    private void addEliminated(String guess) {
        if (eliminatedCount < eliminatedCandidates.length) {
            eliminatedCandidates[eliminatedCount++] = guess;
        }
    }

    private boolean isEliminated(String guess) {
        for (int i = 0; i < eliminatedCount; i++) {
            if (eliminatedCandidates[i].equals(guess)) return true;
        }
        return false;
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
            charFrequency[c] = charFrequency[c] * (0.8 + 0.4 * ratio);
        }
    }

    static int order(char c) {
        return CHAR_TO_INDEX[c];
    }

    static char charOf(int order) {
        return CHARACTERS[order];
    }
}
