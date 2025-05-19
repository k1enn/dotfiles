package com.example.elsa_speak_clone.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.entities.UserScore;
import com.example.elsa_speak_clone.database.firebase.FirebaseDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizService {
    private static final String TAG = "QuizService";
    private final AppDatabase database;
    private final Context context;

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
     * Add XP points to user score for a specific lesson
     * 
     * @param userId The user ID
     * @param lessonId The lesson ID
     * @param points The number of points to add
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

                // Update Firebase if network is available
                try {
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                        FirebaseDbHelper firebaseHelper = FirebaseDbHelper.getInstance();
                        // Update XP in Firebase for this user
                        firebaseHelper.updateUserStats(userId, existingProgress.getXp(), existingProgress.getStreak())
                            .thenAccept(success -> {
                                if (success) {
                                    Log.d(TAG, "User stats updated in Firebase");
                                } else {
                                    Log.e(TAG, "Failed to update user stats in Firebase");
                                }
                            });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating Firebase: " + e.getMessage(), e);
                }
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