package com.example.elsa_speak_clone.database.firebase;

import android.content.Context;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.Lesson;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.entities.Vocabulary;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FirebaseDataSynchronizer {
    private static final String TAG = "FirebaseDataSync";
    
    private final Context context;
    private final AppDatabase database;
    private final FirebaseDbHelper firebaseDbHelper;
    
    public FirebaseDataSynchronizer(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
        this.firebaseDbHelper = FirebaseDbHelper.getInstance();
    }
    
    /**
     * Synchronize the active user data with Firebase
     * 
     * @param userId The user ID to synchronize
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> syncUserData(int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Fetch user from Room database
                User user = database.userDao().getUserById(userId);
                
                if (user == null) {
                    Log.e(TAG, "User not found in local database: " + userId);
                    future.complete(false);
                    return;
                }
                
                // Fetch user progress stats
                UserProgress progress = database.userProgressDao().getUserProgressById(userId);
                int xp = progress != null ? progress.getXp() : 0;
                int streak = progress != null ? progress.getStreak() : 0;
                
                // Update user in Firebase
                firebaseDbHelper.saveUser(user)
                    .thenCompose(saved -> {
                        if (saved) {
                            return firebaseDbHelper.updateUserStats(userId, xp, streak);
                        } else {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
                    .thenAccept(result -> {
                        Log.d(TAG, "User data sync " + (result ? "successful" : "failed") + 
                                    " for user " + userId);
                        future.complete(result);
                    })
                    .exceptionally(e -> {
                        Log.e(TAG, "Error syncing user data: " + e.getMessage(), e);
                        future.complete(false);
                        return null;
                    });
            } catch (Exception e) {
                Log.e(TAG, "Error in syncUserData: " + e.getMessage(), e);
                future.complete(false);
            }
        });
        
        return future;
    }
    
    /**
     * Synchronize all lessons with Firebase
     * 
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> syncAllLessons() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Fetch all lessons from Room database
                List<Lesson> lessons = database.lessonDao().getAllLessons();
                
                if (lessons.isEmpty()) {
                    Log.d(TAG, "No lessons found in local database");
                    future.complete(true);
                    return;
                }
                
                // Save lessons to Firebase
                firebaseDbHelper.saveLessons(lessons)
                    .thenAccept(result -> {
                        Log.d(TAG, "Lessons sync " + (result ? "successful" : "failed") + 
                                    " for " + lessons.size() + " lessons");
                        future.complete(result);
                    })
                    .exceptionally(e -> {
                        Log.e(TAG, "Error syncing lessons: " + e.getMessage(), e);
                        future.complete(false);
                        return null;
                    });
            } catch (Exception e) {
                Log.e(TAG, "Error in syncAllLessons: " + e.getMessage(), e);
                future.complete(false);
            }
        });
        
        return future;
    }
    
    /**
     * Synchronize vocabulary for a specific lesson with Firebase
     * 
     * @param lessonId The lesson ID to synchronize vocabulary for
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> syncLessonVocabulary(int lessonId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Fetch vocabulary for the lesson from Room database
                List<Vocabulary> vocabularyList = database.vocabularyDao().getVocabularyByLessonId(lessonId);
                
                if (vocabularyList.isEmpty()) {
                    Log.d(TAG, "No vocabulary found for lesson " + lessonId);
                    future.complete(true);
                    return;
                }
                
                // Save vocabulary to Firebase
                firebaseDbHelper.saveVocabularyList(vocabularyList)
                    .thenAccept(result -> {
                        Log.d(TAG, "Vocabulary sync " + (result ? "successful" : "failed") + 
                                    " for " + vocabularyList.size() + " words in lesson " + lessonId);
                        future.complete(result);
                    })
                    .exceptionally(e -> {
                        Log.e(TAG, "Error syncing vocabulary" );
                        future.complete(false);
                        return null;
                    });
            } catch (Exception e) {
                Log.e(TAG, "Error in syncLessonVocabulary: " + e.getMessage(), e);
                future.complete(false);
            }
        });
        
        return future;
    }
    
    /**
     * Import lessons from Firebase to local database
     * 
     * @return CompletableFuture with count of lessons imported
     */
    public CompletableFuture<Integer> importLessonsFromFirebase() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        firebaseDbHelper.getAllLessons()
            .thenAccept(lessons -> {
                if (lessons.isEmpty()) {
                    Log.d(TAG, "No lessons found in Firebase to import");
                    future.complete(0);
                    return;
                }
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        int importCount = 0;
                        for (Lesson lesson : lessons) {
                            try {
                                // Check if lesson already exists
                                Lesson existingLesson = database.lessonDao().getLessonById(lesson.getLessonId());
                                
                                if (existingLesson == null) {
                                    // Insert new lesson
                                    database.lessonDao().insertLesson(lesson);
                                    importCount++;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error importing lesson " + lesson.getLessonId(), e);
                            }
                        }
                        
                        Log.d(TAG, "Imported " + importCount + " lessons from Firebase");
                        future.complete(importCount);
                    } catch (Exception e) {
                        Log.e(TAG, "Error importing lessons from Firebase: " + e.getMessage(), e);
                        future.complete(0);
                    }
                });
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error fetching lessons from Firebase: " + e.getMessage(), e);
                future.complete(0);
                return null;
            });
        
        return future;
    }
    
    /**
     * Import vocabulary for a lesson from Firebase to local database
     * 
     * @param lessonId The lesson ID to import vocabulary for
     * @return CompletableFuture with count of vocabulary items imported
     */
    public CompletableFuture<Integer> importLessonVocabularyFromFirebase(int lessonId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        firebaseDbHelper.getVocabularyByLesson(lessonId)
            .thenAccept(vocabularyList -> {
                if (vocabularyList.isEmpty()) {
                    Log.d(TAG, "No vocabulary found in Firebase for lesson " + lessonId);
                    future.complete(0);
                    return;
                }
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        int importCount = 0;
                        for (Vocabulary vocabulary : vocabularyList) {
                            try {
                                // Check if vocabulary already exists
                                Vocabulary existingVocab = database.vocabularyDao()
                                    .getVocabularyById(vocabulary.getWordId());
                                
                                if (existingVocab == null) {
                                    // Insert new vocabulary
                                    database.vocabularyDao().insertVocabulary(vocabulary);
                                    importCount++;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error importing vocabulary " + vocabulary.getWordId(), e);
                            }
                        }
                        
                        Log.d(TAG, "Imported " + importCount + " vocabulary items for lesson " + lessonId);
                        future.complete(importCount);
                    } catch (Exception e) {
                        Log.e(TAG, "Error importing vocabulary from Firebase: " + e.getMessage(), e);
                        future.complete(0);
                    }
                });
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error fetching vocabulary from Firebase: " + e.getMessage(), e);
                future.complete(0);
                return null;
            });
        
        return future;
    }
    
    /**
     * Import users from Firebase to local database
     * 
     * @return CompletableFuture with count of users imported
     */
    public CompletableFuture<Integer> importUsersFromFirebase() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        firebaseDbHelper.getAllUsers()
            .thenAccept(userList -> {
                if (userList.isEmpty()) {
                    Log.d(TAG, "No users found in Firebase");
                    future.complete(0);
                    return;
                }
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        int importCount = 0;
                        for (User user : userList) {
                            try {
                                // Check if user already exists
                                User existingUser = database.userDao().getUserById(user.getUserId());
                                
                                if (existingUser == null) {
                                    // Insert new user
                                    database.userDao().insertUser(user);
                                    importCount++;
                                } else {
                                    // Optional: Update existing user if needed
                                    // database.userDao().updateUser(user);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error importing user " + user.getUserId(), e);
                            }
                        }
                        
                        Log.d(TAG, "Imported " + importCount + " users from Firebase");
                        future.complete(importCount);
                    } catch (Exception e) {
                        Log.e(TAG, "Error importing users from Firebase: " + e.getMessage(), e);
                        future.complete(0);
                    }
                });
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error fetching users from Firebase: " + e.getMessage(), e);
                future.complete(0);
                return null;
            });
        
        return future;
    }
    
    /**
     * Synchronize all users with Firebase
     * 
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> syncAllUsers() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Fetch all users from Room database
                List<User> users = database.userDao().getAllUsers();
                
                if (users.isEmpty()) {
                    Log.d(TAG, "No users found in local database");
                    future.complete(true);
                    return;
                }
                
                // Sync each user individually to handle progress data correctly
                int successCount = 0;
                for (User user : users) {
                    try {
                        // For each user, sync both profile and progress
                        boolean success = syncUserData(user.getUserId()).get();
                        if (success) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing user " + user.getUserId(), e);
                    }
                }
                
                Log.d(TAG, "Synchronized " + successCount + " of " + users.size() + " users to Firebase");
                future.complete(successCount == users.size());
            } catch (Exception e) {
                Log.e(TAG, "Error in syncAllUsers: " + e.getMessage(), e);
                future.complete(false);
            }
        });
        
        return future;
    }
} 