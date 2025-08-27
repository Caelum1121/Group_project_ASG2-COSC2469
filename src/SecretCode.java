public class SecretCode {
    private String correctCode;
    private long counter;

    public SecretCode() {
        // In the real test, your program won't know this
        correctCode = "ABIXCIABCX";
        counter = 0;
    }

    // Let other classes see the correct code (for testing purposes)
    public String getCorrectCode() {
        return this.correctCode;
    }

    // Returns different values based on the guess:
    // -2 : wrong length
    // -1 : invalid characters
    // >=0 : number of correct characters in correct positions
    public int guess(String guessedCode) {
        counter++;

        // Check for invalid characters
        for (int i = 0; i < guessedCode.length(); i++) {
            char c = guessedCode.charAt(i);
            if (c != 'B' && c != 'A' && c != 'C' && c != 'X' && c != 'I' && c != 'U') {
                return -1;
            }
        }

        // Check length
        if (guessedCode.length() != correctCode.length()) {
            return -2;
        }

        // Count how many characters are in the right position
        int matched = 0;
        for (int i = 0; i < correctCode.length(); i++) {
            if (guessedCode.charAt(i) == correctCode.charAt(i)) {
                matched++;
            }
        }

        // Print the guess count when we find the answer
        if (matched == correctCode.length()) {
            System.out.println("Number of guesses: " + counter);
        }
        return matched;
    }

    public static void main(String[] args) {
        System.out.println("=== SecretCodeGuesser ===");
        long t1 = System.currentTimeMillis();
        new SecretCodeGuesser().start();
        long t2 = System.currentTimeMillis();
        System.out.println("Time taken: " + (t2 - t1) + " ms\n");

        System.out.println("=== OptimizedGuesser ===");
        long t3 = System.currentTimeMillis();
        new OptimizedGuesser().start();
        long t4 = System.currentTimeMillis();
        System.out.println("Time taken: " + (t4 - t3) + " ms\n");
    }
}