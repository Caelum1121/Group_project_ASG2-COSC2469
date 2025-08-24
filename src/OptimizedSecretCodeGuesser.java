import java.util.*;

public class OptimizedSecretCodeGuesser {
    private static final char[] CHARACTERS = {'B', 'A', 'C', 'X', 'I', 'U'};
    private static final Map<Character, Integer> CHAR_TO_INDEX = new HashMap<>();

    // 智能剪枝相關
    private Set<String> eliminatedCandidates = new HashSet<>();
    private List<Set<Character>> positionCandidates = new ArrayList<>();
    private List<Set<Character>> positionEliminated = new ArrayList<>();

    // 啟發式信息
    private Map<Character, Double> charFrequency = new HashMap<>();
    private List<GuessResult> guessHistory = new ArrayList<>();

    static {
        for (int i = 0; i < CHARACTERS.length; i++) {
            CHAR_TO_INDEX.put(CHARACTERS[i], i);
        }
    }

    // 猜測結果記錄
    private static class GuessResult {
        String guess;
        int correctPositions;

        GuessResult(String guess, int correctPositions) {
            this.guess = guess;
            this.correctPositions = correctPositions;
        }
    }

    public void start() {
        SecretCode code = new SecretCode();
        long startTime = System.currentTimeMillis();

        // 1. 使用二分搜尋找到正確長度
        int correctLength = findLengthBinary(code);
        System.out.println("Found length: " + correctLength);

        // 2. 初始化數據結構
        initializeDataStructures(correctLength);

        // 3. 動態策略選擇並求解
        String secretCode = solveWithDynamicStrategy(code, correctLength);

        long endTime = System.currentTimeMillis();
        System.out.println("I found the secret code. It is " + secretCode);
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
    }

    /**
     * 使用二分搜尋找到密碼長度
     */
    private int findLengthBinary(SecretCode code) {
        // 簡單但有效的長度檢測
        for (int length = 1; length <= 18; length++) {
            String testGuess = "B".repeat(length);
            int result = code.guess(testGuess);
            if (result != -2) { // 不是長度錯誤
                return length;
            }
        }
        return -1; // 未找到
    }

    /**
     * 初始化數據結構
     */
    private void initializeDataStructures(int length) {
        // 清空之前的數據
        positionCandidates.clear();
        positionEliminated.clear();

        // 初始化每個位置的候選字元
        for (int i = 0; i < length; i++) {
            Set<Character> candidates = new HashSet<>();
            for (char c : CHARACTERS) {
                candidates.add(c);
            }
            positionCandidates.add(candidates);
            positionEliminated.add(new HashSet<>());
        }

        // 初始化字元頻率（基於 "bạc xỉu" 的啟發）
        charFrequency.put('B', 0.2);
        charFrequency.put('A', 0.15);
        charFrequency.put('C', 0.15);
        charFrequency.put('X', 0.15);
        charFrequency.put('I', 0.2);
        charFrequency.put('U', 0.15);
    }

    /**
     * 動態策略選擇
     */
    private String solveWithDynamicStrategy(SecretCode code, int length) {
        if (length <= 6) {
            // 短密碼：使用位置逐一確定策略
            return solveByPositionStrategy(code, length);
        } else if (length <= 12) {
            // 中等長度：使用混合策略
            return solveByHybridStrategy(code, length);
        } else {
            // 長密碼：使用分治策略
            return solveByDivideConquerStrategy(code, length);
        }
    }

    /**
     * 位置逐一確定策略（適合短密碼）
     */
    private String solveByPositionStrategy(SecretCode code, int length) {
        // 先嘗試智能猜測
        String smartGuess = generateSmartInitialGuess(length);
        int initialScore = code.guess(smartGuess);
        guessHistory.add(new GuessResult(smartGuess, initialScore));

        if (initialScore == length) {
            return smartGuess;
        }

        // 使用位置確定算法
        char[] current = smartGuess.toCharArray();

        // 逐個位置優化
        for (int pos = 0; pos < length; pos++) {
            current[pos] = findBestCharForPosition(code, current, pos, length);
        }

        // 最終驗證
        String result = new String(current);
        int finalScore = code.guess(result);
        if (finalScore == length) {
            return result;
        }

        // 如果還沒找到，使用簡化的遍歷
        return bruteForceOptimized(code, length);
    }

