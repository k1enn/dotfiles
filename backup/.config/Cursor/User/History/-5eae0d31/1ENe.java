package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.elsa_speak_clone.database.entities.Vocabulary;

import java.util.List;

@Dao
public interface VocabularyDao {
    @Insert
    long insertVocabulary(Vocabulary vocabulary);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Vocabulary> vocabularies);
    
    @Update
    void updateVocabulary(Vocabulary vocabulary);
    
    @Query("SELECT * FROM Vocabulary WHERE LessonId = :lessonId")
    List<Vocabulary> getVocabularyByLessonId(int lessonId);
    
    @Query("SELECT word FROM Vocabulary WHERE LessonId = :lessonId")
    List<String> getWordsByLessonId(int lessonId);
    
    @Query("SELECT * FROM Vocabulary WHERE VocabularyId = :vocabularyId")
    Vocabulary getVocabularyById(int vocabularyId);
    
    @Query("DELETE FROM Vocabulary")
    void deleteAll();
    
    @Query("SELECT COUNT(*) FROM Vocabulary WHERE LessonId = :lessonId")
    int countVocabularyByLesson(int lessonId);
} 