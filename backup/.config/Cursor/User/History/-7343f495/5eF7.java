package com.example.elsa_speak_clone.services;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.SharedResult;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.entities.UserScore;
import com.example.elsa_speak_clone.database.firebase.FirebaseDbHelper;
import com.example.elsa_speak_clone.database.repositories.UserProgressRepository;
import com.example.elsa_speak_clone.database.repositories.UserRepository;
import com.example.elsa_speak_clone.database.repositories.UserScoreRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DataSyncService {
    private static final String TAG = "DataSyncService";
    private static final String PREFS_NAME = "sync_prefs";
    private static final String LAST_SYNC_KEY = "last_sync_time";
    
    private final Context context;
    private final FirebaseDbHelper firebaseDb;
    private final AppDatabase localDb;
    private final Executor executor;
    private final SharedPreferences syncPrefs;
    
    private final UserRepository userRepository;
    private final UserProgressRepository progressRepository;
    private final UserScoreRepository scoreRepository;
    
    public DataSyncService(Application application) {
        this.context = application.getApplicationContext();
        this.firebaseDb = FirebaseDbHelper.getInstance();
        this.localDb = AppDatabase.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.syncPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Initialize repositories
        this.userRepository = new UserRepository(application);
        this.progressRepository = new UserProgressRepository(application);
        this.scoreRepository = new UserScoreRepository(context);
    }
    
    /**
     * Sync a specific user's data
     */
    public CompletableFuture<Boolean> syncUserData(int userId) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        
        // Check for internet connection
        if (!isNetworkAvailable()) {
            Log.d(TAG, "No network connection available. Skipping sync.");
            result.complete(false);
            return result;
        }
        
        executor.execute(() -> {
            try {
                // Get user from local database
                User user = userRepository.getUserById(userId);
                if (user == null) {
                    Log.e(TAG, "User not found in local database");
                    result.complete(false);
                    return;
                }
                
                // Sync user profile to Firebase
                firebaseDb.saveUser(user).get();
                
                // Sync user progress to Firebase
                List<UserProgress> progressList = progressRepository.getUserProgressList(userId);
                for (UserProgress progress : progressList) {
                    firebaseDb.saveUserProgress(progress).get();
                }
                
                // Sync user scores to Firebase
                List<UserScore> scoreList = scoreRepository.getUserScores(userId);
                for (UserScore score : scoreList) {
                    firebaseDb.saveUserScore(score).get();
                }
                
                // Update last sync time
                updateLastSyncTime();
                
                result.complete(true);
            } catch (Exception e) {
                Log.e(TAG, "Error during data sync", e);
                result.complete(false);
            }
        });
        
        return result;
    }
    
    /**
     * Sync data from Firebase to local database
     */
    public CompletableFuture<Boolean> syncFromFirebase(int userId) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        
        // Check for internet connection
        if (!isNetworkAvailable()) {
            Log.d(TAG, "No network connection available. Skipping sync.");
            result.complete(false);
            return result;
        }
        
        executor.execute(() -> {
            try {
                // Get user from local database to get the email
                User localUser = userRepository.getUserById(userId);
                if (localUser == null) {
                    Log.e(TAG, "User not found in local database");
                    result.complete(false);
                    return;
                }
                
                // Sync user progress from Firebase
                List<UserProgress> firebaseProgress = firebaseDb.getUserProgress(userId).get();
                if (firebaseProgress != null && !firebaseProgress.isEmpty()) {
                    for (UserProgress progress : firebaseProgress) {
                        // Check if progress exists locally, if not, insert it
                        UserProgress localProgress = progressRepository.getUserLessonProgress(userId, progress.getLessonId());
                        if (localProgress == null) {
                            progressRepository.insertUserProgress(progress);
                        } else {
                            // Compare timestamps and use the most recent
                            String localDate = localProgress.getLastStudyDate();
                            String firebaseDate = progress.getLastStudyDate();
                            if (isDateNewer(firebaseDate, localDate)) {
                                progressRepository.updateUserProgress(progress);
                            }
                        }
                    }
                }
                
                // Sync user scores from Firebase
                List<UserScore> firebaseScores = firebaseDb.getUserScores(userId).get();
                if (firebaseScores != null && !firebaseScores.isEmpty()) {
                    for (UserScore score : firebaseScores) {
                        // Check if score exists locally, if not, insert it
                        UserScore localScore = scoreRepository.getUserScoreById(score.getScoreId());
                        if (localScore == null) {
                            scoreRepository.insertUserScore(score);
                        } else {
                            // Compare timestamps and use the most recent
                            String localDate = localScore.getAttemptDate();
                            String firebaseDate = score.getAttemptDate();
                            if (isDateNewer(firebaseDate, localDate)) {
                                scoreRepository.updateUserScore(score);
                            }
                        }
                    }
                }
                
                // Update last sync time
                updateLastSyncTime();
                
                result.complete(true);
            } catch (Exception e) {
                Log.e(TAG, "Error during data sync from Firebase", e);
                result.complete(false);
            }
        });
        
        return result;
    }
    
    /**
     * Perform a full two-way sync
     */
    public CompletableFuture<Boolean> fullSync(int userId) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        
        // First sync from Firebase to local
        syncFromFirebase(userId)
            .thenCompose(fromResult -> {
                // Then sync from local to Firebase
                return syncUserData(userId);
            })
            .thenAccept(toResult -> {
                result.complete(true);
            })
            .exceptionally(ex -> {
                Log.e(TAG, "Error during full sync", ex);
                result.complete(false);
                return null;
            });
        
        return result;
    }
    
    // -------------------------- Utility Methods --------------------------
    
    /**
     * Check if a date is newer than another
     */
    private boolean isDateNewer(String date1, String date2) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d1 = format.parse(date1);
            Date d2 = format.parse(date2);
            return d1.after(d2);
        } catch (Exception e) {
            Log.e(TAG, "Error comparing dates", e);
            return false;
        }
    }
    
    /**
     * Update the last sync time
     */
    private void updateLastSyncTime() {
        long currentTime = System.currentTimeMillis();
        syncPrefs.edit().putLong(LAST_SYNC_KEY, currentTime).apply();
    }
    
    /**
     * Get the last sync time
     */
    public long getLastSyncTime() {
        return syncPrefs.getLong(LAST_SYNC_KEY, 0);
    }
    
    /**
     * Check if network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
} 