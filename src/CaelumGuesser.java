public class CaelumGuesser {

    private static final char[] LETTERS = {'B','A','C','X','I','U'};
    private int totalGuesses = 0;

    public void start() {
        SecretCode code = new SecretCode();
        long startTime = System.currentTimeMillis();

        // === 1️⃣
        int length = findLength(code);

        // === 2️⃣
        int[] freq = new int[LETTERS.length];
        for (int i = 0; i < LETTERS.length; i++) {
            String guess = repeatChar(LETTERS[i], length);
            int correct = code.guess(guess);
            totalGuesses++;
            freq[i] = correct;
        }

        // === 3️⃣
        char[] result = new char[length];
        boolean[] filled = new boolean[length];
        int baseIdx = 0;
        for (int i = 1; i < LETTERS.length; i++) {
            if (freq[i] > freq[baseIdx]) baseIdx = i;
        }
        for (int i = 0; i < length; i++) result[i] = LETTERS[baseIdx];
        int baseScore = freq[baseIdx];

        for (int i = 0; i < LETTERS.length; i++) {
            if (i == baseIdx || freq[i] == 0) continue;
            for (int pos = 0; pos < length && freq[i] > 0; pos++) {
                if (filled[pos]) continue;
                char old = result[pos];
                result[pos] = LETTERS[i];
                int correct = code.guess(new String(result));
                totalGuesses++;
                if (correct > baseScore) {
                    baseScore = correct;
                    filled[pos] = true;
                    freq[i]--;
                } else {
                    result[pos] = old; // 還原
                }
            }
        }

        // === output result
        long endTime = System.currentTimeMillis();
        System.out.println("Secret code: " + new String(result));
        System.out.println("Guesses used: " + totalGuesses);
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
    }


    private int findLength(SecretCode code) {
        for (int l = 1; l <= 18; l++) {
            String guess = repeatChar('A', l);
            int res = code.guess(guess);
            totalGuesses++;
            if (res != -2) return l;
        }
        throw new RuntimeException("Length not found!");
    }

    private String repeatChar(char c, int n) {
        char[] arr = new char[n];
        for (int i = 0; i < n; i++) arr[i] = c;
        return new String(arr);
    }
}
