package com.example.elsa_speak_clone.activities;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.classes.VoiceRecognizer;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.VocabularyDao;
import com.example.elsa_speak_clone.services.NavigationService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechToText extends AppCompatActivity {

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
    private Button btnSpeak;
    private Button btnRandomWord;
    private LottieAnimationView lottieConfetti;

    // Speech recognition components
    private SpeechRecognizer speechRecognizer;
    private VoiceRecognizer voiceRecognizer;
    private boolean isRecognitionAvailable;
    
    // Congratulation popup components
    private PopupWindow congratsPopup;
    private MediaPlayer correctSoundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Try to initialize database before anything else
        tryInitializeDatabase(0);
        
        // Wait for database initialization before proceeding
        if (databaseInitialized.get()) {
            completeInitialization();
        } else {
            Toast.makeText(this, "Failed to initialize database. Please restart the app.", 
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void completeInitialization() {
        setContentView(R.layout.activity_speech_to_text);
        
        // Initialize services
        navigationService = new NavigationService(this);
        
        // Initialize UI components
        initializeUI();
        
        // Set up window insets for edge-to-edge display
        setupWindowInsets();
        
        // Request microphone permission
        requestMicrophonePermission();
        
        // Initialize speech recognizer
        initializeSpeechRecognizer();
        
        // Set up voice recognizer with database
        setupVoiceRecognizer();
        
        // Initialize sound player for correct answers
        correctSoundPlayer = MediaPlayer.create(this, R.raw.correct_sound);
    }

    private void initializeUI() {
        tvPrompt = findViewById(R.id.tvPrompt);
        tvWord = findViewById(R.id.tvWord);
        tvProgress = findViewById(R.id.tvProgress);
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
            voiceRecognizer = new VoiceRecognizer(tvPrompt, tvWord, btnSpeak, btnRandomWord,
                    speechRecognizer, lottieConfetti, database) {
                @Override
                protected void onWordCorrectlyPronounced() {
                    // Play correct sound
                    playCorrectSound();
                    
                    // Show congratulation popup
                    showCongratulationPopup();
                    
                    // Update progress text
                    updateProgressText();
                }
            };
            
            // Verify VoiceRecognizer was created successfully
            if (voiceRecognizer == null) {
                Log.e(TAG, "Failed to create VoiceRecognizer");
                Toast.makeText(this, "Error initializing speech recognition", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
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
                        Toast.makeText(SpeechToText.this, 
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
        if (voiceRecognizer != null) {
            int used = voiceRecognizer.getUsedWordsCount();
            int total = voiceRecognizer.getTotalWordsCount();
            tvProgress.setText("Words practiced: " + used + "/" + total);
        }
    }
    
    private void playCorrectSound() {
        try {
            if (correctSoundPlayer != null) {
                correctSoundPlayer.seekTo(0);
                correctSoundPlayer.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing sound", e);
        }
    }
    
    private void showCongratulationPopup() {
        try {
            // Inflate the popup layout
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_congratulation, null);

            // Create the popup window
            congratsPopup = new PopupWindow(
                    popupView,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    true
            );
            
            // Set background
            congratsPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            
            // Set animation
            congratsPopup.setAnimationStyle(android.R.style.Animation_Dialog);
            
            // Set up the Lottie animation
            LottieAnimationView confettiView = popupView.findViewById(R.id.lottieConfetti);
            confettiView.setAnimation(R.raw.confetti);
            confettiView.playAnimation();
            
            // Set up the continue button
            Button btnContinue = popupView.findViewById(R.id.btnContinue);
            btnContinue.setOnClickListener(v -> {
                if (congratsPopup != null && congratsPopup.isShowing()) {
                    congratsPopup.dismiss();
                }
            });
            
            // Set up animation listener to dismiss popup when animation ends
            confettiView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animator) { }
                @Override public void onAnimationEnd(Animator animator) {
                    if (congratsPopup != null && congratsPopup.isShowing()) {
                        congratsPopup.dismiss();
                    }
                }
                @Override public void onAnimationCancel(Animator animator) { }
                @Override public void onAnimationRepeat(Animator animator) { }
            });
            
            // Show the popup window
            congratsPopup.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
            
            // Dim the background
            View container = congratsPopup.getContentView().getRootView();
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) container.getLayoutParams();
            params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.dimAmount = 0.3f;
            getWindowManager().updateViewLayout(container, params);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing congratulation popup", e);
        }
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
        
        // Release sound player
        if (correctSoundPlayer != null) {
            correctSoundPlayer.release();
            correctSoundPlayer = null;
        }
        
        // Shutdown executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}