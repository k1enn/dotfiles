package com.example.elsa_speak_clone.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.adapters.VoiceRecognizer;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.VocabularyDao;
import com.example.elsa_speak_clone.services.NavigationService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PronuncationActivity extends AppCompatActivity {

    private static final String TAG = "SpeechToTextActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final int MAX_DB_INIT_RETRIES = 3;

    private AppDatabase database;
    private VocabularyDao vocabularyDao;
    private ExecutorService executor;
    private Handler mainHandler;
    private NavigationService navigationService;
    private final AtomicBoolean databaseInitialized = new AtomicBoolean(false);

    // UI elements
    private TextView tvPrompt;
    private TextView tvWord;
    private TextView tvProgress;
    private TextView tvLessonTitle;
    private Button btnSpeak;
    private Button btnRandomWord;
    Toolbar toolbar;
    private LottieAnimationView lottieConfetti;

    // Speech recognition components
    private SpeechRecognizer speechRecognizer;
    private VoiceRecognizer voiceRecognizer;
    private boolean isRecognitionAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Try to initialize database before anything else
        tryInitializeDatabase(0);

        // Wait for database initialization before proceeding
        if (databaseInitialized.get()) {
            initialize();
            setupToolbar ();
        } else {
            Toast.makeText(this, "Failed to initialize database. Please restart the app.", 
                    Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void initialize() {
        setContentView(R.layout.activity_speech_to_text);
        
        navigationService = new NavigationService(this);
        initializeUI();
        setupWindowInsets();
        requestMicrophonePermission();
        initializeSpeechRecognizer();
        setupVoiceRecognizer();
    }
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("PronunciationActivity", "onOptionsItemSelected: " + item.getItemId());
        
        // Handle the back button (up button in action bar)
        if (item.getItemId() == android.R.id.home) {
            Log.d("PronunciationActivity", "Back button pressed");
            onBackPressed();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        Log.d("PronunciationActivity", "onBackPressed called");
        Log.d("PronunciationActivity", "isTaskRoot: " + isTaskRoot());
        super.onBackPressed();
    }
    private void initializeUI() {
        tvPrompt = findViewById(R.id.tvPrompt);
        tvWord = findViewById(R.id.tvWord);
        tvProgress = findViewById(R.id.tvProgress);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        btnRandomWord = findViewById(R.id.btnRandomWord);
        btnSpeak = findViewById(R.id.btnSpeak);
        lottieConfetti = findViewById(R.id.lottieConfetti);

        // Set up speak button
        btnSpeak.setOnClickListener(v -> {
            if (voiceRecognizer != null) {
                voiceRecognizer.startListening();
                tvPrompt.setText("Listening...");
            }
        });
    }

    private void setupWindowInsets() {
        View main = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            WindowInsetsCompat.Type.systemBars();
            return insets;
        });
    }
    
    private void tryInitializeDatabase(int retryCount) {
        try {
            Log.d(TAG, "Initializing database... (Attempt " + (retryCount + 1) + ")");
            
            // Get application context for database initialization
            if (getApplicationContext() == null) {
                Log.e(TAG, "Application context is null");
                return;
            }
            
            // Initialize database with application context
            database = AppDatabase.getInstance(getApplicationContext());
            
            if (database == null) {
                if (retryCount < MAX_DB_INIT_RETRIES) {
                    Log.w(TAG, "Database instance is null, retrying...");
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> tryInitializeDatabase(retryCount + 1), 200);
                } else {
                    Log.e(TAG, "Database initialization failed after " + MAX_DB_INIT_RETRIES + " attempts");
                }
                return;
            }
            
            // Initialize vocabulary DAO
            vocabularyDao = database.vocabularyDao();
            
            if (vocabularyDao == null) {
                Log.e(TAG, "VocabularyDao is null");
                return;
            }
            
            // Initialize threading components
            executor = Executors.newSingleThreadExecutor();
            mainHandler = new Handler(Looper.getMainLooper());
            
            // Mark database as successfully initialized
            databaseInitialized.set(true);
            Log.d(TAG, "Database initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database", e);
            if (retryCount < MAX_DB_INIT_RETRIES) {
                Log.w(TAG, "Retrying database initialization...");
                new Handler(Looper.getMainLooper()).postDelayed(
                        () -> tryInitializeDatabase(retryCount + 1), 200);
            }
        }
    }

    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        isRecognitionAvailable = SpeechRecognizer.isRecognitionAvailable(this);
        if (!isRecognitionAvailable) {
            Log.e(TAG, "Speech recognition is not available on this device.");
            Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_LONG).show();
        }
    }

    private void setupVoiceRecognizer() {
        try {
            if (!databaseInitialized.get() || database == null) {
                Log.e(TAG, "Cannot setup voice recognizer without database");
                Toast.makeText(this, "Error: Database not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // First create the VoiceRecognizer without loading data
            voiceRecognizer = new VoiceRecognizer(tvPrompt, tvWord, this, btnSpeak, btnRandomWord,
                    speechRecognizer, lottieConfetti, database) {
            };
            
            // Verify VoiceRecognizer was created successfully
            if (voiceRecognizer == null) {
                Log.e(TAG, "Failed to create VoiceRecognizer");
                Toast.makeText(this, "Error initializing speech recognition", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Set up progress update listener
            voiceRecognizer.setProgressUpdateListener(() -> updateProgressText());
            
            // Set up UI components that don't require database access
            voiceRecognizer.setupRandomWordButton();
            
            // Load vocabulary data in background thread
            executor.execute(() -> {
                try {
                    // Load vocabulary data in background
                    voiceRecognizer.loadVocabularyForLesson();
                    
                    // Update UI on main thread after data is loaded
                    mainHandler.post(() -> {
                        try {
                            // Start listening and set up remaining components on UI thread
                            voiceRecognizer.startListening();
                            
                            // Process any lesson ID from intent
                            Intent intent = getIntent();
                            if (intent.hasExtra("LESSON_ID")) {
                                int lessonId = intent.getIntExtra("LESSON_ID", 1);
                                voiceRecognizer.setCurrentLessonId(lessonId);
                                updateLessonTitle(lessonId);
                            } else {
                                // Default lesson ID is 1
                                updateLessonTitle(1);
                            }
                            
                            // Initialize progress text
                            updateProgressText();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in post-database setup", e);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading vocabulary data", e);
                    mainHandler.post(() -> {
                        Toast.makeText(PronuncationActivity.this,
                                "Error loading vocabulary data", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up voice recognizer", e);
            Toast.makeText(this, "Error setting up speech recognition", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void updateProgressText() {
        // Ensure we're on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(this::updateProgressText);
            return;
        }
        
        if (voiceRecognizer != null) {
            int used = voiceRecognizer.getUsedWordsCount();
            int total = voiceRecognizer.getTotalWordsCount();
            tvProgress.setText("Words count: " + used + "/" + total);
        }
    }
    
    private void updateLessonTitle(int lessonId) {
        executor.execute(() -> {
            try {
                // Fetch lesson title from database
                String lessonTitle = database.lessonDao().getLessonTitleById(lessonId);
                
                // Update UI on main thread
                mainHandler.post(() -> {
                    if (lessonTitle != null && !lessonTitle.isEmpty()) {
                        tvLessonTitle.setText(lessonTitle);
                    } else {
                        tvLessonTitle.setText(lessonId);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching lesson title", e);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Release speech recognizer
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        
        // Release voice recognizer
        if (voiceRecognizer != null) {
            voiceRecognizer.release();
        }
        
        // Shutdown executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}