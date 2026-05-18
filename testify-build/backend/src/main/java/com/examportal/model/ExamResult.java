package com.examportal.model;

import java.util.List;

public class ExamResult {
    private String userId;
    private int score;
    private int total;
    private List<QuestionResult> questionResults;
    private int passThreshold;
    private boolean passed;

    public ExamResult() {}

    public ExamResult(String userId, int score, int total,
                      List<QuestionResult> questionResults, int passThreshold) {
        this.userId = userId;
        this.score = score;
        this.total = total;
        this.questionResults = questionResults;
        this.passThreshold = passThreshold;
        this.passed = total > 0 && ((double) score * 100 / total) >= passThreshold;
    }

    public String getUserId()                   { return userId; }
    public void setUserId(String userId)        { this.userId = userId; }

    public int getScore()              { return score; }
    public void setScore(int score)    { this.score = score; }

    public int getTotal()              { return total; }
    public void setTotal(int total)    { this.total = total; }

    public List<QuestionResult> getQuestionResults()                       { return questionResults; }
    public void setQuestionResults(List<QuestionResult> questionResults)   { this.questionResults = questionResults; }

    public int getPassThreshold()                    { return passThreshold; }
    public void setPassThreshold(int passThreshold)  { this.passThreshold = passThreshold; }

    public boolean isPassed()              { return passed; }
    public void setPassed(boolean passed)  { this.passed = passed; }

    public static class QuestionResult {
        private String questionId;
        private String questionText;
        private String[] options;
        private int userAnswer;     // 1-indexed
        private int correctAnswer;  // 1-indexed
        private boolean correct;
        private String category;
        private String difficulty;

        public QuestionResult() {}

        public QuestionResult(String questionId, String questionText, String[] options,
                              int userAnswer, int correctAnswer,
                              String category, String difficulty) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.options = options;
            this.userAnswer = userAnswer;
            this.correctAnswer = correctAnswer;
            this.correct = (userAnswer == correctAnswer);
            this.category = category;
            this.difficulty = difficulty;
        }

        public String getQuestionId()                    { return questionId; }
        public void setQuestionId(String questionId)     { this.questionId = questionId; }

        public String getQuestionText()                  { return questionText; }
        public void setQuestionText(String text)         { this.questionText = text; }

        public String[] getOptions()                     { return options; }
        public void setOptions(String[] options)         { this.options = options; }

        public int getUserAnswer()                       { return userAnswer; }
        public void setUserAnswer(int userAnswer)        { this.userAnswer = userAnswer; }

        public int getCorrectAnswer()                    { return correctAnswer; }
        public void setCorrectAnswer(int correctAnswer)  { this.correctAnswer = correctAnswer; }

        public boolean isCorrect()                       { return correct; }
        public void setCorrect(boolean correct)          { this.correct = correct; }

        public String getCategory()                      { return category; }
        public void setCategory(String category)         { this.category = category; }

        public String getDifficulty()                    { return difficulty; }
        public void setDifficulty(String difficulty)     { this.difficulty = difficulty; }
    }
}
