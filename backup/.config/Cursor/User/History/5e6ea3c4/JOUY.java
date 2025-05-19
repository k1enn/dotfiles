package com.example.elsa_speak_clone.database.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.entities.UserScore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    
    // Database references
    private final FirebaseDatabase database;
    private final DatabaseReference usersRef;
    private final DatabaseReference progressRef;
    private final DatabaseReference scoresRef;
    
    // Paths in the database
    private static final String USERS_PATH = "users";
    private static final String PROGRESS_PATH = "progress";
    private static final String SCORES_PATH = "scores";
    
    private FirebaseManager(Context context) {
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(USERS_PATH);
        progressRef = database.getReference(PROGRESS_PATH);
        scoresRef = database.getReference(SCORES_PATH);
    }
    
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new FirebaseManager(context.getApplicationContext());
            Log.d(TAG, "FirebaseManager initialized");
        }
    }
    
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FirebaseManager not initialized. Call initialize(context) first.");
        }
        return instance;
    }
    
    // -------------------- User Operations --------------------
    
    /**
     * Save a user to Firebase
     */
    public CompletableFuture<Boolean> saveUser(User user) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            // Create a map of user data (excluding password for security)
            Map<String, Object> userValues = new HashMap<>();
            userValues.put("userId", user.getUserId());
            userValues.put("gmail", user.getGmail());
            userValues.put("name", user.getName());
            userValues.put("google", user.getGoogle());
            userValues.put("joinDate", user.getJoinDate());
            
            // Generate a safe key from the email
            String userKey = sanitizeKey(user.getGmail());
            
            // Save to Firebase
            usersRef.child(userKey).setValue(userValues)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User saved successfully: " + user.getName());
                        future.complete(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving user: " + e.getMessage(), e);
                        future.complete(false);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in saveUser: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Get a user from Firebase by email
     */
    public CompletableFuture<User> getUserByEmail(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        try {
            String userKey = sanitizeKey(email);
            usersRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = new User();
                        
                        user.setUserId(snapshot.child("userId").getValue(Integer.class));
                        user.setGmail(snapshot.child("gmail").getValue(String.class));
                        user.setName(snapshot.child("name").getValue(String.class));
                        user.setGoogle(snapshot.child("google").getValue(Boolean.class));
                        user.setJoinDate(snapshot.child("joinDate").getValue(String.class));
                        
                        future.complete(user);
                    } else {
                        future.complete(null);
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error getting user: " + error.getMessage());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserByEmail: " + e.getMessage(), e);
            future.complete(null);
        }
        
        return future;
    }
    
    // -------------------- Progress Operations --------------------
    
    /**
     * Save user progress to Firebase
     */
    public CompletableFuture<Boolean> saveUserProgress(UserProgress progress) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            // Create progress key based on user ID and progress ID
            String progressKey = progress.getUserId() + "_" + progress.getProgressId();
            
            // Create a map of progress data
            Map<String, Object> progressValues = new HashMap<>();
            progressValues.put("progressId", progress.getProgressId());
            progressValues.put("userId", progress.getUserId());
            progressValues.put("lessonId", progress.getLessonId());
            progressValues.put("difficultyLevel", progress.getDifficultyLevel());
            progressValues.put("completionTime", progress.getCompletionTime());
            progressValues.put("streak", progress.getStreak());
            progressValues.put("xp", progress.getXp());
            progressValues.put("lastStudyDate", progress.getLastStudyDate());
            
            // Save to Firebase
            progressRef.child(progressKey).setValue(progressValues)
                    .addOnSuccessListener(aVoid -> {
                        future.complete(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving progress: " + e.getMessage(), e);
                        future.complete(false);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in saveUserProgress: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Get all progress for a user
     */
    public CompletableFuture<List<UserProgress>> getUserProgress(int userId) {
        CompletableFuture<List<UserProgress>> future = new CompletableFuture<>();
        
        try {
            // Query progress where userId matches
            progressRef.orderByChild("userId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<UserProgress> progressList = new ArrayList<>();
                            
                            for (DataSnapshot progressSnapshot : snapshot.getChildren()) {
                                UserProgress progress = new UserProgress();
                                
                                progress.setProgressId(progressSnapshot.child("progressId").getValue(Integer.class));
                                progress.setUserId(progressSnapshot.child("userId").getValue(Integer.class));
                                progress.setLessonId(progressSnapshot.child("lessonId").getValue(Integer.class));
                                progress.setDifficultyLevel(progressSnapshot.child("difficultyLevel").getValue(Integer.class));
                                progress.setCompletionTime(progressSnapshot.child("completionTime").getValue(String.class));
                                progress.setStreak(progressSnapshot.child("streak").getValue(Integer.class));
                                progress.setXp(progressSnapshot.child("xp").getValue(Integer.class));
                                progress.setLastStudyDate(progressSnapshot.child("lastStudyDate").getValue(String.class));
                                
                                progressList.add(progress);
                            }
                            
                            future.complete(progressList);
                        }
                        
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error getting user progress: " + error.getMessage());
                            future.complete(new ArrayList<>());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserProgress: " + e.getMessage(), e);
            future.complete(new ArrayList<>());
        }
        
        return future;
    }
    
    // -------------------- Score Operations --------------------
    
    /**
     * Save user score to Firebase
     */
    public CompletableFuture<Boolean> saveUserScore(UserScore score) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            // Create score key based on user ID and score ID
            String scoreKey = score.getUserId() + "_" + score.getScoreId();
            
            // Create a map of score data
            Map<String, Object> scoreValues = new HashMap<>();
            scoreValues.put("scoreId", score.getScoreId());
            scoreValues.put("userId", score.getUserId());
            scoreValues.put("quizId", score.getQuizId());
            scoreValues.put("score", score.getScore());
            scoreValues.put("attemptDate", score.getAttemptDate());
            
            // Save to Firebase
            scoresRef.child(scoreKey).setValue(scoreValues)
                    .addOnSuccessListener(aVoid -> {
                        future.complete(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving score: " + e.getMessage(), e);
                        future.complete(false);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in saveUserScore: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Get all scores for a user
     */
    public CompletableFuture<List<UserScore>> getUserScores(int userId) {
        CompletableFuture<List<UserScore>> future = new CompletableFuture<>();
        
        try {
            // Query scores where userId matches
            scoresRef.orderByChild("userId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<UserScore> scoresList = new ArrayList<>();
                            
                            for (DataSnapshot scoreSnapshot : snapshot.getChildren()) {
                                UserScore score = new UserScore();
                                
                                score.setScoreId(scoreSnapshot.child("scoreId").getValue(Integer.class));
                                score.setUserId(scoreSnapshot.child("userId").getValue(Integer.class));
                                score.setQuizId(scoreSnapshot.child("quizId").getValue(Integer.class));
                                score.setScore(scoreSnapshot.child("score").getValue(Integer.class));
                                score.setAttemptDate(scoreSnapshot.child("attemptDate").getValue(String.class));
                                
                                scoresList.add(score);
                            }
                            
                            future.complete(scoresList);
                        }
                        
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error getting user scores: " + error.getMessage());
                            future.complete(new ArrayList<>());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserScores: " + e.getMessage(), e);
            future.complete(new ArrayList<>());
        }
        
        return future;
    }
    
    // -------------------- Utility Methods --------------------
    
    /**
     * Sanitize key for Firebase (remove invalid characters)
     */
    private String sanitizeKey(String key) {
        if (key == null) {
            return "";
        }
        return key.replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_");
    }
} 