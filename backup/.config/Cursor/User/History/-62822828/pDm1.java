package com.example.elsa_speak_clone.database.repositories;

import android.content.Context;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.UserScoreDao;
import com.example.elsa_speak_clone.database.entities.UserScore;
import com.example.elsa_speak_clone.database.entities.UserScoreWithQuizDetails;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserScoreRepository {
    private static final String TAG = "UserScoreRepository";
    private final UserScoreDao userScoreDao;

    public UserScoreRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userScoreDao = db.userScoreDao();
    }

    /**
     * Get current date formatted as yyyy-MM-dd
     * @return Formatted date string
     */
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    /**
     * Insert a new user score
     * @param userId The user ID
     * @param quizId The quiz ID
     * @param score The score achieved
     * @return The ID of the inserted score or -1 if failed
     */
    public long insertUserScore(int userId, int quizId, int score) {
        try {
            UserScore userScore = new UserScore(0, userId, quizId, score, getCurrentDate());
            Future<Long> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userScoreDao.insert(userScore));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error inserting user score", e);
            return -1;
        }
    }

    /**
     * Get all scores for a user
     * @param userId The user ID
     * @return List of user scores
     */
    public List<UserScore> getUserScores(int userId) {
        try {
            Future<List<UserScore>> future = AppDatabase.databaseWriteExecutor.submit(() ->
                userScoreDao.getUserScores(userId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting user scores", e);
            return null;
        }
    }

    /**
     * Get all scores with quiz details for a user
     * @param userId The user ID
     * @return List of user scores with quiz details
     */
    public List<UserScoreWithQuizDetails> getUserScoresWithQuizDetails(int userId) {
        try {
            Future<List<UserScoreWithQuizDetails>> future = AppDatabase.databaseWriteExecutor.submit(() ->
                userScoreDao.getUserScoresWithQuizDetails(userId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting user scores with quiz details", e);
            return null;
        }
    }

    /**
     * Get user's average score
     * @param userId The user ID
     * @return The average score or 0 if no scores exist or an error occurs
     */
    public float getUserAverageScore(int userId) {
        try {
            Future<Float> future = AppDatabase.databaseWriteExecutor.submit(() ->
                userScoreDao.getUserAverageScore(userId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting average score", e);
            return 0;
        }
    }

    /**
     * Get user's highest score for a specific quiz
     * @param userId The user ID
     * @param quizId The quiz ID
     * @return The highest score or 0 if no scores exist or an error occurs
     */
    public int getUserHighestScoreForQuiz(int userId, int quizId) {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(() ->
                userScoreDao.getUserHighestScoreForQuiz(userId, quizId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting highest score for quiz", e);
            return 0;
        }
    }

    /**
     * Delete all scores for a user
     * @param userId The user ID
     * @return The number of scores deleted
     */
    public int deleteAllUserScores(int userId) {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userScoreDao.deleteAllUserScores(userId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error deleting user scores", e);
            return 0;
        }
    }
} 