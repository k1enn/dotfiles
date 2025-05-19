package com.example.elsa_speak_clone.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.database.entities.UserProgress;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizService {
    private static final String TAG = "QuizService";
    private final AppDatabase database;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public QuizService(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
    }

    /**
     * Get a random quiz for a specific lesson
     * 
     * @param lessonId The lesson ID
     * @return A random quiz for the lesson
     */
    public Quiz getRandomQuizForLesson(int lessonId) {
        return database.quizDao().getRandomQuizForLesson(lessonId);
    }

    /**
     * Add XP points and sync to Firebase
     */
    public void addXpPoints(int userId, int lessonId, int points) {
        try {
            // Check if score entry exists for this specific user AND lesson combination
            UserProgress existingProgress = database.userProgressDao().getUserLessonProgress(userId, lessonId);
            
            if (existingProgress != null) {
                // Update existing progress for this lesson
                existingProgress.setXp(existingProgress.getXp() + points);
                existingProgress.setLastStudyDate(getCurrentDate());
                database.userProgressDao().update(existingProgress);
                Log.d(TAG, "Updated XP for existing progress: " + existingProgress.getProgressId());
                
            } else {
                // Create new progress entry
                // First, determine the next available progress ID
                int progressId = getNextProgressId(userId);
                
                // Create new progress with all required fields
                UserProgress newProgress = new UserProgress(
                    progressId,
                    userId,
                    lessonId,
                    0, // Default difficulty level
                    null, // No completion time yet
                    1, // Start with streak of 1
                    points, // Initial XP points
                    getCurrentDate() // Current date
                );
                
                // Insert the new progress
                long insertedId = database.userProgressDao().insert(newProgress);
                Log.d(TAG, "Created new progress with ID: " + insertedId);
                
            }
            
            Log.d(TAG, "Added " + points + " XP for user " + userId + " in lesson " + lessonId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding XP points: " + e.getMessage(), e);
        }
    }

    /**
     * Updates user progress after completing a quiz
     * @param userId The user ID
     * @param lessonId The lesson ID
     * @param correctAnswers Number of correct answers
     * @param totalQuestions Total number of questions
     */
    public void updateQuizProgress(int userId, int lessonId, int correctAnswers, int totalQuestions) {
        executor.execute(() -> {
            try {
                // Calculate points based on performance (customize this formula as needed)
                int points = (correctAnswers * 20); // 20 points per correct answer
                
                // Add XP points
                addXpPoints(userId, lessonId, points);
                
                // If got a high score (e.g., 80% or more correct), mark lesson as completed
                if (totalQuestions > 0 && (correctAnswers * 100 / totalQuestions) >= 80) {
                    markLessonCompleted(userId, lessonId);
                }
                
                Log.d(TAG, "Updated progress for user " + userId + " in lesson " + lessonId + 
                        " with " + correctAnswers + "/" + totalQuestions + " correct answers");
            } catch (Exception e) {
                Log.e(TAG, "Error updating quiz progress: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Marks a lesson as completed
     * @param userId The user ID
     * @param lessonId The lesson ID
     */
    private void markLessonCompleted(int userId, int lessonId) {
        try {
            // Get current user progress
            UserProgress progress = database.userProgressDao().getUserLessonProgress(userId, lessonId);
            
            if (progress != null) {
                // Set completion time if not already set
                if (progress.getCompletionTime() == null) {
                    progress.setCompletionTime(getCurrentTime());
                    database.userProgressDao().update(progress);
                    Log.d(TAG, "Marked lesson " + lessonId + " as completed for user " + userId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error marking lesson as completed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the current time formatted as a string
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get next available progress ID for a user
     * 
     * @param userId The user ID
     * @return The next available progress ID
     */
    private int getNextProgressId(int userId) {
        try {
            // Count existing progress entries and add 1
            int count = database.userProgressDao().countUserProgressEntries(userId);
            return (userId * 1000) + count + 1; // This creates unique IDs like 1001, 1002, etc. for user 1
        } catch (Exception e) {
            Log.e(TAG, "Error getting next progress ID", e);
            // Fallback to a timestamp-based ID if query fails
            return (userId * 1000) + (int)(System.currentTimeMillis() % 1000);
        }
    }

    /**
     * Get the current date as a string in yyyy-MM-dd format
     * 
     * @return The current date string
     */
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }
} 