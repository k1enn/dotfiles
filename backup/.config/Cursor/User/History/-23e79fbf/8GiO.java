package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.elsa_speak_clone.database.entities.UserProgress;

import java.util.List;

@Dao
public interface UserProgressDao {
    @Insert
    long insert(UserProgress userProgress);

    @Update
    void update(UserProgress userProgress);

    @Query("SELECT * FROM UserProgress WHERE UserId = :userId")
    List<UserProgress> getUserProgress(int userId);

    @Query("SELECT * FROM UserProgress WHERE UserId = :userId AND LessonId = :lessonId")
    UserProgress getUserLessonProgress(int userId, int lessonId);

    @Query("UPDATE UserProgress SET Streak = :streak, LastStudyDate = :date WHERE UserId = :userId")
    void updateUserStreak(int userId, int streak, String date);

    @Query("UPDATE UserProgress SET Xp = Xp + :points WHERE UserId = :userId")
    void addXpPoints(int userId, int points);

    @Query("SELECT COUNT(*) FROM UserProgress WHERE UserId = :userId")
    int countUserProgressEntries(int userId);

    @Query("SELECT * FROM UserProgress WHERE UserId = :userId LIMIT 1")
    UserProgress getUserProgressById(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserProgress(UserProgress progress);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateUserProgress(UserProgress existingProgress);
    
    @Query("SELECT * FROM UserProgress")
    List<UserProgress> getAllUserProgress();
    
    @Query("SELECT SUM(Xp) FROM UserProgress WHERE UserId = :userId")
    int getTotalXpForUser(int userId);
    
    @Query("SELECT MAX(Streak) FROM UserProgress WHERE UserId = :userId")
    int getMaxStreakForUser(int userId);
    
    @Query("DELETE FROM UserProgress WHERE UserId = :userId")
    void deleteUserProgress(int userId);
    
    @Query("UPDATE UserProgress SET CompletedAt = :completionDate WHERE UserId = :userId AND LessonId = :lessonId")
    void markLessonAsCompleted(int userId, int lessonId, String completionDate);
    
    @Query("SELECT COUNT(*) FROM UserProgress WHERE UserId = :userId AND CompletedAt IS NOT NULL")
    int getCompletedLessonsCount(int userId);
    
    @Query("SELECT LessonId FROM UserProgress WHERE UserId = :userId AND CompletedAt IS NOT NULL")
    List<Integer> getCompletedLessonIds(int userId);
} 