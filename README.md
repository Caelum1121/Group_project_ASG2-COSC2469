# Secret Code Guessing Algorithm

📺 **Youtube Presentation**: [Watch here](https://www.youtube.com/watch?v=AQXBaPAem3I)  


🗓️ **Project Duration**:  
Start: 2025/08/05 <br><br>
End: 2025/09/05

## 👥 Team Members:

| Student Name  | Student Number |
| :----------------   | :------: |
| Chang Fang Cih      | s4073761 |
| Do Minh Thinh       | s4022154 |
| Nguyen Ngan Anh     | s4103086 |
| Tran Hoang Nguyen   | s4054071 |

## 📌 About:
This is the Group Project for **Algorithms and Data Structures** <br>
Campus: RMIT Saigon South (SG) <br>
Lecturer: Huo Chong Ling

## 🧭 Assignment Overview
This repository contains the group project for:
- COSC2469 Algorithms and Analysis
- COSC2658 Data Structures and Algorithms

**Group Project (30%)**: Design and implement an efficient algorithm to guess a hidden secret code using only positional feedback. The secret code:
- Uses characters from the set {B, A, C, X, I, U}
- Has unknown length (≤ 18)
- Can only be probed via `guess()` which returns the number of correctly positioned characters

Your algorithm must first determine the code length, then discover the full code, minimizing both the total number of guesses and execution time. You may not inspect or modify `SecretCode`. Implement your strategy in `SecretCodeGuesser.start()`.

Key constraints and expectations:
- Do not use Java Collections Framework classes or external libraries to solve the core problem
- Use AI tools responsibly; verify, adapt, and document AI suggestions
- Efficiency is measured by total `guess()` calls and runtime over three test runs

## 🧪 Algorithm Overview (What the program does)
`SecretCodeGuesser` implements a two-phase strategy to recover the hidden code using only positional feedback from `SecretCode.guess(String)`:

1) Length Discovery:
- Probe strings of increasing length (1 → 18) using uniform 'B's until `guess()` no longer returns -2 (invalid length). Cache that first valid score.

2) Code Deduction via Differential Feedback:
- Measure per-letter exact-position counts by guessing uniform strings (e.g., "BBBB…", "AAAA…", …). This estimates how many positions each letter occupies.
- Select a baseline letter with the highest count and initialize the working string to that letter.
- For each unresolved position, try candidate letters ordered by their remaining counts. Use the score change from `guess()` to confirm whether the candidate or the baseline is correct at that position.
- Stop early when all positions are resolved. Print the discovered code, total guesses (printed by `SecretCode` when solved), and total execution time.

Key ideas:
- Greedy baseline selection reduces the number of tests needed per position.
- Differential feedback (score increases/decreases) confirms correctness per position with minimal extra guesses.

## ▶️ How to Run
Prerequisites:
- JDK 21 or later installed and on your PATH
- macOS/Linux shell (commands below use zsh/bash)

Compile:
```bash
javac -d out src/SecretCode.java src/SecretCodeGuesser.java
```

Run (the entry point is `SecretCode.main`, which calls `SecretCodeGuesser.start()`):
```bash
java -cp out SecretCode
```

Expected console output includes the discovered secret code, total number of guesses, and total execution time.

## 📂 Files
- `src/SecretCode.java`: Simulator of the hidden code and feedback function `guess(String)`. Do not modify.
- `src/SecretCodeGuesser.java`: Implement your algorithm in `start()`.

## 🔢 Contribution:
Total Members: 4 (20 points)

| Student Name       | Score  |
|--------------------|--------|
| Chang Fang Cih     |    5   |
| Nguyen Ngan Anh    |    5   |
| Do Minh Thinh      |    5   |
| Tran Hoang Nguyen  |    5   |
