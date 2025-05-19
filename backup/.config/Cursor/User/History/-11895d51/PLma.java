package com.example.elsa_speak_clone.database.firebase;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.repositories.UserProgressRepository;
import com.example.elsa_speak_clone.utilities.PasswordUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FirebaseUserManager {
    private static final String TAG = "FirebaseUserManager";
    private static final String USERS_NODE = "users";
    private static final String PROGRESS_NODE = "progress";
    private static final String LEADERBOARD_NODE = "leaderboard";
    
    private static FirebaseUserManager instance;
    private final DatabaseReference usersRef;
    private final DatabaseReference progressRef;
    private final DatabaseReference leaderboardRef;
    private final AppDatabase database;
    private final UserProgressRepository progressRepository;
    private final Context context;
    
    private FirebaseUserManager(Context context) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference(USERS_NODE);
        progressRef = firebaseDatabase.getReference(PROGRESS_NODE);
        leaderboardRef = firebaseDatabase.getReference(LEADERBOARD_NODE);
        
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.progressRepository = new UserProgressRepository(new Application());
    }
    
    public static synchronized FirebaseUserManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseUserManager(context);
        }
        return instance;
    }
    
    /**
     * Case 1: User Registration & Login Sync
     * Sync user data with Firebase
     */
    public CompletableFuture<Boolean> syncUserData(User user) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            // Create a map with user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("name", user.getName());
            
            // Store hashed password for security
            // In a real app, consider using Firebase Authentication instead
            userData.put("password", PasswordUtils.hashPassword(user.getPassword()));
            
            userData.put("gmail", user.getGmail());
            userData.put("google", user.getGoogle());
            userData.put("joinDate", user.getJoinDate());
            
            // Save to Firebase
            usersRef.child(String.valueOf(user.getUserId()))
                   .updateChildren(userData)
                   .addOnSuccessListener(aVoid -> {
                       Log.d(TAG, "User data synced successfully for user: " + user.getUserId());
                       future.complete(true);
                   })
                   .addOnFailureListener(e -> {
                       Log.e(TAG, "Error syncing user data: " + e.getMessage(), e);
                       future.complete(false);
                   });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing user data: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Case 2: XP & Streak Auto-Sync
     * Sync user progress (XP and streak)
     */
    public CompletableFuture<Boolean> syncUserProgress(int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            // Get user progress from local database
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    UserProgress progress = database.userProgressDao().getUserProgressById(userId);
                    
                    if (progress == null) {
                        Log.d(TAG, "No progress found for user: " + userId);
                        future.complete(false);
                        return;
                    }
                    
                    // Get user name for leaderboard update
                    User user = database.userDao().getUserById(userId);
                    String userName = (user != null) ? user.getName() : "User " + userId;
                    
                    // Create progress data map
                    Map<String, Object> progressData = new HashMap<>();
                    progressData.put("userId", progress.getUserId());
                    progressData.put("xp", progress.getXp());
                    progressData.put("streak", progress.getStreak());
                    progressData.put("lastStudyDate", progress.getLastStudyDate());
                    // Save to Firebase under progress node
                    progressRef.child(String.valueOf(userId))
                           .updateChildren(progressData)
                           .addOnSuccessListener(aVoid -> {
                               Log.d(TAG, "Progress synced successfully for user: " + userId);
                               
                               // Also update the leaderboard entry
                               updateLeaderboardEntry(userId, userName, progress.getXp(), progress.getStreak());
                               
                               future.complete(true);
                           })
                           .addOnFailureListener(e -> {
                               Log.e(TAG, "Error syncing progress: " + e.getMessage(), e);
                               future.complete(false);
                           });
                } catch (Exception e) {
                    Log.e(TAG, "Error getting user progress: " + e.getMessage(), e);
                    future.complete(false);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error syncing user progress: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Update leaderboard entry for a user
     */
    private void updateLeaderboardEntry(int userId, String name, int xp, int streak) {
        Map<String, Object> leaderboardData = new HashMap<>();
        leaderboardData.put("userId", userId);
        leaderboardData.put("name", name);
        leaderboardData.put("xp", xp);
        leaderboardData.put("streak", streak);
        
        leaderboardRef.child(String.valueOf(userId))
                      .updateChildren(leaderboardData)
                      .addOnSuccessListener(aVoid -> 
                          Log.d(TAG, "Leaderboard updated for user: " + userId))
                      .addOnFailureListener(e -> 
                          Log.e(TAG, "Error updating leaderboard: " + e.getMessage(), e));
    }
    
    /**
     * Case 3: Daily Streak Reset at Midnight
     * Check if streak needs to be reset due to inactivity
     */
    public CompletableFuture<Boolean> checkAndResetStreak(int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                UserProgress progress = database.userProgressDao().getUserProgressById(userId);
                
                if (progress == null) {
                    Log.d(TAG, "No progress found for user: " + userId);
                    future.complete(false);
                    return;
                }
                
                String lastStudyDate = progress.getLastStudyDate();
                String today = getCurrentDate();
                
                if (shouldResetStreak(lastStudyDate, today)) {
                    // Reset streak to 0 if user missed a day
                    progress.setStreak(0);
                    database.userProgressDao().updateUserProgress(progress);
                    
                    // Sync the reset to Firebase
                    syncUserProgress(userId);
                    
                    Log.d(TAG, "Reset streak for user: " + userId);
                    future.complete(true);
                } else {
                    future.complete(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking streak: " + e.getMessage(), e);
                future.complete(false);
            }
        });
        
        return future;
    }
    
    /**
     * Determine if streak should be reset by checking days between last study and today
     */
    private boolean shouldResetStreak(String lastStudyDate, String today) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date lastDate = dateFormat.parse(lastStudyDate);
            Date todayDate = dateFormat.parse(today);
            
            // Calculate days between dates
            long diffInMillies = Math.abs(todayDate.getTime() - lastDate.getTime());
            long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
            
            // Reset streak if more than 1 day has passed
            return diffInDays > 1;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Case 4: Leaderboard Sync (Based on XP)
     * Get top users for leaderboard
     */
    public CompletableFuture<List<Map<String, Object>>> getLeaderboard(int limit) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        
        // Query users ordered by XP, limited to the requested number
        Query query = leaderboardRef.orderByChild("xp").limitToLast(limit);
        
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Map<String, Object>> leaderboardData = new ArrayList<>();
                
                // We need to manually reverse the list since Firebase sorts ascending
                // but we want descending order for the leaderboard
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", userSnapshot.child("userId").getValue(Integer.class));
                    userData.put("name", userSnapshot.child("name").getValue(String.class));
                    userData.put("xp", userSnapshot.child("xp").getValue(Integer.class));
                    userData.put("streak", userSnapshot.child("streak").getValue(Integer.class));
                    
                    // Add to beginning of list for descending order
                    leaderboardData.add(0, userData);
                }
                
                future.complete(leaderboardData);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting leaderboard: " + error.getMessage());
                future.complete(new ArrayList<>());
            }
        });
        
        return future;
    }
    
    /**
     * Case 5: Password Update Sync
     * Update user password
     */
    public CompletableFuture<Boolean> updatePassword(int userId, String newPassword) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            // Hash password for security
            String hashedPassword = PasswordUtils.hashPassword(newPassword);
            
            // Update in Firebase
            Map<String, Object> updates = new HashMap<>();
            updates.put("password", hashedPassword);
            
            usersRef.child(String.valueOf(userId))
                   .updateChildren(updates)
                   .addOnSuccessListener(aVoid -> {
                       Log.d(TAG, "Password updated successfully for user: " + userId);
                       future.complete(true);
                   })
                   .addOnFailureListener(e -> {
                       Log.e(TAG, "Error updating password: " + e.getMessage(), e);
                       future.complete(false);
                   });
        } catch (Exception e) {
            Log.e(TAG, "Error updating password: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Case 6: Offline to Online Sync
     * Restore user data from Firebase when online
     */
    public CompletableFuture<User> getUserFromFirebase(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        // Query users by email
        Query query = usersRef.orderByChild("gmail").equalTo(email);
        
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = new User();
                        
                        user.setUserId(userSnapshot.child("userId").getValue(Integer.class));
                        user.setName(userSnapshot.child("name").getValue(String.class));
                        user.setGmail(userSnapshot.child("gmail").getValue(String.class));
                        user.setPassword(userSnapshot.child("password").getValue(String.class));
                        user.setGoogle(userSnapshot.child("google").getValue(Boolean.class));
                        user.setJoinDate(userSnapshot.child("joinDate").getValue(String.class));
                        
                        Log.d(TAG, "Found user in Firebase: " + user.getUserId());
                        future.complete(user);
                        return;
                    }
                }
                
                Log.d(TAG, "User not found in Firebase for email: " + email);
                future.complete(null);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting user from Firebase: " + error.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }
    
    /**
     * Restore user progress from Firebase
     */
    public CompletableFuture<Boolean> restoreUserProgress(int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        progressRef.child(String.valueOf(userId))
                  .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        final Integer xp = snapshot.child("xp").getValue(Integer.class);
                        final Integer streak = snapshot.child("streak").getValue(Integer.class);
                        final String lastStudyDate = snapshot.child("lastStudyDate").getValue(String.class);
                        
                        // Save to local database
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            try {
                                UserProgress existingProgress = database.userProgressDao().getUserProgressById(userId);
                                
                                if (existingProgress == null) {
                                    // Create new progress entry
                                    UserProgress newProgress = new UserProgress();
                                    newProgress.setUserId(userId);
                                    newProgress.setXp(xp != null ? xp : 0);
                                    newProgress.setStreak(streak != null ? streak : 0);
                                    newProgress.setLastStudyDate(lastStudyDate != null ? lastStudyDate : getCurrentDate());
                                    newProgress.setLessonId(1); // Default lesson
                                    newProgress.setProgressId(generateProgressId(userId));
                                    
                                    database.userProgressDao().insertUserProgress(newProgress);
                                } else {
                                    // Update existing progress if Firebase has higher values
                                    if (xp != null && xp > existingProgress.getXp()) {
                                        existingProgress.setXp(xp);
                                    }
                                    
                                    if (streak != null && streak > existingProgress.getStreak()) {
                                        existingProgress.setStreak(streak);
                                    }
                                    
                                    if (lastStudyDate != null) {
                                        existingProgress.setLastStudyDate(lastStudyDate);
                                    }
                                    
                                    database.userProgressDao().updateUserProgress(existingProgress);
                                }
                                
                                Log.d(TAG, "Restored progress for user: " + userId);
                                future.complete(true);
                            } catch (Exception e) {
                                Log.e(TAG, "Error saving progress to local database: " + e.getMessage(), e);
                                future.complete(false);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing progress data: " + e.getMessage(), e);
                        future.complete(false);
                    }
                } else {
                    Log.d(TAG, "No progress found in Firebase for user: " + userId);
                    future.complete(false);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting progress from Firebase: " + error.getMessage());
                future.complete(false);
            }
        });
        
        return future;
    }
    
    /**
     * Generate a unique progress ID based on user ID
     */
    private int generateProgressId(int userId) {
        return userId * 1000 + (int)(Math.random() * 999);
    }
    
    /**
     * Get current date in yyyy-MM-dd format
     */
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }
    
    /**
     * Get user ID from username (from Firebase)
     */
    public CompletableFuture<Integer> getUserIdFromUsername(String username) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        // Query users by username
        Query query = usersRef.orderByChild("name").equalTo(username);
        
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        Integer userId = userSnapshot.child("userId").getValue(Integer.class);
                        if (userId != null) {
                            Log.d(TAG, "Found user ID in Firebase: " + userId + " for username: " + username);
                            future.complete(userId);
                            return;
                        }
                    }
                }
                
                Log.d(TAG, "User not found in Firebase for username: " + username);
                future.complete(-1); // Invalid user ID
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting user from Firebase: " + error.getMessage());
                future.complete(-1);
            }
        });
        
        return future;
    }
    
    /**
     * Sync user progress by username
     */
    public CompletableFuture<Boolean> syncUserProgress(String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        // First get user ID from username
        getUserIdFromUsername(username)
            .thenCompose(userId -> {
                if (userId > 0) {
                    // User found, now sync progress by ID
                    return syncUserProgress(userId);
                } else {
                    Log.e(TAG, "Failed to find user ID for username: " + username);
                    CompletableFuture<Boolean> result = new CompletableFuture<>();
                    result.complete(false);
                    return result;
                }
            })
            .thenAccept(future::complete)
            .exceptionally(e -> {
                Log.e(TAG, "Error syncing progress by username: " + e.getMessage(), e);
                future.complete(false);
                return null;
            });
        
        return future;
    }
    
    /**
     * Restore user progress by username
     */
    public CompletableFuture<Boolean> restoreUserProgress(String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        // First get user ID from username
        getUserIdFromUsername(username)
            .thenCompose(userId -> {
                if (userId > 0) {
                    // User found, now restore progress by ID
                    return restoreUserProgress(userId);
                } else {
                    Log.e(TAG, "Failed to find user ID for username: " + username);
                    CompletableFuture<Boolean> result = new CompletableFuture<>();
                    result.complete(false);
                    return result;
                }
            })
            .thenAccept(future::complete)
            .exceptionally(e -> {
                Log.e(TAG, "Error restoring progress by username: " + e.getMessage(), e);
                future.complete(false);
                return null;
            });
        
        return future;
    }
    
    /**
     * Check if a username exists in Firebase
     */
    public CompletableFuture<Boolean> checkUsernameExistsInFirebase(String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        // Query users by username
        Query query = usersRef.orderByChild("name").equalTo(username);
        
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean exists = snapshot.exists() && snapshot.getChildrenCount() > 0;
                Log.d(TAG, "Username " + username + " exists in Firebase: " + exists);
                future.complete(exists);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking if username exists in Firebase: " + error.getMessage());
                future.complete(false); // Default to not existing on error
            }
        });
        
        return future;
    }
    
    /**
     * Check if an email exists in Firebase
     */
    public CompletableFuture<Boolean> checkEmailExistsInFirebase(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        // Query users by email
        Query query = usersRef.orderByChild("gmail").equalTo(email);
        
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean exists = snapshot.exists() && snapshot.getChildrenCount() > 0;
                Log.d(TAG, "Email " + email + " exists in Firebase: " + exists);
                future.complete(exists);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking if email exists in Firebase: " + error.getMessage());
                future.complete(false); // Default to not existing on error
            }
        });
        
        return future;
    }
    
    /**
     * Check if a user ID exists in Firebase
     */
    public CompletableFuture<Boolean> checkUserIdExistsInFirebase(int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        usersRef.child(String.valueOf(userId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                Log.d(TAG, "User ID " + userId + " exists in Firebase: " + exists);
                future.complete(exists);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking if user ID exists in Firebase: " + error.getMessage());
                future.complete(false); // Default to not existing on error
            }
        });
        
        return future;
    }
} 