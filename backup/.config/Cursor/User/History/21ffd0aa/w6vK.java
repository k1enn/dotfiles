package com.example.elsa_speak_clone.database.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Manages synchronization status and history
 */
public class SyncStatusManager {
    private static final String TAG = "SyncStatusManager";
    private static final String PREF_NAME = "sync_status_prefs";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";
    private static final String KEY_SYNC_STATUS = "last_sync_status";
    
    private final SharedPreferences preferences;
    
    public SyncStatusManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Update the last sync time and status
     * 
     * @param success Whether the sync was successful
     */
    public void updateSyncStatus(boolean success) {
        long timestamp = System.currentTimeMillis();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_LAST_SYNC, timestamp);
        editor.putBoolean(KEY_SYNC_STATUS, success);
        editor.apply();
        
        Log.d(TAG, "Updated sync status: " + (success ? "SUCCESS" : "FAILED") + 
                   " at " + formatTimestamp(timestamp));
    }
    
    /**
     * Get the timestamp of the last sync attempt
     * 
     * @return Timestamp in milliseconds, or 0 if no sync has been performed
     */
    public long getLastSyncTime() {
        return preferences.getLong(KEY_LAST_SYNC, 0);
    }
    
    /**
     * Get the formatted date/time of the last sync
     * 
     * @return Formatted date/time string, or "Never" if no sync has been performed
     */
    public String getLastSyncTimeFormatted() {
        long timestamp = getLastSyncTime();
        if (timestamp == 0) {
            return "Never";
        }
        return formatTimestamp(timestamp);
    }
    
    /**
     * Check if the last sync was successful
     * 
     * @return true if the last sync was successful, false otherwise
     */
    public boolean wasLastSyncSuccessful() {
        return preferences.getBoolean(KEY_SYNC_STATUS, false);
    }
    
    /**
     * Format a timestamp as a readable date/time string
     * 
     * @param timestamp The timestamp in milliseconds
     * @return Formatted date/time string
     */
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Check if a sync is needed based on time since last sync
     * 
     * @param intervalHours Minimum hours between syncs
     * @return true if sync is needed, false otherwise
     */
    public boolean isSyncNeeded(int intervalHours) {
        long lastSync = getLastSyncTime();
        if (lastSync == 0) {
            return true; // Never synced before
        }
        
        long currentTime = System.currentTimeMillis();
        long hoursSinceLastSync = (currentTime - lastSync) / (60 * 60 * 1000);
        
        return hoursSinceLastSync >= intervalHours;
    }
} 