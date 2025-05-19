package com.example.elsa_speak_clone.database.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.elsa_speak_clone.database.entities.Lesson;
import com.example.elsa_speak_clone.database.entities.User;
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
    
    // Firebase database paths
    private static final String USERS_PATH = "users";
    private static final String LESSONS_PATH = "lessons";
    private static final String VOCABULARY_PATH = "vocabulary";
    
    // Database instance
    private final FirebaseDatabase database;
    private final DatabaseReference rootRef;
    
    // Singleton instance
    private static volatile FirebaseDbHelper instance;
    
    private FirebaseDbHelper() {
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference();
        
        // Note: setPersistenceEnabled is now called in ElsaSpeakApp class
    }
    
    public static synchronized FirebaseDbHelper getInstance() {
        if (instance == null) {
            synchronized (FirebaseDbHelper.class) {
                if (instance == null) {
                    instance = new FirebaseDbHelper();
                }
            }
        }
        return instance;
    }
    
    /**
     * Set up offline persistence for frequently accessed data
     */
    public void setupOfflinePersistence() {
        try {
            rootRef.child(USERS_PATH).keepSynced(true);
            rootRef.child(LESSONS_PATH).keepSynced(true);
            rootRef.child(VOCABULARY_PATH).keepSynced(true);
            Log.d(TAG, "Firebase offline persistence configured");
        } catch (Exception e) {
            Log.e(TAG, "Error configuring offline persistence", e);
        }
    }
    
    /**
     * Save or update user data to Firebase
     * 
     * @param user The user to save/update
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> saveUser(User user) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            // Create a map of the user data with all fields from User entity
            Map<String, Object> userValues = new HashMap<>();
            userValues.put("userId", user.getUserId());
            userValues.put("gmail", user.getGmail());
            userValues.put("name", user.getName());
            userValues.put("password", user.getPassword());  // Consider if you want to store passwords in Firebase
            userValues.put("google", user.getGoogle());
            userValues.put("joinDate", user.getJoinDate());
            
            // Save to Firebase at users/{userId}
            rootRef.child(USERS_PATH).child(String.valueOf(user.getUserId()))
                   .updateChildren(userValues)
                   .addOnSuccessListener(aVoid -> {
                       Log.d(TAG, "User saved successfully: " + user.getUserId());
                       future.complete(true);
                   })
                   .addOnFailureListener(e -> {
                       Log.e(TAG, "Error saving user: " + e.getMessage(), e);
                       future.complete(false);
                   });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing user data: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Get XP for a user from the UserProgress table
     * This is a placeholder - implement the actual logic
     */
    private int getUserXp(int userId) {
        // TODO: Implement actual XP retrieval from UserProgress table
        return 0;
    }
    
    /**
     * Get streak for a user from the UserProgress table
     * This is a placeholder - implement the actual logic
     */
    private int getUserStreak(int userId) {
        // TODO: Implement actual streak retrieval from UserProgress table
        return 0;
    }
    
    /**
     * Update user XP and streak in Firebase
     * 
     * @param userId User ID
     * @param xp Current XP
     * @param streak Current streak
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> updateUserStats(int userId, int xp, int streak) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("xp", xp);
            updates.put("streak", streak);
            
            rootRef.child(USERS_PATH).child(String.valueOf(userId))
                   .updateChildren(updates)
                   .addOnSuccessListener(aVoid -> {
                       Log.d(TAG, "User stats updated: userId=" + userId + ", xp=" + xp + ", streak=" + streak);
                       future.complete(true);
                   })
                   .addOnFailureListener(e -> {
                       Log.e(TAG, "Error updating user stats: " + e.getMessage(), e);
                       future.complete(false);
                   });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing user stats update: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Save a lesson to Firebase
     * 
     * @param lesson The lesson to save
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> saveLesson(Lesson lesson) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> lessonValues = new HashMap<>();
            lessonValues.put("lessonId", lesson.getLessonId());
            lessonValues.put("topic", lesson.getTopic());
            lessonValues.put("lessonContent", lesson.getLessonContent());
            lessonValues.put("difficultyLevel", lesson.getDifficultyLevel());
            
            rootRef.child(LESSONS_PATH).child(String.valueOf(lesson.getLessonId()))
                   .updateChildren(lessonValues)
                   .addOnSuccessListener(aVoid -> {
                       Log.d(TAG, "Lesson saved successfully: " + lesson.getLessonId());
                       future.complete(true);
                   })
                   .addOnFailureListener(e -> {
                       Log.e(TAG, "Error saving lesson: " + e.getMessage(), e);
                       future.complete(false);
                   });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing lesson data: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Save multiple lessons to Firebase
     * 
     * @param lessons List of lessons to save
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> saveLessons(List<Lesson> lessons) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> updates = new HashMap<>();
            
            for (Lesson lesson : lessons) {
                Map<String, Object> lessonValues = new HashMap<>();
                lessonValues.put("lessonId", lesson.getLessonId());
                lessonValues.put("topic", lesson.getTopic());
                lessonValues.put("lessonContent", lesson.getLessonContent());
                lessonValues.put("difficultyLevel", lesson.getDifficultyLevel());
                
                updates.put(String.valueOf(lesson.getLessonId()), lessonValues);
            }
            
            rootRef.child(LESSONS_PATH)
                   .updateChildren(updates)
                   .addOnSuccessListener(aVoid -> {
                       Log.d(TAG, "Saved " + lessons.size() + " lessons successfully");
                       future.complete(true);
                   })
                   .addOnFailureListener(e -> {
                       Log.e(TAG, "Error saving lessons: " + e.getMessage(), e);
                       future.complete(false);
                   });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing lessons data: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Save vocabulary items to Firebase
     * 
     * @param vocabularyList The list of vocabulary items to save
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> saveVocabulary(List<Vocabulary> vocabularyList) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> updates = new HashMap<>();
            
            for (Vocabulary vocabulary : vocabularyList) {
                String key = String.valueOf(vocabulary.getWordId());
                Map<String, Object> vocabValues = new HashMap<>();
                vocabValues.put("vocabularyId", vocabulary.getWordId());
                vocabValues.put("word", vocabulary.getWord());
                vocabValues.put("pronunciation", vocabulary.getPronunciation());
                vocabValues.put("lessonId", vocabulary.getLessonId());
                
                updates.put(key, vocabValues);
            }
            
            // Batch update to Firebase
            rootRef.child(VOCABULARY_PATH).updateChildren(updates)
                   .addOnSuccessListener(aVoid -> {
                       Log.d(TAG, "Saved " + vocabularyList.size() + " vocabulary items to Firebase");
                       future.complete(true);
                   })
                   .addOnFailureListener(e -> {
                       Log.e(TAG, "Error saving vocabulary to Firebase: " + e.getMessage(), e);
                       future.complete(false);
                   });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing vocabulary data for Firebase: " + e.getMessage(), e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Fetch all users from Firebase
     * 
     * @return CompletableFuture with list of users
     */
    public CompletableFuture<List<User>> getAllUsers() {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        
        rootRef.child(USERS_PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();
                
                try {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        try {
                            int userId = userSnapshot.child("userId").getValue(Integer.class);
                            String gmail = userSnapshot.child("gmail").getValue(String.class);
                            String name = userSnapshot.child("name").getValue(String.class);
                            String password = userSnapshot.child("password").getValue(String.class);
                            Boolean google = userSnapshot.child("google").getValue(Boolean.class);
                            String joinDate = userSnapshot.child("joinDate").getValue(String.class);
                            
                            // Use the complete constructor
                            User user = new User(userId, gmail, name, password, 
                                               google != null ? google : false, 
                                               joinDate != null ? joinDate : "");
                            users.add(user);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user data: " + e.getMessage(), e);
                        }
                    }
                    
                    Log.d(TAG, "Fetched " + users.size() + " users from Firebase");
                    future.complete(users);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing users data: " + e.getMessage(), e);
                    future.complete(new ArrayList<>());
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching users: " + databaseError.getMessage());
                future.complete(new ArrayList<>());
            }
        });
        
        return future;
    }
    
    /**
     * Fetch all lessons from Firebase
     * 
     * @return CompletableFuture with list of lessons
     */
    public CompletableFuture<List<Lesson>> getAllLessons() {
        CompletableFuture<List<Lesson>> future = new CompletableFuture<>();
        
        rootRef.child(LESSONS_PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Lesson> lessons = new ArrayList<>();
                
                try {
                    for (DataSnapshot lessonSnapshot : dataSnapshot.getChildren()) {
                        try {
                            int lessonId = lessonSnapshot.child("lessonId").getValue(Integer.class);
                            String topic = lessonSnapshot.child("topic").getValue(String.class);
                            String content = lessonSnapshot.child("lessonContent").getValue(String.class);
                            int difficultyLevel = lessonSnapshot.child("difficultyLevel").getValue(Integer.class);
                            
                            Lesson lesson = new Lesson(lessonId, topic, content, difficultyLevel);
                            lessons.add(lesson);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing lesson data: " + e.getMessage(), e);
                        }
                    }
                    
                    Log.d(TAG, "Fetched " + lessons.size() + " lessons from Firebase");
                    future.complete(lessons);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing lessons data: " + e.getMessage(), e);
                    future.complete(new ArrayList<>());
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching lessons: " + databaseError.getMessage());
                future.complete(new ArrayList<>());
            }
        });
        
        return future;
    }
    
    /**
     * Fetch vocabulary for a specific lesson from Firebase
     * 
     * @param lessonId The lesson ID
     * @return CompletableFuture with list of vocabulary
     */
    public CompletableFuture<List<Vocabulary>> getVocabularyByLesson(int lessonId) {
        CompletableFuture<List<Vocabulary>> future = new CompletableFuture<>();
        
        rootRef.child(VOCABULARY_PATH)
               .orderByChild("lessonId")
               .equalTo(lessonId)
               .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Vocabulary> vocabularyList = new ArrayList<>();
                
                try {
                    for (DataSnapshot vocabSnapshot : dataSnapshot.getChildren()) {
                        try {
                            int vocabularyId = vocabSnapshot.child("vocabularyId").getValue(Integer.class);
                            String word = vocabSnapshot.child("word").getValue(String.class);
                            String pronunciation = vocabSnapshot.child("pronunciation").getValue(String.class);
                            int lessonIdValue = vocabSnapshot.child("lessonId").getValue(Integer.class);
                            
                            Vocabulary vocabulary = new Vocabulary(vocabularyId, word, pronunciation, lessonIdValue);
                            vocabularyList.add(vocabulary);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing vocabulary data: " + e.getMessage(), e);
                        }
                    }
                    
                    Log.d(TAG, "Fetched " + vocabularyList.size() + " vocabulary items for lesson " + lessonId);
                    future.complete(vocabularyList);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing vocabulary data: " + e.getMessage(), e);
                    future.complete(new ArrayList<>());
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching vocabulary: " + databaseError.getMessage());
                future.complete(new ArrayList<>());
            }
        });
        
        return future;
    }
    
    /**
     * Fetch a specific vocabulary item by ID from Firebase
     * 
     * @param vocabularyId The vocabulary ID
     * @return CompletableFuture with the vocabulary item
     */
    public CompletableFuture<Vocabulary> getVocabularyById(int vocabularyId) {
        CompletableFuture<Vocabulary> future = new CompletableFuture<>();
        
        rootRef.child(VOCABULARY_PATH).child(String.valueOf(vocabularyId))
               .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (!dataSnapshot.exists()) {
                        Log.d(TAG, "Vocabulary with ID " + vocabularyId + " not found in Firebase");
                        future.complete(null);
                        return;
                    }
                    
                    int id = dataSnapshot.child("vocabularyId").getValue(Integer.class);
                    String word = dataSnapshot.child("word").getValue(String.class);
                    String pronunciation = dataSnapshot.child("pronunciation").getValue(String.class);
                    int lessonId = dataSnapshot.child("lessonId").getValue(Integer.class);
                    
                    Vocabulary vocabulary = new Vocabulary(id, word, pronunciation, lessonId);
                    
                    Log.d(TAG, "Successfully fetched vocabulary with ID " + vocabularyId);
                    future.complete(vocabulary);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing vocabulary data: " + e.getMessage(), e);
                    future.complete(null);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching vocabulary: " + databaseError.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }
    
    /**
     * Fetch a user by ID from Firebase
     * 
     * @param userId The user ID
     * @return CompletableFuture with the user
     */
    public CompletableFuture<User> getUserById(int userId) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        rootRef.child(USERS_PATH).child(String.valueOf(userId))
               .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (!dataSnapshot.exists()) {
                        Log.d(TAG, "User with ID " + userId + " not found in Firebase");
                        future.complete(null);
                        return;
                    }
                    
                    int id = dataSnapshot.child("userId").getValue(Integer.class);
                    String gmail = dataSnapshot.child("gmail").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String password = dataSnapshot.child("password").getValue(String.class);
                    Boolean google = dataSnapshot.child("google").getValue(Boolean.class);
                    String joinDate = dataSnapshot.child("joinDate").getValue(String.class);
                    
                    User user = new User(id, gmail, name, password, 
                                       google != null ? google : false, 
                                       joinDate != null ? joinDate : "");
                    
                    Log.d(TAG, "Successfully fetched user with ID " + userId);
                    future.complete(user);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing user data: " + e.getMessage(), e);
                    future.complete(null);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching user: " + databaseError.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }
} 