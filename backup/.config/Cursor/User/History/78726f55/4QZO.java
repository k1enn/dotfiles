package com.example.elsa_speak_clone.database.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.UserProgressDao;
import com.example.elsa_speak_clone.database.entities.UserProgress;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.ArrayList;

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

    // Load user streak and XP for observation
    public void loadUserMetrics(int userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<UserProgress> progressList = userProgressDao.getUserProgress(userId);
            if (progressList != null && !progressList.isEmpty()) {
                // We'll take the most recent progress for streak and XP
                UserProgress latestProgress = progressList.get(0);
                userStreak.postValue(latestProgress.getStreak());
                userXp.postValue(latestProgress.getXp());
            } else {
                userStreak.postValue(0);
                userXp.postValue(0);
            }
        });
    }


    public void updateDailyStreak(int userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserProgress userProgress = userProgressDao.getUserProgressById(userId);
            
            if (userProgress != null) {
                String lastStudyDate = userProgress.getLastStudyDate();
                String today = getCurrentDate();
                int currentStreak = userProgress.getStreak();
                
                // Simple streak logic - this would need to be enhanced for a real app
                if (!lastStudyDate.equals(today)) {
                    // Last study was not today
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    try {
                        Date lastDate = dateFormat.parse(lastStudyDate);
                        Date todayDate = dateFormat.parse(today);
                        
                        // Calculate days between
                        long diffInMillies = todayDate.getTime() - lastDate.getTime();
                        long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
                        
                        if (diffInDays == 1) {
                            // Consecutive day - increase streak
                            currentStreak++;
                        } else if (diffInDays > 1) {
                            // Missed days - reset streak
                            currentStreak = 1;
                        }
                        
                        // Update streak and last study date
                        userProgressDao.updateUserStreak(userId, currentStreak, today);
                        userStreak.postValue(currentStreak);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing dates for streak calculation", e);
                    }
                }
            } else {
                // No progress record yet, create one with streak of 1
                try {
                    UserProgress newProgress = new UserProgress(
                            generateUniqueProgressId(),
                            userId,
                            1, // Default lesson ID
                            1, // Default difficulty
                            getCurrentDate(),
                            1, // Initial streak
                            0, // Initial XP
                            getCurrentDate()
                    );
                    userProgressDao.insert(newProgress);
                    userStreak.postValue(1);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating initial user progress", e);
                }
            }
        });
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

    public UserProgressDao getUserProgressDao() {
        return userProgressDao;
    }
} 