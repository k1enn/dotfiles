package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.elsa_speak_clone.database.entity.Quiz;

import java.util.List;

@Dao
public interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Quiz> quizzes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Quiz quiz);

    @Query("SELECT * FROM Quizzes")
    List<Quiz> getAllQuizzes();

    @Query("SELECT * FROM Quizzes WHERE QuizId = :quizId")
    Quiz getQuizById(int quizId);

    @Query("SELECT * FROM Quizzes WHERE LessonId = :lessonId")
    List<Quiz> getQuizzesByLesson(int lessonId);
} 