package com.examportal.model;

public class Question {
    private String id;
    private String questionText;
    private String[] options;
    private int correctOption; // 1-indexed
    private String category;  // e.g. Java, SQL, OS
    private String difficulty; // Easy, Medium, Hard

    public Question() {}

    public Question(String id, String questionText, String[] options, int correctOption) {
        this.id = id;
        this.questionText = questionText;
        this.options = options;
        this.correctOption = correctOption;
        this.category = "General";
        this.difficulty = "Medium";
    }

    public Question(String id, String questionText, String[] options, int correctOption,
                    String category, String difficulty) {
        this.id = id;
        this.questionText = questionText;
        this.options = options;
        this.correctOption = correctOption;
        this.category = category != null && !category.isBlank() ? category : "General";
        this.difficulty = difficulty != null && !difficulty.isBlank() ? difficulty : "Medium";
    }

    public String getId()                   { return id; }
    public void setId(String id)            { this.id = id; }

    public String getQuestionText()                  { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String[] getOptions()               { return options; }
    public void setOptions(String[] options)   { this.options = options; }

    public int getCorrectOption()                    { return correctOption; }
    public void setCorrectOption(int correctOption)  { this.correctOption = correctOption; }

    public String getCategory()                  { return category != null ? category : "General"; }
    public void setCategory(String category)     { this.category = category; }

    public String getDifficulty()                { return difficulty != null ? difficulty : "Medium"; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public boolean isCorrect(int userAnswer) {
        return userAnswer == correctOption;
    }

    // Returns a safe copy without revealing the correct option (for exam delivery)
    public QuestionDTO toDTO() {
        return new QuestionDTO(id, questionText, options, category, difficulty);
    }

    public static class QuestionDTO {
        private String id;
        private String questionText;
        private String[] options;
        private String category;
        private String difficulty;

        public QuestionDTO(String id, String questionText, String[] options,
                           String category, String difficulty) {
            this.id = id;
            this.questionText = questionText;
            this.options = options;
            this.category = category;
            this.difficulty = difficulty;
        }

        public String getId()             { return id; }
        public String getQuestionText()   { return questionText; }
        public String[] getOptions()      { return options; }
        public String getCategory()       { return category; }
        public String getDifficulty()     { return difficulty; }
    }
}
