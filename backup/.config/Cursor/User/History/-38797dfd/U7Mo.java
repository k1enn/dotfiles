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

    /**
     * Completely restore user data from Firebase
     */
    public CompletableFuture<Boolean> fullUserDataRestore(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        firebaseDbHelper.getUserByEmail(email)
            .thenAccept(firebaseUser -> {
                if (firebaseUser == null) {
                    Log.w(TAG, "No user found in Firebase with email: " + email);
                    future.complete(false);
                    return;
                }
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        // 1. First, save the user record
                        database.userDao().insertUser(firebaseUser);
                        
                        // 2. Get user progress from Firebase
                        int userId = firebaseUser.getUserId();
                        
                        firebaseDbHelper.getUserStats(userId)
                            .thenAccept(stats -> {
                                if (stats == null) {
                                    Log.d(TAG, "No progress stats found for user " + userId);
                                    future.complete(true); // User restored but no progress
                                    return;
                                }
                                
                                try {
                                    // Check if progress already exists
                                    UserProgress existingProgress = database.userProgressDao().getUserProgressById(userId);
                                    
                                    if (existingProgress == null) {
                                        // Create new progress record
                                        UserProgress progress = new UserProgress();
                                        progress.setProgressId(getNextProgressId());
                                        progress.setUserId(userId);
                                        progress.setXp(stats.getXp());
                                        progress.setStreak(stats.getStreak());
                                        progress.setLastStudyDate(stats.getLastStudyDate());
                                        
                                        database.userProgressDao().insertUserProgress(progress);
                                        Log.d(TAG, "Restored progress for user " + userId);
                                    } else {
                                        // Update existing progress
                                        existingProgress.setXp(stats.getXp());
                                        existingProgress.setStreak(stats.getStreak());
                                        existingProgress.setLastStudyDate(stats.getLastStudyDate());
                                        database.userProgressDao().updateUserProgress(existingProgress);
                                    }
                                    
                                    Log.d(TAG, "User " + userId + " fully restored with progress data");
                                    future.complete(true);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error restoring user progress: " + e.getMessage(), e);
                                    // User was still restored, even if progress wasn't
                                    future.complete(true);
                                }
                            })
                            .exceptionally(e -> {
                                Log.e(TAG, "Error getting user stats: " + e.getMessage(), e);
                                // User was still restored, even if progress wasn't
                                future.complete(true);
                                return null;
                            });
                    } catch (Exception e) {
                        Log.e(TAG, "Error restoring user: " + e.getMessage(), e);
                        future.complete(false);
                    }
                });
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error fetching user from Firebase: " + e.getMessage(), e);
                future.complete(false);
                return null;
            });
        
        return future;
    }

    /**
     * Get next available progress ID
     */
    private int getNextProgressId() {
        try {
            Integer maxId = database.userProgressDao().getMaxProgressId();
            return (maxId != null ? maxId : 0) + 1;
        } catch (Exception e) {
            Log.e(TAG, "Error getting next progress ID: " + e.getMessage(), e);
            return 1; // Default to 1 if error
        }
    }

    /**
     * Restore user data from Firebase after app reinstall
     * 
     * @param email User's email address
     * @return CompletableFuture with the restored User object or null if not found
     */
    public CompletableFuture<User> restoreUserByEmail(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        firebaseDbHelper.getUserByEmail(email)
            .thenAccept(user -> {
                if (user == null) {
                    Log.d(TAG, "No user found in Firebase with email: " + email);
                    future.complete(null);
                    return;
                }
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        // Check if user already exists locally
                        User existingUser = database.userDao().getUserByEmail(email);
                        
                        if (existingUser == null) {
                            // Insert the restored user
                            long userId = database.userDao().insertUser(user);
                            Log.d(TAG, "Restored user from Firebase: " + userId);
                            
                            // Also restore user progress if available
                            restoreUserProgress(user.getUserId())
                                .thenAccept(result -> {
                                    Log.d(TAG, "User progress restoration " + 
                                             (result ? "successful" : "failed or not found"));
                                    future.complete(user);
                                });
                        } else {
                            Log.d(TAG, "User already exists locally, skipping restore");
                            future.complete(existingUser);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error restoring user: " + e.getMessage(), e);
                        future.complete(null);
                    }
                });
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error fetching user from Firebase: " + e.getMessage(), e);
                future.complete(null);
                return null;
            });
        
        return future;
    }

    /**
     * Restore user progress from Firebase
     * 
     * @param userId User ID to restore progress for
     * @return CompletableFuture with success status
     */
    private CompletableFuture<Boolean> restoreUserProgress(int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        firebaseDbHelper.getUserStats(userId)
            .thenAccept(stats -> {
                if (stats == null) {
                    Log.d(TAG, "No progress stats found for user " + userId);
                    future.complete(false);
                    return;
                }
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        // Check if progress already exists
                        UserProgress existingProgress = database.userProgressDao().getUserProgressById(userId);
                        
                        if (existingProgress == null) {
                            // Create new progress record
                            UserProgress progress = new UserProgress();
                            progress.setUserId(userId);
                            progress.setXp(stats.getXp());
                            progress.setStreak(stats.getStreak());
                            database.userProgressDao().insertUserProgress(progress);
                            Log.d(TAG, "Restored progress for user " + userId);
                            future.complete(true);
                        } else {
                            // Update existing progress if Firebase has newer data
                            if (stats.getXp() > existingProgress.getXp()) {
                                existingProgress.setXp(stats.getXp());
                                existingProgress.setStreak(stats.getStreak());
                                database.userProgressDao().updateUserProgress(existingProgress);
                                Log.d(TAG, "Updated progress for user " + userId);
                            }
                            future.complete(true);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error restoring user progress: " + e.getMessage(), e);
                        future.complete(false);
                    }
                });
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error fetching user stats from Firebase: " + e.getMessage(), e);
                future.complete(false);
                return null;
            });
        
        return future;
    }

    /**
     * Sync only user progress (XP and streak) to Firebase
     * Used after completing quizzes or updating streak
     * 
     * @param userId The user ID to sync progress for
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> syncUserProgress(int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Fetch user progress from Room database
                UserProgress progress = database.userProgressDao().getUserProgressById(userId);
                
                if (progress == null) {
                    Log.w(TAG, "No progress found for user " + userId);
                    future.complete(false);
                    return;
                }
                
                // Update user stats in Firebase
                firebaseDbHelper.updateUserStats(userId, progress.getXp(), progress.getStreak())
                    .thenAccept(result -> {
                        Log.d(TAG, "User progress sync " + (result ? "successful" : "failed") + 
                                " for user " + userId);
                        future.complete(result);
                    })
                    .exceptionally(e -> {
                        Log.e(TAG, "Error syncing user progress: " + e.getMessage(), e);
                        future.complete(false);
                        return null;
                    });
            } catch (Exception e) {
                Log.e(TAG, "Error in syncUserProgress: " + e.getMessage(), e);
                future.complete(false);
            }
        });
        
        return future;
    }
} 