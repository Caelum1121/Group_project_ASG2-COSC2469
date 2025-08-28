public class SecretCodeGuesser {

    // Available alphabet for constructing guesses
    private static final char[] ALPHABET = new char[] {'B', 'A', 'C', 'X', 'I', 'U'};

    // Cache the number of 'B' matches at correct length to avoid redundant guess
    private int lastBMatchesAtCorrectLength = -1;
    /**
     * Main entry point that orchestrates the two-phase code breaking process:
     * Phase 1: Determine the correct length of the secret code
     * Phase 2: Deduce the actual code using differential feedback analysis
     */
    public void start() {
        SecretCode code = new SecretCode();

        // Phase 1: determine correct length by probing lengths until not -2
        int length = determineLength(code);
        if (length <= 0) {
            System.out.println("Failed to determine secret code length.");
            return;
        }

        // Phase 2: deduce code efficiently using differential feedback
        String found = deduceCode(code, length);
        System.out.println("I found the secret code. It is " + found);

        // Print concise complexity info for the submission report
        System.out.println("Found length: " + length );
    }

    /**
     * Phase 1: Determines the correct length of the secret code
     * Uses linear search, testing lengths 1-18 with uniform 'B' strings
     * Returns the length when feedback is not -2 (invalid length indicator)
     */
    private int determineLength(SecretCode code) {

        // Linear search for length in range [1, 18]
        for (int len = 1; len <= 18; len++) {
            // Create a candidate string of all 'B's at current test length
            String candidate = repeatChar('B', len);
            int result = callGuess(code, candidate);

            // If result is not -2, we found a valid length
            // -2 typically indicates "wrong length" in mastermind-style games
            if (result != -2) {
                // Found valid length - cache the 'B' match count for Phase 2
                lastBMatchesAtCorrectLength = result;
                return len;
            }
        }

        // If we reach here, no valid length found (shouldn't happen for valid secret codes)
        return -1;
    }

    /**
     * Phase 2: Deduces the actual secret code using a sophisticated differential analysis approach
     * Strategy:
     * 1. Measure how many times each letter appears in correct positions using uniform strings
     * 2. Choose the most frequent letter as baseline (appears in most positions)
     * 3. Systematically test each position by substituting candidates and analyzing score changes
     */
    private String deduceCode(SecretCode code, int length) {
        // Step 1: Measure per-letter exact-position counts using uniform strings
        // This tells us how many positions each letter occupies in the secret code
        int[] letterCounts = new int[ALPHABET.length];

        // Test each letter by making a uniform string (e.g., "BBBB", "AAAA", etc.)
        for (int i = 0; i < ALPHABET.length; i++) {
            char letter = ALPHABET[i];
            int matches;

            // Optimization: reuse cached result for 'B' from length determination phase
            if (letter == 'B' && lastBMatchesAtCorrectLength >= 0) {
                String guess = repeatChar(letter, length);
                matches = lastBMatchesAtCorrectLength;
                // If all positions match, we found the code (all same letter)
                if (matches == length) {
                    return guess;
                }

            } else {
                // Test uniform string of this letter
                String guess = repeatChar(letter, length);
                matches = callGuess(code, guess);
                if (matches == length) {
                    return guess; // Found uniform solution
                }
            }
            // Store count of positions where this letter appears correctly
            letterCounts[i] = Math.max(0, matches);
        }

        // Step 2: Choose the baseline letter (appears in most positions)
        // This minimizes the number of position-by-position tests needed
        int baselineIndex = 0;
        for (int i = 1; i < ALPHABET.length; i++) {
            if (letterCounts[i] > letterCounts[baselineIndex]) {
                baselineIndex = i;
            }
        }

        char baselineLetter = ALPHABET[baselineIndex];
        // Start with all positions set to baseline letter
        char[] current = repeatCharArray(baselineLetter, length);
        int currentScore = letterCounts[baselineIndex];

        // Track remaining letter counts to guide search efficiency
        int[] remainingByLetter = letterCounts.clone();

        // Track which positions have been definitively resolved
        boolean[] resolved = new boolean[length];
        int resolvedCount = 0;

        // If all positions are baseline already, we're done
        if (currentScore == length) {
            return String.valueOf(current);
        }

        // Step 3: Resolve each position using minimal tests with differential feedback
        for (int pos = 0; pos < length; pos++) {
            if (resolved[pos]) continue;

            // Optimization: if remaining positions equal remaining baseline count,
            // all remaining positions must be baseline
            int remainingPositions = length - resolvedCount;
            if (remainingPositions == remainingByLetter[baselineIndex]) {
                for (int j = pos; j < length; j++) {
                    if (!resolved[j]) {
                        current[j] = baselineLetter;
                        resolved[j] = true;
                        resolvedCount++;
                    }
                }
                break;
            }

            // Get letters ordered by remaining count (most promising first)
            int[] order = orderByCountsDescending(remainingByLetter);
            boolean foundForThisPos = false;

            // Test candidate letters for this position in order of likelihood
            for (int idx : order) {
                if (idx == baselineIndex) continue; // Skip baseline (already set)
                if (remainingByLetter[idx] <= 0) continue; // Skip exhausted letters

                char candidate = ALPHABET[idx];
                char original = current[pos];

                // Temporarily substitute candidate and test
                current[pos] = candidate;
                int newScore = callGuess(code, String.valueOf(current));

                if (newScore > currentScore) {
                    // Score increased: candidate is correct for this position
                    currentScore = newScore;
                    resolved[pos] = true;
                    resolvedCount++;
                    remainingByLetter[idx]--; // Decrement remaining count for this letter
                    foundForThisPos = true;
                    break;
                } else if (newScore < currentScore) {
                    // Score decreased: original (baseline) was correct for this position
                    // This is an optimization that detects baseline positions early
                    current[pos] = original; // Revert to baseline
                    resolved[pos] = true;
                    resolvedCount++;
                    remainingByLetter[baselineIndex]--; // Decrement baseline count
                    foundForThisPos = true;
                    break; // Stop testing more candidates for this position
                } else {
                    // Score unchanged: candidate is wrong, revert and try next
                    current[pos] = original;
                }
            }

            // If no candidate changed the score, assume baseline is correct
            if (!foundForThisPos) {
                resolved[pos] = true;
                resolvedCount++;
                remainingByLetter[baselineIndex]--;
            }

            // Early termination if we've found all positions
            if (currentScore == length) {
                return String.valueOf(current);
            }
        }

        // Final confirmation guess to trigger success print if not already triggered
        callGuess(code, String.valueOf(current));
        return String.valueOf(current);
    }

    /**
     * Wrapper for SecretCode.guess() that tracks the number of guesses made
     */
    private int callGuess(SecretCode code, String guess) {
        return code.guess(guess);
    }

    /**
     * Utility: Creates a string of repeated characters
     */
    private String repeatChar(char c, int length) {
        return String.valueOf(repeatCharArray(c, length));
    }

    /**
     * Utility: Creates a character array of repeated characters
     */
    private char[] repeatCharArray(char c, int length) {
        char[] arr = new char[length];
        for (int i = 0; i < length; i++) arr[i] = c;
        return arr;
    }

    /**
     * Returns indices of the counts array sorted by descending count values
     * Used to prioritize testing letters that are more likely to appear
     * in remaining positions (greedy optimization)
     */
    private int[] orderByCountsDescending(int[] counts) {
        int k = counts.length;
        int[] order = new int[k];
        boolean[] used = new boolean[k];

        // Simple selection sort to find indices in descending count order
        for (int r = 0; r < k; r++) {
            int best = -1;
            // Find the unused index with highest count
            for (int i = 0; i < k; i++) {
                if (used[i]) continue;
                if (best == -1 || counts[i] > counts[best]) best = i;
            }
            order[r] = best;
            used[best] = true;
        }
        return order;
    }
}