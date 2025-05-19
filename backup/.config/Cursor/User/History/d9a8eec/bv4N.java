package com.example.elsa_speak_clone.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Vocabulary",
        foreignKeys = @ForeignKey(
                entity = Lesson.class,
                parentColumns = "LessonId",
                childColumns = "LessonId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("LessonId")})
public class Vocabulary {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "WordId")
    private int wordId;

    @ColumnInfo(name = "Word")
    private String word;

    @ColumnInfo(name = "Pronunciation")
    private String pronunciation;

    @ColumnInfo(name = "LessonId")
    private int lessonId;

    // Constructor
    public Vocabulary(int wordId, String word, String pronunciation, int lessonId) {
        this.wordId = wordId;
        this.word = word;
        this.pronunciation = pronunciation;
        this.lessonId = lessonId;
    }

    // Getters and setters
    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }
} 