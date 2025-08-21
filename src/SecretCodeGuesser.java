public class SecretCodeGuesser{

  public void start() {
    // Efficient secret code guessing using differential feedback + letter counts
    SecretCode code = new SecretCode();

    int correctLength = -1;
    int baseMatches = -1; // matches for the current baseline guess

    // 1) Determine correct length by probing; length <= 18 per spec
    for (int length = 1; length <= 18; length++) {
      String candidate = "B".repeat(length);
      int result = code.guess(candidate);
      if (result != -2) { // equal length detected
        correctLength = length;
        baseMatches = result; // number of 'B' positions already correct

        // 2) Determine frequency of each letter (each all-same guess counts exact positions)
        int[] remaining = new int[6]; // order: B,A,C,X,I,U
        remaining[0] = baseMatches; // 'B'
        remaining[1] = code.guess(repeatChar('A', correctLength));
        remaining[2] = code.guess(repeatChar('C', correctLength));
        remaining[3] = code.guess(repeatChar('X', correctLength));
        remaining[4] = code.guess(repeatChar('I', correctLength));
        remaining[5] = code.guess(repeatChar('U', correctLength));

        // Prepare baseline guess: start with all 'B'
        char[] baseline = new char[correctLength];
        for (int i = 0; i < correctLength; i++) baseline[i] = 'B';

        // If fully solved already
        if (baseMatches == correctLength) {
          System.out.println("I found the secret code. It is " + new String(baseline));
          return;
        }

        boolean[] resolved = new boolean[correctLength];

        // 3) Resolve each position using remaining counts to prune trials
        for (int i = 0; i < correctLength; i++) {
          if (resolved[i]) continue;

          int baseIdx = indexOfLetter(baseline[i]);

          int attempts = 0;
          while (!resolved[i]) {
            int nextLetterIdx = pickNextLetterIndexByRemaining(remaining, baseIdx);
            if (nextLetterIdx == -1) {
              // No other candidates with remaining > 0; baseline must be correct
              resolved[i] = true;
              remaining[baseIdx]--;
              break;
            }

            char trial = charOfIndex(nextLetterIdx);

            // Build guess differing only at i
            char oldChar = baseline[i];
            if (trial == oldChar) {
              // Skip redundant
              remaining[nextLetterIdx]--; // avoid looping forever; treat as consumed
              continue;
            }

            char[] temp = copyOf(baseline);
            temp[i] = trial;
            int res = code.guess(new String(temp));

            if (res == correctLength) {
              System.out.println("I found the secret code. It is " + new String(temp));
              return;
            }

            if (res > baseMatches) {
              // trial letter is correct for this position
              baseline[i] = trial;
              baseMatches = res;
              remaining[nextLetterIdx]--;
              resolved[i] = true;
            } else if (res < baseMatches) {
              // baseline letter was correct
              remaining[baseIdx]--;
              resolved[i] = true;
            } else {
              // equal: trial is not correct; consume one from remaining to avoid re-picking soon
              remaining[nextLetterIdx]--;
              attempts++;
              // If all other candidates exhausted, baseline must be correct
              if (!hasAlternativeCandidate(remaining, baseIdx)) {
                remaining[baseIdx]--;
                resolved[i] = true;
              }
            }
          }
        }

        // Final verification to trigger official guess count and ensure correctness
        int finalRes = code.guess(new String(baseline));
        if (finalRes == correctLength) {
          System.out.println("I found the secret code. It is " + new String(baseline));
        } else {
          System.out.println("Failed to fully resolve the secret code.");
        }
        return;
      }
    }

    System.out.println("Failed to determine secret code length.");
  }

  private int indexOfLetter(char c) {
    if (c == 'B') return 0;
    if (c == 'A') return 1;
    if (c == 'C') return 2;
    if (c == 'X') return 3;
    if (c == 'I') return 4;
    return 5; // 'U'
  }

  private char charOfIndex(int idx) {
    if (idx == 0) return 'B';
    if (idx == 1) return 'A';
    if (idx == 2) return 'C';
    if (idx == 3) return 'X';
    if (idx == 4) return 'I';
    return 'U';
  }

  private boolean hasAlternativeCandidate(int[] remaining, int baseIdx) {
    for (int i = 0; i < remaining.length; i++) {
      if (i != baseIdx && remaining[i] > 0) return true;
    }
    return false;
  }

  private int pickNextLetterIndexByRemaining(int[] remaining, int baseIdx) {
    // pick non-base with max remaining
    int bestIdx = -1;
    int bestVal = -1;
    for (int i = 0; i < remaining.length; i++) {
      if (i == baseIdx) continue;
      if (remaining[i] > bestVal) {
        bestVal = remaining[i];
        bestIdx = i;
      }
    }
    if (bestVal <= 0) return -1;
    return bestIdx;
  }

  private String repeatChar(char c, int n) {
    char[] arr = new char[n];
    for (int i = 0; i < n; i++) arr[i] = c;
    return new String(arr);
  }

  private char[] copyOf(char[] arr) {
    char[] out = new char[arr.length];
    for (int i = 0; i < arr.length; i++) out[i] = arr[i];
    return out;
  }

  static int order(char c) {
    if (c == 'B') {
      return 0;
    } else if (c == 'A') {
      return 1;
    } else if (c == 'C') {
      return 2;
    } else if (c == 'X') {
      return 3;
    } else if (c == 'I') {
      return 4;
    } 
    return 5;
  }

  static char charOf(int order) {
    if (order == 0) {
      return 'B';
    } else if (order == 1) {
      return 'A';
    } else if (order == 2) {
      return 'C';
    } else if (order == 3) {
      return 'X';
    } else if (order == 4) {
      return 'I';
    } 
    return 'U';
  }

  // return the next value in 'BACXIU' order, that is
  // B < A < C < X < I < U
  public String next(String current) {
    char[] curr = current.toCharArray();
    for (int i = curr.length - 1; i >=0; i--) {
      if (order(curr[i]) < 5) {
        // increase this one and stop
        curr[i] = charOf(order(curr[i]) + 1);
        break;
      }
      curr[i] = 'B';
    }
    return String.valueOf(curr);
  }  
}
