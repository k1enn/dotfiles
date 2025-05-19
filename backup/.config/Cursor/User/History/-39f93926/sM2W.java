package com.example.elsa_speak_clone.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Lessons")
public class Lesson {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "LessonId")
    private int lessonId;

    @ColumnInfo(name = "Topic")
    private String topic;

    @ColumnInfo(name = "LessonContent")
    private String lessonContent;

    @ColumnInfo(name = "DifficultyLevel")
    private int difficultyLevel;

    // Constructor
    public Lesson(int lessonId, String topic, String lessonContent, int difficultyLevel) {
        this.lessonId = lessonId;
        this.topic = topic;
        this.lessonContent = lessonContent;
        this.difficultyLevel = difficultyLevel;
    }

    // Getters and setters
    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getLessonContent() {
        return lessonContent;
    }

    public void setLessonContent(String lessonContent) {
        this.lessonContent = lessonContent;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
} 