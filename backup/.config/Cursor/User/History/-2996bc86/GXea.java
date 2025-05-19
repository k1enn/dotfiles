package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.elsa_speak_clone.database.entities.UserScore;
import com.example.elsa_speak_clone.database.entities.UserScoreWithQuizDetails;

import java.util.List;

@Dao
public interface UserScoreDao {
    @Insert
    long insert(UserScore userScore);

    @Query("SELECT * FROM UserScores WHERE UserId = :userId ORDER BY AttemptDate DESC")
    List<UserScore> getUserScores(int userId);

    @Query("SELECT us.*, q.Question FROM UserScores us JOIN Quizzes q ON us.QuizId = q.QuizId WHERE us.UserId = :userId ORDER BY us.AttemptDate DESC")
    List<UserScoreWithQuizDetails> getUserScoresWithQuizDetails(int userId);
} 