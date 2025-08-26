public class SecretCode {
  private String correctCode;
  private long counter;

  public SecretCode() {
    // for the real test, your program will not know this
    correctCode = "BACXIUBACXIU"; //BACXIUBACXIU
    counter = 0;
  }

  // Returns 
  // -2 : if length of guessedCode is wrong
  // -1 : if guessedCode contains invalid characters
  // >=0 : number of correct characters in correct positions
  public int guess(String guessedCode) {
    //Every time you call guess, counter++ happens.
    counter++;
    // validation
    for (int i = 0; i < guessedCode.length(); i++) {
      char c = guessedCode.charAt(i);
      if (c != 'B' && c != 'A' && c != 'C' && c != 'X' && c != 'I' && c != 'U') {  
       return -1;
      }
    }

    if (guessedCode.length() != correctCode.length()) {
      return -2;
    }
    
    int matched = 0;
    for(int i=0; i < correctCode.length(); i++){
      if(guessedCode.charAt(i) == correctCode.charAt(i)){
        matched++;
      }
    }  

    //If your guess exactly matches correctCode
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
      System.out.println("Time taken: " + (t2-t1) + " ms\n");

      System.out.println("=== OptimizedGuesser ===");
      long t3 = System.currentTimeMillis();
      new OptimizedGuesser().start();
      long t4 = System.currentTimeMillis();
      System.out.println("Time taken: " + (t4-t3) + " ms\n");

  }
}
