package com.example.elsa_speak_clone.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Quizzes",
        foreignKeys = @ForeignKey(
                entity = Lesson.class,
                parentColumns = "LessonId",
                childColumns = "LessonId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("LessonId")})
public class Quiz {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "QuizId")
    private int quizId;

    @ColumnInfo(name = "Question")
    private String question;

    @ColumnInfo(name = "Answer")
    private String answer;

    @ColumnInfo(name = "LessonId")
    private int lessonId;

    // Constructor
    public Quiz(int quizId, String question, String answer, int lessonId) {
        this.quizId = quizId;
        this.question = question;
        this.answer = answer;
        this.lessonId = lessonId;
    }

    // Getters and setters
    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }
} 