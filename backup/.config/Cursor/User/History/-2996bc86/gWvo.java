package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.elsa_speak_clone.database.entities.UserScore;
import com.example.elsa_speak_clone.database.entities.UserScoreWithQuizDetails;

import java.util.List;

@Dao
public interface UserScoreDao {
    @Insert
    long insert(UserScore userScore);

    @Update
    int update(UserScore userScore);
    
    @Delete
    int delete(UserScore userScore);

    @Query("SELECT * FROM UserScores WHERE UserId = :userId ORDER BY AttemptDate DESC")
    List<UserScore> getUserScores(int userId);
    
    @Query("SELECT * FROM UserScores WHERE ScoreId = :scoreId")
    UserScore getUserScoreById(int scoreId);
    
    @Query("SELECT us.*, q.Question FROM UserScores us JOIN Quizzes q ON us.QuizId = q.QuizId WHERE us.UserId = :userId ORDER BY us.AttemptDate DESC")
    List<UserScoreWithQuizDetails> getUserScoresWithQuizDetails(int userId);
    
    @Query("SELECT COUNT(*) FROM UserScores WHERE UserId = :userId")
    int getUserScoreCount(int userId);
    
    @Query("SELECT AVG(Score) FROM UserScores WHERE UserId = :userId")
    float getUserAverageScore(int userId);
    
    @Query("SELECT MAX(Score) FROM UserScores WHERE UserId = :userId AND QuizId = :quizId")
    int getUserHighestScoreForQuiz(int userId, int quizId);
    
    @Query("DELETE FROM UserScores WHERE UserId = :userId")
    int deleteAllUserScores(int userId);
}