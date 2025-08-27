public class SecretCodeGuesser {

  private static final char[] ALPHABET = new char[] {'B', 'A', 'C', 'X', 'I', 'U'};
  private int lastBMatchesAtCorrectLength = -1;

  public void start() {
    SecretCode code = new SecretCode();

    // First find out how long the secret code is
    int length = determineLength(code);
    if (length <= 0) {
      System.out.println("Failed to determine secret code length.");
      return;
    }

    System.out.println("Found length: " + length);

    // Now figure out what the actual code is
    String found = deduceCode(code, length);
    System.out.println("I found the secret code. It is " + found);
  }

  private int determineLength(SecretCode code) {
    // Try different lengths from 0 to 18 inclusive
    for (int len = 0; len <= 18; len++) {
      String candidate = repeatChar('B', len);
      int result = callGuess(code, candidate);
      if (result != -2) {
        // Found the right length - save this result for later
        lastBMatchesAtCorrectLength = result;
        return len;
      }
    }

    return -1;
  }

  private String deduceCode(SecretCode code, int length) {
    // First, test each letter to see how many times it appears
    int[] letterCounts = new int[ALPHABET.length];

    for (int i = 0; i < ALPHABET.length; i++) {
      char letter = ALPHABET[i];
      int matches;
      if (letter == 'B' && lastBMatchesAtCorrectLength >= 0) {
        // We already know this from finding the length
        matches = lastBMatchesAtCorrectLength;
      } else {
        String guess = repeatChar(letter, length);
        matches = callGuess(code, guess);
      }
      letterCounts[i] = Math.max(0, matches);
    }

    // Start with the most common letter as our baseline
    int baselineIndex = 0;
    for (int i = 1; i < ALPHABET.length; i++) {
      if (letterCounts[i] > letterCounts[baselineIndex]) {
        baselineIndex = i;
      }
    }

    char baselineLetter = ALPHABET[baselineIndex];
    char[] current = repeatCharArray(baselineLetter, length);
    int currentScore = letterCounts[baselineIndex];

    int[] remainingByLetter = letterCounts.clone();
    boolean[] resolved = new boolean[length];
    int resolvedCount = 0;

    // If it's all the same letter, we're done
    if (currentScore == length) {
      return String.valueOf(current);
    }

    // Go through each position and figure out what letter belongs there
    for (int pos = 0; pos < length; pos++) {
      if (resolved[pos]) continue;

      // Quick check: if remaining positions = remaining baseline letters, we're done
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

      // Try other letters in order of how often they appear
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
          // This letter works here
          currentScore = newScore;
          resolved[pos] = true;
          resolvedCount++;
          remainingByLetter[idx]--;
          foundForThisPos = true;
          break;
        }

        current[pos] = original;
      }

      if (!foundForThisPos) {
        // Must be the baseline letter
        resolved[pos] = true;
        resolvedCount++;
        remainingByLetter[baselineIndex]--;
      }

      if (currentScore == length) {
        return String.valueOf(current);
      }
    }

    // Just in case, make one final guess
    callGuess(code, String.valueOf(current));
    return String.valueOf(current);
  }

  private int callGuess(SecretCode code, String guess) {
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

  private int[] orderByCountsDescending(int[] counts) {
    int k = counts.length;
    int[] order = new int[k];
    boolean[] used = new boolean[k];

    // Simple selection sort to get indices in descending order
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