package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.elsa_speak_clone.database.entities.Quiz;

import java.util.List;

@Dao
public interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Quiz> quizzes);

    @Insert
    long insert(Quiz quiz);

    @Update
    void update(Quiz quiz);

    @Delete
    void delete(Quiz quiz);

    @Query("SELECT * FROM Quizzes")
    List<Quiz> getAllQuizzes();

    @Query("SELECT * FROM Quizzes WHERE QuizId = :quizId")
    Quiz getQuizById(int quizId);

    @Query("SELECT * FROM Quizzes WHERE LessonId = :lessonId")
    List<Quiz> getQuizzesForLesson(int lessonId);

    @Query("SELECT * FROM Quizzes WHERE LessonId = :lessonId ORDER BY RANDOM() LIMIT 1")
    Quiz getRandomQuizForLesson(int lessonId);

    @Query("SELECT * FROM Quizzes WHERE LessonId = :lessonId AND QuizId NOT IN (:excludedIds) ORDER BY RANDOM() LIMIT 1")
    Quiz getRandomQuizForLessonExcluding(int lessonId, List<Integer> excludedIds);

    @Query("DELETE FROM Quizzes")
    void deleteAll();
} 