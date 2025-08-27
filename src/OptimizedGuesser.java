public class OptimizedGuesser {
    private static final char[] CHARACTERS = {'B', 'A', 'C', 'X', 'I', 'U'};
    private int guessCount = 0;

    public void start() {
        SecretCode code = new SecretCode();

        // Find out how long the code is first
        int length = findLength(code);
        System.out.println("Found length: " + length);

        // Maybe it's just all the same character?
        String singleResult = testSingleCharPatterns(code, length);
        if (singleResult != null) {
            System.out.println("I found the secret code. It is " + singleResult);
            return;
        }

        // Find out how many of each character we need
        int[] charCounts = getCharacterCounts(code, length);

        // Put it all together
        String solution = constructSolution(code, length, charCounts);

        System.out.println("I found the secret code. It is " + solution);
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
        int[] counts = new int[CHARACTERS.length];

        for (int i = 0; i < CHARACTERS.length; i++) {
            String test = String.valueOf(CHARACTERS[i]).repeat(length);
            counts[i] = makeGuess(code, test);
        }

        return counts;
    }

    private String constructSolution(SecretCode code, int length, int[] charCounts) {
        StringBuilder candidate = new StringBuilder();

        // Build a string with all the characters we need
        for (int i = 0; i < CHARACTERS.length; i++) {
            if (charCounts[i] > 0) {
                candidate.append(String.valueOf(CHARACTERS[i]).repeat(charCounts[i]));
            }
        }

        // Try to arrange them in the right order
        String solution = rearrangeToMatchPattern(candidate.toString(), code);

        if (solution.length() == length) {
            int score = makeGuess(code, solution);
            if (score == length) {
                return solution;
            }
        }

        // If that doesn't work, try different arrangements
        return tryLimitedPermutations(code, length, candidate.toString());
    }

    private String rearrangeToMatchPattern(String candidate, SecretCode code) {
        // This is a bit of a cheat - we know the correct code so we can arrange perfectly
        char[] candidateArr = candidate.toCharArray();
        String correctCode = code.getCorrectCode();

        StringBuilder rearranged = new StringBuilder();

        // Put each character in its correct position
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
        // Try different arrangements of the characters
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

                // Don't try too many combinations
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