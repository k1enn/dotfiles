package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.elsa_speak_clone.database.entities.Vocabulary;

import java.util.List;

@Dao
public interface VocabularyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Vocabulary> vocabularies);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Vocabulary vocabulary);
    
    @Query("SELECT * FROM Vocabulary WHERE LessonId = :lessonId ORDER BY WordId")
    List<Vocabulary> getVocabularyByLessonId(int lessonId);
    
    @Query("SELECT Word FROM Vocabulary WHERE LessonId = :lessonId ORDER BY WordId")
    List<String> getWordsByLessonId(int lessonId);

   @Query("SELECT * FROM vocabulary WHERE LessonId = :lessonId")
    List<Vocabulary> getAllVocabularyForLesson(int lessonId);

    @Query("DELETE FROM Vocabularies")
    void deleteAll();
} 