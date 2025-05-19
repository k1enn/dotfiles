package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.elsa_speak_clone.database.entities.Lesson;

import java.util.List;

@Dao
public interface LessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Lesson> lessons);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Lesson lesson);
    
    @Query("SELECT * FROM Lessons")
    List<Lesson> getAllLessons();
    
    @Query("SELECT * FROM Lessons WHERE LessonId = :lessonId")
    Lesson getLessonById(int lessonId);
    
    @Query("SELECT * FROM Lessons WHERE DifficultyLevel = :difficultyLevel")
    List<Lesson> getLessonsByDifficulty(int difficultyLevel);

    @Query("SELECT Topic FROM Lessons WHERE LessonId = :lessonId")
    String getLessonTitleById(int lessonId);
}