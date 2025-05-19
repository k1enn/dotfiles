package com.example.elsa_speak_clone.database.repositories;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.UserProgressDao;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.firebase.FirebaseDataManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class UserProgressRepository {
    private static final String TAG = "UserProgressRepository";
    private final UserProgressDao userProgressDao;
    private final AppDatabase database;

    // Properties
    private final MutableLiveData<List<UserProgress>> allUserProgress = new MutableLiveData<>();
    private final MutableLiveData<UserProgress> currentProgress = new MutableLiveData<>();
    private final MutableLiveData<Integer> userStreak = new MutableLiveData<>();
    private final MutableLiveData<Integer> userXp = new MutableLiveData<>();

    public UserProgressRepository(Application application) {
        database = AppDatabase.getInstance(application);
        userProgressDao = database.userProgressDao();
    }
    public UserProgressRepository(Context context) {
        database = AppDatabase.getInstance(context);
        userProgressDao = database.userProgressDao();
    }

    // Get LiveData for observing user progress
    public LiveData<List<UserProgress>> getAllUserProgress(int userId) {
        refreshUserProgress(userId);
        return allUserProgress;
    }

    public LiveData<UserProgress> getCurrentProgress() {
        return currentProgress;
    }

    public LiveData<Integer> getUserStreak() {
        return userStreak;
    }

    public LiveData<Integer> getUserXp() {
        return userXp;
    }

    // Update methods
    public void updateUserStreak(int newStreak) {
        userStreak.setValue(newStreak);
    }

    public void updateUserXp(int newXp) {
        userXp.setValue(newXp);
    }

    // Refresh the LiveData with the latest data from the database
    public void refreshUserProgress(int userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<UserProgress> progress = userProgressDao.getUserProgress(userId);
            allUserProgress.postValue(progress);
        });
    }

    // Get user progress for a specific lesson
    public UserProgress getUserLessonProgress(int userId, int lessonId) {
        try {
            Future<UserProgress> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userProgressDao.getUserLessonProgress(userId, lessonId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting user lesson progress", e);
            return null;
        }
    }

    // Load user streak and XP
    public void loadUserMetrics(int userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<UserProgress> progressList = userProgressDao.getUserProgress(userId);
            if (progressList != null && !progressList.isEmpty()) {
                // Take most recent progress
                UserProgress latestProgress = progressList.get(0);
                userStreak.postValue(latestProgress.getStreak());
                userXp.postValue(latestProgress.getXp());
            } else {
                userStreak.postValue(0);
                userXp.postValue(0);
            }
        });
    }

    public void updateStreakAndSyncToFirebase(int userId, String username) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // First update the streak locally
                updateDailyStreak(userId);

                // Get the updated streak and xp to sync back to Firebase
                UserProgress progress = userProgressDao.getUserProgressById(userId);
                if (progress != null && username != null && !username.isEmpty()) {
                    // Use FirebaseDataManager to update Firebase
                    FirebaseDataManager.getInstance(null).updateUserStats(
                            username,
                            progress.getXp(),
                            progress.getStreak()
                    );
                    Log.d(TAG, "Synced updated streak to Firebase: " + progress.getStreak());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating streak and syncing to Firebase", e);
            }
        });
    }
    public long getDaysBetweenDates(Date date1, Date date2) {
        long diffInMillis = Math.abs(date2.getTime() - date1.getTime());
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }
    public void updateDailyStreak(int userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // First check if we have a Firebase user
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                String firebaseUid = firebaseUser != null ? firebaseUser.getUid() : null;

                UserProgress userProgress = userProgressDao.getUserProgressById(userId);
                String today = getCurrentDate();

                if (userProgress != null) {
                    String lastStudyDate = userProgress.getLastStudyDate();
                    int currentStreak = userProgress.getStreak();

                    // Only process if there's a valid last study date
                    if (lastStudyDate != null && !lastStudyDate.isEmpty()) {
                        // Skip processing if user already visited today
                        if (lastStudyDate.equals(today)) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            dateFormat.setLenient(false); // Strict date parsing

                            try {
                                Date lastDate = dateFormat.parse(lastStudyDate);
                                Date todayDate = dateFormat.parse(today);

                                if (lastDate != null && todayDate != null) {
                                    long diffInDays = getDaysBetweenDates(lastDate, todayDate);
                                    Log.d(TAG, "diffInDays: " + diffInDays);

                                    if (diffInDays == 1) {
                                        // Consecutive day - increase streak
                                        currentStreak++;
                                        Log.d(TAG, "Increasing streak to: " + currentStreak);
                                    } else if (diffInDays > 1){
                                        // Missed days - reset streak
                                        currentStreak = 1;
                                        Log.d(TAG, "Resetting streak to 1 (missed " + diffInDays + " days)");
                                    }

                                    // Update streak and last study date
                                    userProgressDao.updateUserStreak(userId, currentStreak, today);

                                    // If we have a Firebase user, also update data in Firebase
                                    if (firebaseUid != null) {
                                        updateStreakInFirebase(firebaseUid, userId, currentStreak, today);
                                    }

                                    // Update LiveData on main thread
                                    userStreak.postValue(currentStreak);

                                    Log.d(TAG, "Updated streak: " + currentStreak + " for user: " + userId);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing dates for streak calculation", e);
                            }
                        } else {
                            Log.d(TAG, "User already studied today, streak remains: " + currentStreak);
                        }
                    } else {
                        // Invalid last study date, update it to today and keep current streak
                        userProgressDao.updateUserStreak(userId, currentStreak, today);

                        // Also update Firebase if applicable
                        if (firebaseUid != null) {
                            updateStreakInFirebase(firebaseUid, userId, currentStreak, today);
                        }

                        Log.d(TAG, "Updated last study date to today, streak unchanged: " + currentStreak);
                    }
                } else {
                    // No progress record yet, create one with streak of 1
                    Log.d(TAG, "Creating initial progress record for user: " + userId);

                    int progressId = generateUniqueProgressId();
                    UserProgress newProgress = new UserProgress(
                            progressId,
                            userId,
                            1, // Default lesson ID
                            1, // Default difficulty
                            getCurrentDate(),
                            1, // Initial streak
                            0, // Initial XP
                            getCurrentDate()
                    );
                    userProgressDao.insert(newProgress);

                    // Also create in Firebase if applicable
                    if (firebaseUid != null) {
                        createInitialProgressInFirebase(firebaseUid, userId, progressId);
                    }

                    userStreak.postValue(1);
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in updateDailyStreak", e);
            }
        });
    }

    // Helper method to update streak in Firebase
    private void updateStreakInFirebase(String firebaseUid, int userId, int streak, String lastStudyDate) {
        try {
            // Use FirebaseDataManager and usersTableRef instead of direct Firebase reference
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                String userEmail = firebaseUser.getEmail();
                String username = userEmail != null ? userEmail.split("@")[0] : firebaseUid;
                
                // Use FirebaseDataManager's updateUserStats method which uses usersTableRef
                FirebaseDataManager.getInstance(null).updateUserStats(
                        username,
                        userProgressDao.getUserProgressById(userId).getXp(), // Keep existing XP
                        streak  // Update streak
                ).thenAccept(success -> {
                    if (success) {
                        Log.d(TAG, "Successfully updated streak in Firebase for: " + username);
                    } else {
                        Log.e(TAG, "Failed to update streak in Firebase for: " + username);
                    }
                });
            } else {
                Log.e(TAG, "Cannot update Firebase streak: No authenticated user");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating Firebase streak", e);
        }
    }

    // Helper method to create initial progress in Firebase
    private void createInitialProgressInFirebase(String firebaseUid, int userId, int progressId) {
        try {
            // Get Firebase user to determine username
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                String userEmail = firebaseUser.getEmail();
                String username = userEmail != null ? userEmail.split("@")[0] : firebaseUid;
                
                // Use FirebaseDataManager which manages the correct references
                FirebaseDataManager.getInstance(null).updateUserStats(
                        username,
                        0, // Initial XP
                        1  // Initial streak
                ).thenAccept(success -> {
                    if (success) {
                        Log.d(TAG, "Successfully created initial progress in Firebase for: " + username);
                    } else {
                        Log.e(TAG, "Failed to create initial progress in Firebase for: " + username);
                    }
                });
            } else {
                Log.e(TAG, "Cannot create Firebase progress: No authenticated user");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating initial Firebase progress", e);
        }
    }

    // Helper method to generate a unique progress ID
    private int generateUniqueProgressId() {
        Random random = new Random();
        return 100000 + random.nextInt(900000); // 6-digit ID
    }

    // Helper method to get current date as string
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    // Check if user has any progress records
    public boolean hasUserProgress(int userId) {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userProgressDao.countUserProgressEntries(userId));
            return future.get() > 0;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error checking user progress", e);
            return false;
        }
    }

    /**
     * Get all progress entries for a user as a list (not LiveData)
     */
    public List<UserProgress> getUserProgressList(int userId) {
        try {
            Future<List<UserProgress>> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userProgressDao.getUserProgress(userId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting user progress list", e);
            return new ArrayList<>();
        }
    }

    /**
     * Update a specific user progress entry (used for syncing)
     */
    public boolean updateUserProgress(UserProgress progress) {
        try {
            AppDatabase.databaseWriteExecutor.execute(() -> 
                    userProgressDao.update(progress));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user progress", e);
            return false;
        }
    }

    /**
     * Insert a user progress entry (used for syncing)
     */
    public long insertUserProgress(UserProgress progress) {
        try {
            Future<Long> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userProgressDao.insert(progress));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error inserting user progress", e);
            return -1;
        }
    }

    /**
     * Insert a new progress record
     */
    public void insertProgress(UserProgress progress) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userProgressDao.insert(progress);
        });
    }

    /**
     * Update an existing progress record
     */
    public void updateProgress(UserProgress progress) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userProgressDao.update(progress);
        });
    }

    public UserProgressDao getUserProgressDao() {
        return userProgressDao;
    }
} 