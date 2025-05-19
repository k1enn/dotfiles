package com.example.elsa_speak_clone.database.firebase;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.Lesson;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages all synchronization operations between local database and Firebase
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    private static final int DEFAULT_SYNC_INTERVAL_HOURS = 24;
    
    private final Context context;
    private final FirebaseDataSynchronizer synchronizer;
    private final SyncStatusManager statusManager;
    
    public SyncManager(Context context) {
        this.context = context;
        this.synchronizer = new FirebaseDataSynchronizer(context);
        this.statusManager = new SyncStatusManager(context);
    }
    
    /**
     * Check if network is available
     * 
     * @return true if network is available, false otherwise
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }
    
    /**
     * Check if a sync is needed based on time since last sync
     * 
     * @return true if sync is needed, false otherwise
     */
    public boolean isSyncNeeded() {
        return statusManager.isSyncNeeded(DEFAULT_SYNC_INTERVAL_HOURS);
    }
    
    /**
     * Perform a complete data synchronization if network is available and sync is needed
     * 
     * @param forceSync true to force sync regardless of time since last sync
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> performSync(boolean forceSync) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network not available, skipping sync");
            return CompletableFuture.completedFuture(false);
        }
        
        if (!forceSync && !isSyncNeeded()) {
            Log.d(TAG, "Sync not needed, last sync was " + statusManager.getLastSyncTimeFormatted());
            return CompletableFuture.completedFuture(true);
        }
        
        Log.d(TAG, "Starting data synchronization...");
        
        // Perform bidirectional sync for lessons, vocabulary, and users
        return synchronizeLessons()
            .thenCompose(lessonResult -> {
                Log.d(TAG, "Lesson sync " + (lessonResult ? "successful" : "failed"));
                return synchronizeVocabulary();
            })
            .thenCompose(vocabResult -> {
                Log.d(TAG, "Vocabulary sync " + (vocabResult ? "successful" : "failed"));
                return synchronizeUsers();
            })
            .thenApply(userResult -> {
                Log.d(TAG, "User sync " + (userResult ? "successful" : "failed"));
                
                // Overall success if all components succeeded
                boolean overallSuccess = userResult;
                statusManager.updateSyncStatus(overallSuccess);
                
                Log.d(TAG, "Sync completed " + (overallSuccess ? "successfully" : "with some errors"));
                return overallSuccess;
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error during sync: " + e.getMessage(), e);
                statusManager.updateSyncStatus(false);
                return false;
            });
    }
    
    /**
     * Synchronize all lessons bidirectionally
     */
    private CompletableFuture<Boolean> synchronizeLessons() {
        return synchronizer.importLessonsFromFirebase()
            .thenCompose(count -> {
                Log.d(TAG, "Imported " + count + " new lessons from Firebase");
                return synchronizer.syncAllLessons();
            });
    }
    
    /**
     * Synchronize vocabulary for all lessons bidirectionally
     */
    private CompletableFuture<Boolean> synchronizeVocabulary() {
        List<Lesson> lessons = null;
        try {
            lessons = AppDatabase.getInstance(context).lessonDao().getAllLessons();
        } catch (Exception e) {
            Log.e(TAG, "Error getting lessons for vocabulary sync", e);
            return CompletableFuture.completedFuture(false);
        }
        
        if (lessons == null || lessons.isEmpty()) {
            return CompletableFuture.completedFuture(true);
        }
        
        // Create a chain of futures for each lesson's vocabulary
        CompletableFuture<Boolean> chainedFuture = CompletableFuture.completedFuture(true);
        
        for (Lesson lesson : lessons) {
            chainedFuture = chainedFuture.thenCompose(result -> {
                if (!result) {
                    return CompletableFuture.completedFuture(false);
                }
                
                // First import vocabulary from Firebase
                return synchronizer.importLessonVocabularyFromFirebase(lesson.getLessonId())
                    .thenCompose(count -> {
                        Log.d(TAG, "Imported " + count + " vocabulary items for lesson " + lesson.getLessonId());
                        // Then sync back to Firebase to ensure it has everything
                        return synchronizer.syncLessonVocabulary(lesson.getLessonId());
                    });
            });
        }
        
        return chainedFuture;
    }
    
    /**
     * Synchronize user data bidirectionally
     */
    private CompletableFuture<Boolean> synchronizeUsers() {
        return synchronizer.importUsersFromFirebase()
            .thenCompose(userCount -> {
                Log.d(TAG, "Imported " + userCount + " users from Firebase");
                return synchronizer.syncAllUsers();
            });
    }
    
    /**
     * Get the last sync status for display
     * 
     * @return String with last sync status
     */
    public String getSyncStatusText() {
        String time = statusManager.getLastSyncTimeFormatted();
        boolean success = statusManager.wasLastSyncSuccessful();
        
        if (time.equals("Never")) {
            return "Never synchronized";
        } else {
            return "Last sync: " + time + " (" + (success ? "successful" : "failed") + ")";
        }
    }
    
    /**
     * Force a full data sync regardless of schedule
     * 
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> forceFullSync() {
        return performSync(true);
    }
} 