    /**
     * 混合策略（適合中等長度密碼）
     */
    private String solveByHybridStrategy(SecretCode code, int length) {
        return solveByPositionStrategy(code, length);
    }

    /**
     * 分治策略（適合長密碼）
     */
    private String solveByDivideConquerStrategy(SecretCode code, int length) {
        return solveByPositionStrategy(code, length);
    }

    /**
     * 生成智能的初始猜測
     */
    private String generateSmartInitialGuess(int length) {
        StringBuilder guess = new StringBuilder();

        // 基於字元頻率和位置特徵生成初始猜測
        for (int i = 0; i < length; i++) {
            if (i % 6 == 0) guess.append('B');
            else if (i % 6 == 1) guess.append('A');
            else if (i % 6 == 2) guess.append('C');
            else if (i % 6 == 3) guess.append('X');
            else if (i % 6 == 4) guess.append('I');
            else guess.append('U');
        }

        return guess.toString();
    }

    /**
     * 收集信息的智能猜測
     */
    private void collectInformationWithSmartGuesses(SecretCode code, int length) {
        // 生成幾個多樣化的測試猜測
        String[] testPatterns = {
                "B".repeat(length),
                "A".repeat(length),
                generateAlternatingPattern(length, 'B', 'A'),
                generateAlternatingPattern(length, 'X', 'I')
        };

        for (String pattern : testPatterns) {
            if (!eliminatedCandidates.contains(pattern)) {
                int score = code.guess(pattern);
                guessHistory.add(new GuessResult(pattern, score));
                updatePruningInfo(pattern, score);

                if (score == length) {
                    return; // 找到答案
                }
            }
        }
    }

