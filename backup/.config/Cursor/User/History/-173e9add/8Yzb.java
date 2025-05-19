package com.example.elsa_speak_clone.database.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.elsa_speak_clone.database.entities.Lesson;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.database.entities.SharedResult;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.entities.UserScore;
import com.example.elsa_speak_clone.database.entities.Vocabulary;
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

public class FirebaseDbHelper {
    private static final String TAG = "FirebaseDbHelper";
    private final FirebaseDatabase database;
    private final DatabaseReference rootRef;
    
    // Database paths
    private static final String USERS_PATH = "users";
    private static final String PROGRESS_PATH = "progress";
    private static final String SCORES_PATH = "scores";
    private static final String SHARED_RESULTS_PATH = "shared_results";

    private static FirebaseDbHelper instance;

    private FirebaseDbHelper() {
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference();
    }

    public static synchronized FirebaseDbHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseDbHelper();
        }
        return instance;
    }
    
    // -------------------------- User Operations --------------------------
    
    /**
     * Save user profile to Firebase
     */
    public CompletableFuture<Boolean> saveUser(User user) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            // We'll use email as a key (after sanitizing)
            String userKey = sanitizeKey(user.getGmail());
            DatabaseReference userRef = rootRef.child(USERS_PATH).child(userKey);
            
            Map<String, Object> userValues = new HashMap<>();
            userValues.put("userId", user.getUserId());
            userValues.put("name", user.getName());
            userValues.put("gmail", user.getGmail());
            userValues.put("google", user.getGoogle());
            userValues.put("dateCreated", user.getDateCreated());
            userValues.put("lastLogin", user.getLastLogin());
            
            userRef.setValue(userValues)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved successfully");
                    future.complete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user", e);
                    future.complete(false);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in saveUser", e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Get user by email
     */
    public CompletableFuture<User> getUserByEmail(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        try {
            String userKey = sanitizeKey(email);
            DatabaseReference userRef = rootRef.child(USERS_PATH).child(userKey);
            
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            User user = new User();
                            user.setUserId(snapshot.child("userId").getValue(Integer.class));
                            user.setName(snapshot.child("name").getValue(String.class));
                            user.setGmail(snapshot.child("gmail").getValue(String.class));
                            user.setGoogle(snapshot.child("google").getValue(Boolean.class));
                            user.setDateCreated(snapshot.child("dateCreated").getValue(String.class));
                            user.setLastLogin(snapshot.child("lastLogin").getValue(String.class));
                            
                            future.complete(user);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user data", e);
                            future.complete(null);
                        }
                    } else {
                        Log.d(TAG, "User not found");
                        future.complete(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserByEmail", e);
            future.complete(null);
        }
        
        return future;
    }
    
    // -------------------------- Progress Operations --------------------------
    
    /**
     * Save user progress to Firebase
     */
    public CompletableFuture<Boolean> saveUserProgress(UserProgress progress) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            String userKey = String.valueOf(progress.getUserId());
            DatabaseReference progressRef = rootRef.child(PROGRESS_PATH)
                    .child(userKey)
                    .child(String.valueOf(progress.getLessonId()));
            
            Map<String, Object> progressValues = new HashMap<>();
            progressValues.put("progressId", progress.getProgressId());
            progressValues.put("userId", progress.getUserId());
            progressValues.put("lessonId", progress.getLessonId());
            progressValues.put("difficultyLevel", progress.getDifficultyLevel());
            progressValues.put("completionTime", progress.getCompletionTime());
            progressValues.put("streak", progress.getStreak());
            progressValues.put("xp", progress.getXp());
            progressValues.put("lastStudyDate", progress.getLastStudyDate());
            
            progressRef.setValue(progressValues)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Progress saved successfully");
                    future.complete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving progress", e);
                    future.complete(false);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in saveUserProgress", e);
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
            String userKey = String.valueOf(userId);
            DatabaseReference progressRef = rootRef.child(PROGRESS_PATH).child(userKey);
            
            progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<UserProgress> progressList = new ArrayList<>();
                    
                    for (DataSnapshot progressSnapshot : snapshot.getChildren()) {
                        try {
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
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing progress data", e);
                        }
                    }
                    
                    future.complete(progressList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                    future.complete(new ArrayList<>());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserProgress", e);
            future.complete(new ArrayList<>());
        }
        
        return future;
    }
    
    // -------------------------- UserScore Operations --------------------------
    
    /**
     * Save user score to Firebase
     */
    public CompletableFuture<Boolean> saveUserScore(UserScore score) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            String userKey = String.valueOf(score.getUserId());
            DatabaseReference scoreRef = rootRef.child(SCORES_PATH)
                    .child(userKey)
                    .child(String.valueOf(score.getScoreId()));
            
            Map<String, Object> scoreValues = new HashMap<>();
            scoreValues.put("scoreId", score.getScoreId());
            scoreValues.put("userId", score.getUserId());
            scoreValues.put("quizId", score.getQuizId());
            scoreValues.put("score", score.getScore());
            scoreValues.put("attemptDate", score.getAttemptDate());
            scoreValues.put("lessonId", score.getLessonId());
            
            scoreRef.setValue(scoreValues)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Score saved successfully");
                    future.complete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving score", e);
                    future.complete(false);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in saveUserScore", e);
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
            String userKey = String.valueOf(userId);
            DatabaseReference scoresRef = rootRef.child(SCORES_PATH).child(userKey);
            
            scoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<UserScore> scoresList = new ArrayList<>();
                    
                    for (DataSnapshot scoreSnapshot : snapshot.getChildren()) {
                        try {
                            UserScore score = new UserScore();
                            score.setScoreId(scoreSnapshot.child("scoreId").getValue(Integer.class));
                            score.setUserId(scoreSnapshot.child("userId").getValue(Integer.class));
                            score.setQuizId(scoreSnapshot.child("quizId").getValue(Integer.class));
                            score.setScore(scoreSnapshot.child("score").getValue(Integer.class));
                            score.setAttemptDate(scoreSnapshot.child("attemptDate").getValue(String.class));
                            score.setLessonId(scoreSnapshot.child("lessonId").getValue(Integer.class));
                            
                            scoresList.add(score);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing score data", e);
                        }
                    }
                    
                    future.complete(scoresList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                    future.complete(new ArrayList<>());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserScores", e);
            future.complete(new ArrayList<>());
        }
        
        return future;
    }
    
    // -------------------------- Shared Results Operations --------------------------
    
    /**
     * Save shared result to Firebase
     */
    public CompletableFuture<Boolean> saveSharedResult(SharedResult result) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            String userKey = String.valueOf(result.getUserId());
            DatabaseReference resultRef = rootRef.child(SHARED_RESULTS_PATH)
                    .child(userKey)
                    .child(String.valueOf(result.getShareId()));
            
            Map<String, Object> resultValues = new HashMap<>();
            resultValues.put("shareId", result.getShareId());
            resultValues.put("userId", result.getUserId());
            resultValues.put("message", result.getMessage());
            resultValues.put("shareDate", result.getShareDate());
            
            resultRef.setValue(resultValues)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Shared result saved successfully");
                    future.complete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving shared result", e);
                    future.complete(false);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in saveSharedResult", e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Get shared results for a user
     */
    public CompletableFuture<List<SharedResult>> getSharedResults(int userId) {
        CompletableFuture<List<SharedResult>> future = new CompletableFuture<>();
        
        try {
            String userKey = String.valueOf(userId);
            DatabaseReference resultsRef = rootRef.child(SHARED_RESULTS_PATH).child(userKey);
            
            resultsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<SharedResult> resultsList = new ArrayList<>();
                    
                    for (DataSnapshot resultSnapshot : snapshot.getChildren()) {
                        try {
                            SharedResult result = new SharedResult();
                            result.setShareId(resultSnapshot.child("shareId").getValue(Integer.class));
                            result.setUserId(resultSnapshot.child("userId").getValue(Integer.class));
                            result.setMessage(resultSnapshot.child("message").getValue(String.class));
                            result.setShareDate(resultSnapshot.child("shareDate").getValue(String.class));
                            
                            resultsList.add(result);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing shared result data", e);
                        }
                    }
                    
                    future.complete(resultsList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                    future.complete(new ArrayList<>());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in getSharedResults", e);
            future.complete(new ArrayList<>());
        }
        
        return future;
    }
    
    // -------------------------- Utility Methods --------------------------
    
    /**
     * Sanitize a key for Firebase (remove invalid characters)
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