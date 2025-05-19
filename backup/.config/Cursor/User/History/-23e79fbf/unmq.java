package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.elsa_speak_clone.database.entity.UserProgress;

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
} 