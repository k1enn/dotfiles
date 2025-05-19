package com.example.elsa_speak_clone.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "UserProgress",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "UserId",
                        childColumns = "UserId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Lesson.class,
                        parentColumns = "LessonId",
                        childColumns = "LessonId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("UserId"), @Index("LessonId")})
public class UserProgress {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "ProgressId")
    private int progressId;

    @ColumnInfo(name = "UserId")
    private int userId;

    @ColumnInfo(name = "LessonId")
    private int lessonId;

    @ColumnInfo(name = "DifficultyLevel")
    private int difficultyLevel;

    @ColumnInfo(name = "CompletionTime")
    private String completionTime;

    @ColumnInfo(name = "Streak")
    private int streak;

    @ColumnInfo(name = "Xp")
    private int xp;

    @ColumnInfo(name = "LastStudyDate")
    private String lastStudyDate;

    // Constructor
    public UserProgress(int progressId, int userId, int lessonId, int difficultyLevel, 
                        String completionTime, int streak, int xp, String lastStudyDate) {
        this.progressId = progressId;
        this.userId = userId;
        this.lessonId = lessonId;
        this.difficultyLevel = difficultyLevel;
        this.completionTime = completionTime;
        this.streak = streak;
        this.xp = xp;
        this.lastStudyDate = lastStudyDate;
    }

    // Default constructor required by Room
    public UserProgress() {
        // Default values
        this.progressId = 0;
        this.userId = 0;
        this.lessonId = 0;
        this.difficultyLevel = 0;
        this.completionTime = null;
        this.streak = 0;
        this.xp = 0;
        this.lastStudyDate = null;
    }

    // Getters and setters
    public int getProgressId() {
        return progressId;
    }

    public void setProgressId(int progressId) {
        this.progressId = progressId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(String completionTime) {
        this.completionTime = completionTime;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public String getLastStudyDate() {
        return lastStudyDate;
    }

    public void setLastStudyDate(String lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
    }

    public void setCurrentStreak(int streak) {
        this.streak = streak;
    }
} 