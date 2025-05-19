package com.example.elsa_speak_clone.database.firebase;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.concurrent.CompletableFuture;

/**
 * Manages user data synchronization between local database and Firebase
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
     */
    public boolean isSyncNeeded() {
        return statusManager.isSyncNeeded(DEFAULT_SYNC_INTERVAL_HOURS);
    }
    
    /**
     * Sync user data to Firebase
     * Case 1: User already logged in - Sync to Firebase
     * Case 3: New user registration - Sync to Firebase
     * 
     * @param userId The user ID to sync
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> syncUserToFirebase(int userId) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network not available, skipping user sync");
            return CompletableFuture.completedFuture(false);
        }
        
        Log.d(TAG, "Syncing user data to Firebase for user: " + userId);
        
        return synchronizer.syncUserData(userId)
            .thenApply(result -> {
                statusManager.updateSyncStatus(result);
                Log.d(TAG, "User sync to Firebase " + (result ? "successful" : "failed") + 
                          " for user " + userId);
                return result;
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error syncing user to Firebase: " + e.getMessage(), e);
                statusManager.updateSyncStatus(false);
                return false;
            });
    }
    
    /**
     * Recover user data from Firebase
     * Case 2: User login after app deletion
     * 
     * @param email User's email address
     * @return CompletableFuture with the restored user or null if not found
     */
    public CompletableFuture<Boolean> recoverUserFromFirebase(String email) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network not available, skipping user recovery");
            return CompletableFuture.completedFuture(false);
        }
        
        Log.d(TAG, "Attempting to recover user data from Firebase for: " + email);
        
        return synchronizer.restoreUserByEmail(email)
            .thenApply(user -> {
                boolean success = (user != null);
                statusManager.updateSyncStatus(success);
                Log.d(TAG, "User recovery from Firebase " + 
                         (success ? "successful" : "failed or not found") + 
                         " for email " + email);
                return success;
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error recovering user from Firebase: " + e.getMessage(), e);
                statusManager.updateSyncStatus(false);
                return false;
            });
    }
    
    /**
     * Sync user progress update to Firebase
     * Case 4: After completing a quiz or updating streak
     * 
     * @param userId User ID
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> syncUserProgressToFirebase(int userId) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network not available, skipping progress sync");
            return CompletableFuture.completedFuture(false);
        }
        
        Log.d(TAG, "Syncing user progress to Firebase for user: " + userId);
        
        return synchronizer.syncUserProgress(userId)
            .thenApply(result -> {
                // We don't update the full sync status for individual progress updates
                Log.d(TAG, "User progress sync " + (result ? "successful" : "failed") + 
                         " for user " + userId);
                return result;
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error syncing user progress: " + e.getMessage(), e);
                return false;
            });
    }
    
    /**
     * Get the last sync status for display
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
} 