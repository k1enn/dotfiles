package com.example.elsa_speak_clone.database.entities;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "UserScores",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "UserId",
                        childColumns = "UserId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Quiz.class,
                        parentColumns = "QuizId",
                        childColumns = "QuizId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("UserId"), @Index("QuizId")})
public class UserScore {
    @PrimaryKey
    @NonNull
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

    // Constructor
    public UserScore(int scoreId, int userId, int quizId, int score, String attemptDate) {
        this.scoreId = scoreId;
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.attemptDate = attemptDate;
    }

    public UserScore() {
        
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

    public void setLessonId(int lessonId) {
        // This method appears to be intended for a lessonId field
        // Since UserScore doesn't have this field directly, we need to implement
        // this method to maintain API compatibility
        
        // If you need to associate scores with lessons directly in the future,
        // you could add a lessonId field to this entity
        Log.w("UserScore", "setLessonId called but entity doesn't have this field");
        // Alternative implementation could be to fetch the Quiz and update something
    }

    public void setDateUpdated(String currentDate) {
        // This method should update the attemptDate field
        this.attemptDate = currentDate;
    }

    /**
     * Calculate a user's performance level based on their score
     * @return A string representing the performance level
     */
    public String getPerformanceLevel() {
        if (score >= 90) {
            return "Excellent";
        } else if (score >= 75) {
            return "Good";
        } else if (score >= 60) {
            return "Average";
        } else if (score >= 40) {
            return "Need Improvement";
        } else {
            return "Poor";
        }
    }
    
    /**
     * Format the attempt date in a user-friendly format
     * @return Formatted date string
     */
    public String getFormattedAttemptDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(attemptDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            return attemptDate; // Return original if parsing fails
        }
    }
    
    /**
     * Check if this score is a passing score (typically above 60%)
     * @return True if this is a passing score
     */
    public boolean isPassing() {
        return score >= 60;
    }
    
    /**
     * Update the score and date in one method call
     * @param newScore The new score value
     * @param date The date of the update
     */
    public void updateScoreAndDate(int newScore, String date) {
        this.score = newScore;
        this.attemptDate = date;
    }
}