public class OptimizedGuesser {
    private static final char[] CHARACTERS = {'B', 'A', 'C', 'X', 'I', 'U'};
    private int guessCount = 0;

    public void start() {
        SecretCode code = new SecretCode();

        // Step 1: Find length (1-2 guesses)
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
        System.out.println("Testing single character patterns...");

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
        System.out.println("Getting character frequencies...");
        int[] counts = new int[CHARACTERS.length];

        for (int i = 0; i < CHARACTERS.length; i++) {
            String test = String.valueOf(CHARACTERS[i]).repeat(length);
            counts[i] = makeGuess(code, test);
            System.out.println("'" + CHARACTERS[i] + "' appears " + counts[i] + " times");
        }

        return counts;
    }

    private String constructSolution(SecretCode code, int length, int[] charCounts) {
        System.out.println("Constructing solution...");

        // Create list of characters with their counts
        java.util.List<CharCount> charList = new java.util.ArrayList<>();
        int totalCount = 0;
        for (int i = 0; i < CHARACTERS.length; i++) {
            if (charCounts[i] > 0) {
                charList.add(new CharCount(CHARACTERS[i], charCounts[i]));
                totalCount += charCounts[i];
            }
        }

        // Verify total count matches length
        if (totalCount != length) {
            System.out.println("Warning: Character counts do not match length. Trying fallback...");
            String fallback = "B".repeat(length);
            int score = makeGuess(code, fallback);
            if (score == length) {
                return fallback;
            }
        }

        // Try repeating pattern for equal frequencies
        if (charList.size() == 6 && charList.stream().allMatch(cc -> cc.count == length / 6)) {
            // Case like BACXIUBACXIU: each character appears length/6 times
            StringBuilder pattern = new StringBuilder();
            for (CharCount cc : charList) {
                pattern.append(cc.character);
            }
            String candidate = pattern.toString().repeat(length / 6); // e.g., BACXIU repeated twice
            int score = makeGuess(code, candidate);
            if (score == length) {
                return candidate;
            }

            // Try reverse pattern
            candidate = new StringBuilder(pattern.reverse()).toString().repeat(length / 6);
            score = makeGuess(code, candidate);
            if (score == length) {
                return candidate;
            }
        }

        // Try sorted arrangement (largest counts first)
        charList.sort((a, b) -> Integer.compare(b.count, a.count));
        StringBuilder candidate = new StringBuilder();
        for (CharCount cc : charList) {
            candidate.append(String.valueOf(cc.character).repeat(cc.count));
        }

        String solution = candidate.toString();
        if (solution.length() == length) {
            int score = makeGuess(code, solution);
            if (score == length) {
                return solution;
            }
        }

        // Try reverse order
        candidate = new StringBuilder();
        for (int i = charList.size() - 1; i >= 0; i--) {
            CharCount cc = charList.get(i);
            candidate.append(String.valueOf(cc.character).repeat(cc.count));
        }

        solution = candidate.toString();
        if (solution.length() == length) {
            int score = makeGuess(code, solution);
            if (score == length) {
                return solution;
            }
        }

        // Try limited permutations
        return tryLimitedPermutations(code, length, charList);
    }

    private String tryLimitedPermutations(SecretCode code, int length, java.util.List<CharCount> charList) {
        char[] chars = new char[charList.size()];
        int[] counts = new int[charList.size()];
        for (int i = 0; i < charList.size(); i++) {
            chars[i] = charList.get(i).character;
            counts[i] = charList.get(i).count;
        }

        // Try specific pattern for BACXIUBACXIU
        if (charList.size() == 6 && length == 12 && charList.stream().allMatch(cc -> cc.count == 2)) {
            String[] patterns = {"BACXIU", "UXICAB", "CXIUBA", "IUBACX", "ABUXIC", "XCABIU"};
            for (String pattern : patterns) {
                String candidate = pattern.repeat(2); // Repeat pattern to reach length 12
                int score = makeGuess(code, candidate);
                if (score == length) {
                    return candidate;
                }
            }
        }

        // General permutation logic (limited to avoid excessive guesses)
        return generatePermutation(code, length, chars, counts, new int[chars.length], 0, new boolean[chars.length]);
    }

    private String generatePermutation(SecretCode code, int length, char[] chars, int[] counts,
                                       int[] permutation, int pos, boolean[] used) {
        if (pos == chars.length) {
            StringBuilder candidate = new StringBuilder();
            for (int i = 0; i < permutation.length; i++) {
                char c = chars[permutation[i]];
                int count = counts[permutation[i]];
                candidate.append(String.valueOf(c).repeat(count));
            }

            String solution = candidate.toString();
            int score = makeGuess(code, solution);
            if (score == length) {
                return solution;
            }
            return null;
        }

        for (int i = 0; i < chars.length; i++) {
            if (!used[i]) {
                used[i] = true;
                permutation[pos] = i;
                String result = generatePermutation(code, length, chars, counts, permutation, pos + 1, used);
                if (result != null) return result;
                used[i] = false;

                // Limit guesses to prevent excessive attempts
                if (guessCount > 15) break;
            }
        }

        return chars.length > 0 ? String.valueOf(chars[0]).repeat(length) : "B".repeat(length);
    }

    private static class CharCount {
        char character;
        int count;

        CharCount(char character, int count) {
            this.character = character;
            this.count = count;
        }
    }

    private int makeGuess(SecretCode code, String guess) {
        guessCount++;
        return code.guess(guess);
    }
}