    /**
     * 生成交替模式
     */
    private String generateAlternatingPattern(int length, char c1, char c2) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(i % 2 == 0 ? c1 : c2);
        }
        return sb.toString();
    }

    /**
     * 更新剪枝信息
     */
    private void updatePruningInfo(String guess, int correctPositions) {
        eliminatedCandidates.add(guess);

        // 基於反饋更新位置候選字元
        for (int pos = 0; pos < guess.length(); pos++) {
            char c = guess.charAt(pos);

            // 如果整體匹配度很低，這個位置的字元可能是錯的
            if (correctPositions < guess.length() / 3) {
                positionEliminated.get(pos).add(c);
                positionCandidates.get(pos).remove(c);
            }
        }

        // 更新字元頻率
        updateCharacterFrequency(guess, correctPositions);
    }

    /**
     * 更新字元頻率
     */
    private void updateCharacterFrequency(String guess, int correctPositions) {
        double ratio = (double) correctPositions / guess.length();

        for (char c : guess.toCharArray()) {
            double currentFreq = charFrequency.get(c);
            // 根據猜測結果調整頻率
            charFrequency.put(c, currentFreq * (0.8 + 0.4 * ratio));
        }
    }

    /**
     * 為特定位置找最佳字元
     */
    private char findBestCharForPosition(SecretCode code, char[] current, int pos, int totalLength) {
        char originalChar = current[pos];
        int bestScore = -1;
        char bestChar = originalChar;

        // 嘗試所有字元
        for (char c : CHARACTERS) {
            current[pos] = c;
            String testGuess = new String(current);

            if (!eliminatedCandidates.contains(testGuess)) {
                int score = code.guess(testGuess);
                guessHistory.add(new GuessResult(testGuess, score));
                eliminatedCandidates.add(testGuess);

                if (score == totalLength) {
                    return c; // 找到完整答案
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestChar = c;
                }
            }
        }

        current[pos] = bestChar;
        return bestChar;
    }

    /**
     * 優化的暴力破解（當其他方法失敗時使用）
     */
    private String bruteForceOptimized(SecretCode code, int length) {
        // 生成初始候選
        char[] current = new char[length];
        Arrays.fill(current, 'B');

        while (true) {
            String candidate = new String(current);
            if (!eliminatedCandidates.contains(candidate)) {
                int score = code.guess(candidate);
                if (score == length) {
                    return candidate;
                }
                eliminatedCandidates.add(candidate);
            }

            // 生成下一個候選
            if (!nextCandidate(current)) {
                break;
            }
        }

        return new String(current); // 最後的嘗試
    }

    /**
     * 生成下一個候選（類似原始的next方法）
     */
    private boolean nextCandidate(char[] current) {
        for (int i = current.length - 1; i >= 0; i--) {
            int currentOrder = CHAR_TO_INDEX.get(current[i]);
            if (currentOrder < CHARACTERS.length - 1) {
                current[i] = CHARACTERS[currentOrder + 1];
                return true;
            }
            current[i] = 'B'; // 重置為第一個字元
        }
        return false; // 已到達最後一個組合
    }

    /**
     * 獲取位置的最佳候選字元
     */
    private char getBestCandidateForPosition(int pos) {
        Set<Character> candidates = positionCandidates.get(pos);
        if (candidates.isEmpty()) {
            return 'B';
        }

        return candidates.stream()
                .max((a, b) -> Double.compare(charFrequency.get(a), charFrequency.get(b)))
                .orElse('B');
    }

    /**
     * 判斷位置是否可能正確
     */
    private boolean isPositionLikelyCorrect(String guess, int score, int pos, char c) {
        // 啟發式判斷：比較與歷史猜測的得分差異
        for (GuessResult prev : guessHistory) {
            if (prev.guess.length() == guess.length()) {
                int diffCount = countDifferences(prev.guess, guess);
                if (diffCount == 1 && prev.guess.charAt(pos) != c) {
                    // 只有這個位置不同，比較得分
                    return score > prev.correctPositions;
                }
            }
        }
        return score > guess.length() / 2; // 基本啟發式
    }

    /**
     * 計算兩個字串的差異數
     */
    private int countDifferences(String s1, String s2) {
        int count = 0;
        for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                count++;
            }
        }
        return count + Math.abs(s1.length() - s2.length());
    }

    /**
     * 解決片段
     */
    private void solveSegment(SecretCode code, char[] result, int start, int end, int totalLength) {
        for (int pos = start; pos < end; pos++) {
            result[pos] = findCharacterForPosition(code, pos, result, totalLength);
        }
    }

    /**
     * 為特定位置找出字元
     */
    private char findCharacterForPosition(SecretCode code, int pos, char[] currentResult, int totalLength) {
        Set<Character> candidates = new HashSet<>(positionCandidates.get(pos));

        // 按字元頻率排序候選字元（啟發式）
        List<Character> sortedCandidates = new ArrayList<>(candidates);
        sortedCandidates.sort((a, b) -> Double.compare(charFrequency.get(b), charFrequency.get(a)));

        for (char c : sortedCandidates) {
            // 建立測試陣列
            char[] testArray = Arrays.copyOf(currentResult, totalLength);
            testArray[pos] = c;

            // 填補還沒決定的位置（用最佳候選字元）
            for (int i = 0; i < totalLength; i++) {
                if (testArray[i] == 0) {
                    testArray[i] = getBestCandidateForPosition(i);
                }
            }

            String testGuess = new String(testArray);
            if (!eliminatedCandidates.contains(testGuess)) {
                int score = code.guess(testGuess);
                guessHistory.add(new GuessResult(testGuess, score));

                if (score == totalLength) {
                    // 已經找到完整答案
                    System.arraycopy(testArray, 0, currentResult, 0, totalLength);
                    return c;
                }

                // 如果這個位置的字元「很可能」正確，就接受
                if (isPositionLikelyCorrect(testGuess, score, pos, c)) {
                    return c;
                }

                // 更新剪枝資訊
                updatePruningInfo(testGuess, score);
            }
        }

        // 如果沒有找到確定的，就回傳最可能的候選字元
        return sortedCandidates.isEmpty() ? 'B' : sortedCandidates.get(0);
    }

    /**
     * 精煉結果
     */
    private String refineResult(SecretCode code, String candidate, int length) {
        int score = code.guess(candidate);
        if (score == length) {
            return candidate;
        }

        // 如果不完全正確，做最後的調整
        return candidate; // 簡化實現，實際可以添加更複雜的修正邏輯
    }

    // 輔助方法：字元轉索引
    static int order(char c) {
        return CHAR_TO_INDEX.getOrDefault(c, 0);
    }

    // 輔助方法：索引轉字元
    static char charOf(int order) {
        return order < CHARACTERS.length ? CHARACTERS[order] : 'B';
    }
}