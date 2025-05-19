package com.example.elsa_speak_clone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.DataInitializer;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.repositories.UserRepository;
import com.example.elsa_speak_clone.utilities.ConfigManager;
import com.example.elsa_speak_clone.database.firebase.FirebaseDbHelper;
import com.example.elsa_speak_clone.database.firebase.FirebaseDataSynchronizer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int MIN_SPLASH_TIME = 1500; // Minimum time to show splash screen
    private static final int MAX_WAIT_TIME = 15000; // Maximum time to wait for initialization
    
    private TextView tvLoadingMessage;
    private SessionManager sessionManager;
    private UserRepository userRepository;
    private long startTime;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean initializationComplete = new AtomicBoolean(false);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Initialize UI elements
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        
        // Record start time
        startTime = System.currentTimeMillis();
        

        // Initialize context for DataInitializer
        DataInitializer.initialize(getApplicationContext());
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Initialize repositories and services
        userRepository = new UserRepository(getApplication());

        // Start loading data in background
        loadData();
        
        // Set a maximum timeout for initialization
        mainHandler.postDelayed(() -> {
            if (!initializationComplete.get()) {
                Log.w(TAG, "Initialization taking too long, proceeding anyway");
                proceedToNextScreen();
            }
        }, MAX_WAIT_TIME);
    }
    
    private void loadData() {
        executor.execute(() -> {
            try {
                // Initialize the database
                updateLoadingMessage("Initializing database...");
                AppDatabase database = AppDatabase.getInstance(getApplicationContext());
                
                if (database == null) {
                    Log.e(TAG, "Failed to initialize database");
                    mainHandler.post(this::proceedToNextScreen);
                    return;
                }

                // Preload necessary data
                updateLoadingMessage("Loading lessons and quizzes...");
                database.runInTransaction(() -> {
                    try {
                        // Force access to DAOs to ensure they're initialized
                        database.lessonDao().getAllLessons();
                        database.quizDao().getAllQuizzes();
                        database.vocabularyDao().getWordsByLessonId(1);
                        Log.d(TAG, "Successfully preloaded initial data");
                    } catch (Exception e) {
                        Log.e(TAG, "Error preloading data", e);
                    }
                });
                
                // Initialize ConfigManager
                updateLoadingMessage("Loading configuration...");
                try {
                    ConfigManager.initialize(getApplicationContext());
                    Log.d(TAG, "ConfigManager initialized successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing ConfigManager", e);
                }
                

                // Calculate elapsed time and ensure minimum display time
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingTime = Math.max(0, MIN_SPLASH_TIME - elapsedTime);
                
                // Wait for minimum splash time if needed
                if (remainingTime > 0) {
                    Thread.sleep(remainingTime);
                }

                // Initialize Firebase
                updateLoadingMessage("Connecting to cloud...");
                try {
                    FirebaseDbHelper firebaseHelper = FirebaseDbHelper.getInstance();
                    firebaseHelper.setupOfflinePersistence();
                    
                    // Example of syncing data (you could make this optional or only run when online)
                    FirebaseDataSynchronizer synchronizer = new FirebaseDataSynchronizer(getApplicationContext());
                    
                    // Sync lessons with Firebase - only if we have internet
                    if (isNetworkAvailable()) {
                        // You can decide whether to import from or export to Firebase
                        // Here we'll do a two-way sync for lessons:
                        
                        // First, import any new lessons from Firebase
                        synchronizer.importLessonsFromFirebase()
                            .thenCompose(count -> {
                                Log.d(TAG, "Imported " + count + " new lessons from Firebase");
                                // Then export all lessons to ensure Firebase has everything
                                return synchronizer.syncAllLessons();
                            })
                            .exceptionally(e -> {
                                Log.e(TAG, "Error during data synchronization: " + e.getMessage(), e);
                                return false;
                            });
                    }
                    
                    Log.d(TAG, "Firebase initialization completed");
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
                }

                // Mark initialization as complete and proceed
                initializationComplete.set(true);
                mainHandler.post(this::proceedToNextScreen);
                
            } catch (Exception e) {
                Log.e(TAG, "Error during initialization: " + e.getMessage(), e);
                mainHandler.post(this::proceedToNextScreen);
            }
        });
    }
    
    private void updateLoadingMessage(String message) {
        Log.d(TAG, "Status: " + message);
        mainHandler.post(() -> tvLoadingMessage.setText(message));
    }
    
    private void proceedToNextScreen() {
        if (isFinishing()) {
            return;
        }
        
        if (sessionManager.isLoggedIn()) {
            // User is logged in, go to MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            // User is not logged in, go to LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        // Apply transition
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
} 