package com.examportal.model;

public class ExamConfig {
    private int questionCount;
    private int durationSeconds;
    private int passThreshold; // percentage 0-100

    public ExamConfig() {
        this.questionCount = 10;
        this.durationSeconds = 60;
        this.passThreshold = 60;
    }

    public ExamConfig(int questionCount, int durationSeconds, int passThreshold) {
        this.questionCount = questionCount;
        this.durationSeconds = durationSeconds;
        this.passThreshold = passThreshold;
    }

    public int getQuestionCount()                    { return questionCount; }
    public void setQuestionCount(int questionCount)  { this.questionCount = questionCount; }

    public int getDurationSeconds()                      { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds)  { this.durationSeconds = durationSeconds; }

    public int getPassThreshold()                    { return passThreshold; }
    public void setPassThreshold(int passThreshold)  { this.passThreshold = passThreshold; }
}
