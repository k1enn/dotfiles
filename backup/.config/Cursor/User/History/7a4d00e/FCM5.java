package com.example.elsa_speak_clone.database.firebase;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.repositories.UserProgressRepository;
import com.example.elsa_speak_clone.database.repositories.UserRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FirebaseDataManager {
    private static final String TAG = "FirebaseDataManager";
    private static FirebaseDataManager instance;
    
    // Database references
    private final FirebaseDatabase database;
    private final DatabaseReference usersRef;
    private final DatabaseReference leaderboardRef;
    
    // Local data access
    private final Context context;
    private final UserRepository userRepository;
    private final UserProgressRepository progressRepository;
    private final SessionManager sessionManager;
    
    // Database paths
    private static final String USERS_PATH = "users";
    private static final String LEADERBOARD_PATH = "leaderboard";
    
    private FirebaseDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = FirebaseDatabase.getInstance();
        this.usersRef = database.getReference(USERS_PATH);
        this.leaderboardRef = database.getReference(LEADERBOARD_PATH);
        
        // Initialize local data access
        if (context.getApplicationContext() instanceof Application) {
            Application app = (Application) context.getApplicationContext();
            this.userRepository = new UserRepository(app);
            this.progressRepository = new UserProgressRepository(app);
        } else {
            Log.e(TAG, "Context is not from an Application - using application context");
            // Fallback in case the provided context is not from an Application
            Context appContext = context.getApplicationContext();
            if (appContext instanceof Application) {
                Application app = (Application) appContext;
                this.userRepository = new UserRepository(app);
                this.progressRepository = new UserProgressRepository(app);
            } else {
                throw new IllegalArgumentException("FirebaseDataManager requires an Application context");
            }
        }
        
        this.sessionManager = new SessionManager(context);
    }
    
    public static synchronized FirebaseDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseDataManager(context);
        }
        return instance;
    }
    
    /**
     * Check if a username exists in Firebase
     */
    public CompletableFuture<Boolean> isUsernameExists(String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        leaderboardRef.orderByKey().equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        future.complete(snapshot.exists());
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking username: " + error.getMessage());
                        future.complete(false);
                    }
                });
        
        return future;
    }
    
  
    public CompletableFuture<Boolean> updateLeaderboard(String username, int userId, int streak, int xp) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Cannot update leaderboard with empty username");
            future.complete(false);
            return future;
        }
        
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("userId", Integer.valueOf(userId));
        userUpdate.put("userStreak", Integer.valueOf(streak));
        userUpdate.put("userXp", Integer.valueOf(xp));
        
       
        leaderboardRef.child(username)
            .updateChildren(userUpdate)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Leaderboard entry created/updated successfully for " + username);
                future.complete(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating/updating leaderboard entry: " + e.getMessage());
                future.complete(false);
            });
        
        return future;
    }
    
    /**
     * Get user progress from Firebase 
     */
    public CompletableFuture<Map<String, Object>> getUserProgressFromFirebase(String username) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        leaderboardRef.child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Map<String, Object> progressData = new HashMap<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                progressData.put(child.getKey(), child.getValue());
                            }
                            future.complete(progressData);
                        } else {
                            future.complete(null);
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting user progress: " + error.getMessage());
                        future.complete(null);
                    }
                });
        
        return future;
    }
    
    /**
     * Sync local user progress to Firebase after quiz completion
     */
    public void syncUserProgressAfterQuiz(int userId, int lessonId) {
        try {
            // First update local progress
            UserProgress progress = progressRepository.getUserLessonProgress(userId, lessonId);
            User user = userRepository.getUserById(userId);
            
            if (progress != null && user != null) {
                // Update local progress
                int currentXp = progress.getXp();
                int currentStreak = progress.getStreak();
                
                int newXp = currentXp;
                progress.setXp(newXp);

                progress.setStreak(currentStreak);
                
                // Update the progress in the database
                progressRepository.updateUserProgress(progress);
                
                // Update Firebase leaderboard
                updateLeaderboard(user.getName(), userId, currentStreak, getTotalUserXp(userId))
                        .thenAccept(success -> {
                            if (success) {
                                Log.d(TAG, "Progress synced to Firebase successfully");
                            } else {
                                Log.e(TAG, "Failed to sync progress to Firebase");
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing user progress: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate total XP for a user across all lessons
     */
    private int getTotalUserXp(int userId) {
        List<UserProgress> progressList = progressRepository.getUserProgressList(userId);
        int totalXp = 0;
        
        if (progressList != null) {
            for (UserProgress progress : progressList) {
                totalXp += progress.getXp();
            }
        }
        
        return totalXp;
    }
    
    /**
     * Pull user progress from Firebase to local database
     */
    public CompletableFuture<Boolean> pullUserProgressToLocal(String username, int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        getUserProgressFromFirebase(username)
                .thenAccept(progressData -> {
                    if (progressData != null) {
                        try {
                            // We have user progress data in Firebase, now update local
                            Integer firebaseStreak = (Integer) progressData.get("userStreak");
                            Integer firebaseXp = (Integer) progressData.get("userXp");
                            
                            // Get all progress for this user
                            List<UserProgress> localProgress = progressRepository.getUserProgressList(userId);
                            
                            if (localProgress != null && !localProgress.isEmpty()) {
                                // Distribute XP among lessons 
                                if (firebaseXp != null && firebaseXp > 0) {
                                    int xpPerLesson = firebaseXp / localProgress.size();
                                    int remainderXp = firebaseXp % localProgress.size();
                                    
                                    for (int i = 0; i < localProgress.size(); i++) {
                                        UserProgress progress = localProgress.get(i);
                                        // Add extra XP to first lesson if there's a remainder
                                        int lessonXp = xpPerLesson + (i == 0 ? remainderXp : 0);
                                        progress.setXp(lessonXp);
                                        
                                        // Set streak for all lessons
                                        if (firebaseStreak != null) {
                                            progress.setStreak(firebaseStreak);
                                        }
                                        
                                        progressRepository.updateUserProgress(progress);
                                    }
                                    
                                    future.complete(true);
                                } else {
                                    future.complete(false);
                                }
                            } else {
                                future.complete(false);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating local progress: " + e.getMessage(), e);
                            future.complete(false);
                        }
                    } else {
                        future.complete(false);
                    }
                });
        
        return future;
    }
    
    /**
     * Get top users for leaderboard
     */
    public CompletableFuture<Map<String, Map<String, Object>>> getTopUsers(int limit) {
        CompletableFuture<Map<String, Map<String, Object>>> future = new CompletableFuture<>();
        
        leaderboardRef.orderByChild("userXp")
                .limitToLast(limit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Map<String, Object>> topUsers = new HashMap<>();
                        
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String username = userSnapshot.getKey();
                            Map<String, Object> userData = new HashMap<>();
                            
                            for (DataSnapshot field : userSnapshot.getChildren()) {
                                userData.put(field.getKey(), field.getValue());
                            }
                            
                            topUsers.put(username, userData);
                        }
                        
                        future.complete(topUsers);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting top users: " + error.getMessage());
                        future.complete(new HashMap<>());
                    }
                });
        
        return future;
    }
    
    /**
     * Get user streak for a specific user
     */
    public int getUserStreak(int userId) {
        try {
            List<UserProgress> progressList = progressRepository.getUserProgressList(userId);
            if (progressList != null && !progressList.isEmpty()) {
                // Find the maximum streak among all progress entries
                int maxStreak = 0;
                for (UserProgress progress : progressList) {
                    if (progress.getStreak() > maxStreak) {
                        maxStreak = progress.getStreak();
                    }
                }
                return maxStreak;
            }
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user streak: " + e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Get user streak for a specific lesson
     */
    public int getUserLessonStreak(int userId, int lessonId) {
        UserProgress progress = progressRepository.getUserLessonProgress(userId, lessonId);
        return progress != null ? progress.getStreak() : 0;
    }
    
    /**
     * Check if a username exists in the Firebase leaderboard
     * @param username The username to check
     * @return CompletableFuture that completes with true if username exists in leaderboard, false otherwise
     */
    public CompletableFuture<Boolean> isUsernameExistsInLeaderboard(String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "Cannot check empty username in leaderboard");
            future.complete(false);
            return future;
        }
        
        leaderboardRef.child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean exists = snapshot.exists();
                        Log.d(TAG, "Username '" + username + "' " + 
                              (exists ? "exists" : "does not exist") + " in leaderboard");
                        future.complete(exists);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking username in leaderboard: " + error.getMessage());
                        future.complete(false);
                    }
                });
        
        return future;
    }
} 