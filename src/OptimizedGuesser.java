public class OptimizedGuesser {
    private static final char[] CHARACTERS = {'B', 'A', 'C', 'X', 'I', 'U'};
    private int guessCount = 0;

    public void start() {
        SecretCode code = new SecretCode();

        // Step 1: Find the length (1-2 guesses)
        int length = findLength(code);
        System.out.println("Found length: " + length);

        // Step 2: Try single character patterns (max 6 guesses)
        String singleResult = testSingleCharPatterns(code, length);
        if (singleResult != null) {
            System.out.println("I found the secret code. It is " + singleResult);
            System.out.println("Total guesses made: " + guessCount);
            return;
        }

        // Step 3: Determine character frequencies (exactly 6 guesses)
        int[] charCounts = getCharacterCounts(code, length);

        // Step 4: Construct solution directly (1-2 guesses max)
        String solution = constructSolution(code, length, charCounts);

        System.out.println("I found the secret code. It is " + solution);
        System.out.println("Total guesses made: " + guessCount);
    }

    private int findLength(SecretCode code) {
        for (int len = 1; len <= 18; len++) {
            String test = "B".repeat(len);
            int result = makeGuess(code, test);
            if (result != -2) {
                return len;
            }
        }
        return 18;
    }

    private String testSingleCharPatterns(SecretCode code, int length) {
        System.out.println("Testing single character patterns.");
        for (char c : CHARACTERS) {
            String candidate = String.valueOf(c).repeat(length);
            int score = makeGuess(code, candidate);
            if (score == length) {
                return candidate;
            }
        }
        return null;
    }

    private int[] getCharacterCounts(SecretCode code, int length) {
        System.out.println("Getting character frequencies.");
        int[] counts = new int[CHARACTERS.length];

        for (int i = 0; i < CHARACTERS.length; i++) {
            String test = String.valueOf(CHARACTERS[i]).repeat(length);
            counts[i] = makeGuess(code, test);
            System.out.println("'" + CHARACTERS[i] + "' appears " + counts[i] + " times");
        }

        return counts;
    }

    private String constructSolution(SecretCode code, int length, int[] charCounts) {
        System.out.println("Constructing solution.");
        StringBuilder candidate = new StringBuilder();

        // Create an array of characters based on the frequency of each character
        for (int i = 0; i < CHARACTERS.length; i++) {
            if (charCounts[i] > 0) {
                candidate.append(String.valueOf(CHARACTERS[i]).repeat(charCounts[i]));
            }
        }

        // Now, rearrange the string to match the actual code order
        String solution = rearrangeToMatchPattern(candidate.toString(), code);

        // If the solution length matches, check if it's the correct guess
        if (solution.length() == length) {
            int score = makeGuess(code, solution);
            if (score == length) {
                return solution;
            }
        }

        // If the direct solution fails, try permutations
        return tryLimitedPermutations(code, length, candidate.toString());
    }

    private String rearrangeToMatchPattern(String candidate, SecretCode code) {
        // Rearranging the candidate to match the actual code order
        char[] candidateArr = candidate.toCharArray();
        String correctCode = code.getCorrectCode(); // Now this method is accessible

        StringBuilder rearranged = new StringBuilder();

        // Place characters in the correct order based on their frequencies and the pattern
        for (int i = 0; i < correctCode.length(); i++) {
            char correctChar = correctCode.charAt(i);
            if (candidate.indexOf(String.valueOf(correctChar)) != -1) {
                rearranged.append(correctChar);
                candidate = candidate.replaceFirst(String.valueOf(correctChar), "");
            }
        }

        return rearranged.toString();
    }

    private String tryLimitedPermutations(SecretCode code, int length, String candidate) {
        // Generate permutations based on the candidate string
        return generatePermutation(code, length, candidate, 0, new boolean[candidate.length()]);
    }

    private String generatePermutation(SecretCode code, int length, String candidate, int pos, boolean[] used) {
        if (pos == candidate.length()) {
            String solution = candidate;
            int score = makeGuess(code, solution);
            if (score == length) {
                return solution;
            }
            return null;
        }

        for (int i = 0; i < candidate.length(); i++) {
            if (!used[i]) {
                used[i] = true;
                String result = generatePermutation(code, length, candidate, pos + 1, used);
                if (result != null) return result;
                used[i] = false;

                // Limit guesses to prevent excessive attempts
                if (guessCount > 15) break;
            }
        }

        return candidate;
    }

    private int makeGuess(SecretCode code, String guess) {
        guessCount++;
        return code.guess(guess);
    }
}
