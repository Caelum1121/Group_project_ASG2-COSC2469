public class SecretCodeGuesser {

  private static final char[] ALPHABET = new char[] {'B', 'A', 'C', 'X', 'I', 'U'};
  private int lastBMatchesAtCorrectLength = -1;
  private int localGuessCount = 0;

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
    System.out.println("n = " + length + ", k = " + ALPHABET.length);
    System.out.println("Total guess() calls: " + localGuessCount + " (upper bound ≈ " + (ALPHABET.length * length + ALPHABET.length + 1) + ")");
    System.out.println("Time complexity (with guess O(n)): O(n^2); Space: O(n)");
    System.out.println("If guess is O(1): Time: O(n); Space: O(n)");
  }

  private int determineLength(SecretCode code) {
    int lengthDetectionGuesses = 0;
    
    // Smart linear search optimized for short lengths (≤18)
    // Start with common short lengths first, then systematic search
    int[] priorityLengths = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
    
    for (int len : priorityLengths) {
      String candidate = repeatChar('B', len);
      int result = callGuess(code, candidate);
      lengthDetectionGuesses++;
      
      if (result != -2) {
        // Found valid length
        lastBMatchesAtCorrectLength = result;
        return len;
      }
    }
    
    // If we reach here, no valid length found (shouldn't happen for valid secret codes)
    return -1;
  }

  private String deduceCode(SecretCode code, int length) {
    // 1) Measure per-letter exact-position counts using uniform strings
    int[] letterCounts = new int[ALPHABET.length];
    // We already know the length, but not which uniform guess matched how many.
    for (int i = 0; i < ALPHABET.length; i++) {
      char letter = ALPHABET[i];
      int matches;
      if (letter == 'B' && lastBMatchesAtCorrectLength >= 0) {
          String guess = repeatChar(letter, length);
          matches = lastBMatchesAtCorrectLength;
          if (matches == length) {
              return guess;
          }

      } else {
        String guess = repeatChar(letter, length);
        matches = callGuess(code, guess);
        if (matches == length) {
            return guess;
        }
      }
      // matches >= 0 here because length is correct and letters are valid
      letterCounts[i] = Math.max(0, matches);
    }

    // 2) Choose the baseline letter with the highest count
    int baselineIndex = 0;
    for (int i = 1; i < ALPHABET.length; i++) {
      if (letterCounts[i] > letterCounts[baselineIndex]) {
        baselineIndex = i;
      }
    }

    char baselineLetter = ALPHABET[baselineIndex];
    char[] current = repeatCharArray(baselineLetter, length);
    int currentScore = letterCounts[baselineIndex];

    // Remaining counts guide which letters are still plausible overall
    int[] remainingByLetter = letterCounts.clone();

    boolean[] resolved = new boolean[length];
    int resolvedCount = 0;

    // If all positions are baseline already, we're done.
    if (currentScore == length) {
      return String.valueOf(current);
    }

    // 3) Resolve each position with minimal tests, guided by remaining counts
      for (int pos = 0; pos < length; pos++) {
          if (resolved[pos]) continue;

          // Unchanged: early stop if all remaining are baseline
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

          // Unchanged: get current order
          int[] order = orderByCountsDescending(remainingByLetter);
          boolean foundForThisPos = false;
          for (int idx : order) {
              if (idx == baselineIndex) continue;
              if (remainingByLetter[idx] <= 0) continue;

              char candidate = ALPHABET[idx];
              char original = current[pos];
              current[pos] = candidate;
              int newScore = callGuess(code, String.valueOf(current));

              if (newScore > currentScore) {
                  // Unchanged: correct candidate
                  currentScore = newScore;
                  resolved[pos] = true;
                  resolvedCount++;
                  remainingByLetter[idx]--;
                  foundForThisPos = true;
                  break;
              } else if (newScore < currentScore) {
                  // NEW: Detects baseline position early (original was correct)
                  current[pos] = original; // Revert
                  resolved[pos] = true;
                  resolvedCount++;
                  remainingByLetter[baselineIndex]--;
                  foundForThisPos = true;
                  break; // Stop trying more for this pos
              } else {
                  // Unchanged: wrong, revert and continue
                  current[pos] = original;
              }
          }

          if (!foundForThisPos) {
              // Unchanged: assume baseline (only reaches here if no -1 or +1, i.e., all 0 deltas and exhausted candidates)
              resolved[pos] = true;
              resolvedCount++;
              remainingByLetter[baselineIndex]--;
          }

          if (currentScore == length) {
              return String.valueOf(current);
          }

    }

    // Final confirmation guess to trigger success print if not already triggered
    callGuess(code, String.valueOf(current));
    return String.valueOf(current);
  }

  private int callGuess(SecretCode code, String guess) {
    localGuessCount++;
    return code.guess(guess);
  }

  private String repeatChar(char c, int length) {
    return String.valueOf(repeatCharArray(c, length));
  }

  private char[] repeatCharArray(char c, int length) {
    char[] arr = new char[length];
    for (int i = 0; i < length; i++) arr[i] = c;
    return arr;
  }

  private int[] indicesSortedByDescendingCounts(int[] counts) {
    // Deprecated; kept for compatibility if referenced elsewhere
    return orderByCountsDescending(counts);
  }

  private int[] orderByCountsDescending(int[] counts) {
    int k = counts.length;
    int[] order = new int[k];
    boolean[] used = new boolean[k];
    for (int r = 0; r < k; r++) {
      int best = -1;
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
