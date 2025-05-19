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
import com.example.elsa_speak_clone.database.firebase.SyncManager;

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

                // Sync to Firebase
                syncProgressToFirebase(userId);
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
                
                // Sync to Firebase
                syncProgressToFirebase(userId);
            }
            
            Log.d(TAG, "Added " + points + " XP for user " + userId + " in lesson " + lessonId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding XP points: " + e.getMessage(), e);
        }
    }

    /**
     * Sync progress to Firebase after updates
     */
    private void syncProgressToFirebase(int userId) {
        try {
            // Check if network is available
            if (isNetworkAvailable()) {
                SyncManager syncManager = new SyncManager(context);
                syncManager.syncUserProgressToFirebase(userId)
                    .thenAccept(success -> {
                        if (success) {
                            Log.d(TAG, "User progress successfully synced to Firebase");
                        } else {
                            Log.w(TAG, "Failed to sync user progress to Firebase");
                        }
                    })
                    .exceptionally(e -> {
                        Log.e(TAG, "Error syncing to Firebase: " + e.getMessage(), e);
                        return null;
                    });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initiating Firebase sync: " + e.getMessage(), e);
        }
    }

    /**
     * Check if network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
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