package com.example.elsa_speak_clone.database.entities;

import androidx.room.ColumnInfo;

/**
 * This class represents the combined data from UserScores and Quizzes tables
 */
public class UserScoreWithQuizDetails {
    @ColumnInfo(name = "ScoreId")
    private int scoreId;

    @ColumnInfo(name = "UserId")
    private int userId;

    @ColumnInfo(name = "QuizId")
    private int quizId;

    @ColumnInfo(name = "Score")
    private int score;

    @ColumnInfo(name = "AttemptDate")
    private String attemptDate;

    @ColumnInfo(name = "Question")
    private String question;

    // Constructor
    public UserScoreWithQuizDetails(int scoreId, int userId, int quizId, int score, 
                                   String attemptDate, String question) {
        this.scoreId = scoreId;
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.attemptDate = attemptDate;
        this.question = question;
    }

    // Getters and setters
    public int getScoreId() {
        return scoreId;
    }

    public void setScoreId(int scoreId) {
        this.scoreId = scoreId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(String attemptDate) {
        this.attemptDate = attemptDate;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
} 