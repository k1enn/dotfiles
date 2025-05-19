package com.example.elsa_speak_clone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.DataInitializer;
import com.example.elsa_speak_clone.database.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int MIN_SPLASH_TIME = 1500; // Minimum time to show splash (milliseconds)
    
    private TextView tvLoadingMessage;
    private SessionManager sessionManager;
    private long startTime;
    private boolean dataLoaded = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
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
        
        // Start loading data in background
        loadData();
    }
    
    private void loadData() {
        executor.execute(() -> {
            try {
                // Step 1: Initialize the database
                updateLoadingMessage("Initializing database...");
                AppDatabase database = AppDatabase.getInstance(getApplicationContext());
                
                // Step 2: Preload necessary data
                updateLoadingMessage("Loading lessons and quizzes...");
                
                // You might want to preload some data here if needed:
                // Example: database.lessonDao().getAllLessons();
                
                // Mark loading as complete
                dataLoaded = true;
                
                // Calculate remaining time to show splash screen
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingTime = Math.max(0, MIN_SPLASH_TIME - elapsedTime);
                
                // Wait for minimum splash time if needed
                if (remainingTime > 0) {
                    Thread.sleep(remainingTime);
                }
                
                // Proceed to next screen on UI thread
                runOnUiThread(this::proceedToNextScreen);
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading data: " + e.getMessage(), e);
                // Even if there's an error, we should still navigate forward after a delay
                new Handler().postDelayed(this::proceedToNextScreen, 2000);
            }
        });
    }
    
    private void updateLoadingMessage(String message) {
        runOnUiThread(() -> tvLoadingMessage.setText(message));
    }
    
    private void proceedToNextScreen() {
        // Check if user is logged in
        if (sessionManager.isLoggedIn()) {
            // User is logged in, go to MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            // User is not logged in, go to LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        
        // Apply sliding transition if desired
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        
        // Close this activity
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                // Wait a reasonable time for executing tasks to complete
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
} 