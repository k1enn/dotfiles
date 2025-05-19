package com.example.elsa_speak_clone.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private Button btnListen;
    Toolbar toolbar;
    private LottieAnimationView lottieConfetti;
    private TextToSpeech textToSpeech;
    private TextView tvPronunciationScore;
    private View pronunciationFeedbackView;
    private int lastScore = 0;
    private TextView tvPhonetic;
    private int currentStreak = 0;
    private TextView tvStreak;
    private SharedPreferences prefs;
    private TextView tvHint;
    private Button btnShowHint;
    private int wordsToday = 0;
    private int dailyGoal = 10;
    private ProgressBar pbDailyGoal;
    private TextView tvDailyGoal;

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

        prefs = getSharedPreferences("pronunciation_prefs", MODE_PRIVATE);
        currentStreak = prefs.getInt("current_streak", 0);
        tvStreak = findViewById(R.id.tvStreak);
        updateStreakDisplay();

        // Get today's practice count
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        wordsToday = prefs.getInt("words_practiced_" + today, 0);
        updateDailyGoalUI();

        pbDailyGoal = findViewById(R.id.pbDailyGoal);
        tvDailyGoal = findViewById(R.id.tvDailyGoal);
    }

    private void initialize() {
        setContentView(R.layout.activity_speech_to_text);
        
        navigationService = new NavigationService(this);
        initializeUI();
        setupWindowInsets();
        requestMicrophonePermission();
        initializeSpeechRecognizer();
        setupVoiceRecognizer();
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });
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
        btnListen = findViewById(R.id.btnListen);
        lottieConfetti = findViewById(R.id.lottieConfetti);
        tvPronunciationScore = findViewById(R.id.tvPronunciationScore);
        pronunciationFeedbackView = findViewById(R.id.pronunciationFeedbackView);
        tvPhonetic = findViewById(R.id.tvPhonetic);
        tvStreak = findViewById(R.id.tvStreak);
        tvHint = findViewById(R.id.tvHint);
        btnShowHint = findViewById(R.id.btnShowHint);

        // Set up speak button
        btnSpeak.setOnClickListener(v -> {
            if (voiceRecognizer != null) {
                voiceRecognizer.startListening();
                tvPrompt.setText("Listening...");
            }
        });

        // Set up listen button
        btnListen.setOnClickListener(v -> {
            String currentWord = tvWord.getText().toString();
            if (!currentWord.isEmpty()) {
                speakWord(currentWord);
            }
        });

        // Set up hint button
        btnShowHint.setOnClickListener(v -> showHint());
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
    
    private void showPronunciationFeedback(int score) {
        lastScore = score;
        
        tvPronunciationScore.setText(score + "%");
        
        int color;
        if (score >= 80) {
            color = Color.parseColor("#4CAF50");
            tvPrompt.setText("Excellent pronunciation!");
        } else if (score >= 60) {
            color = Color.parseColor("#FFC107");
            tvPrompt.setText("Good pronunciation. Keep practicing!");
        } else {
            color = Color.parseColor("#F44336");
            tvPrompt.setText("Try again to improve your pronunciation");
        }
        
        pronunciationFeedbackView.setBackgroundColor(color);
        pronunciationFeedbackView.setAlpha(0.7f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(pronunciationFeedbackView, "alpha", 0f, 0.7f);
        fadeIn.setDuration(300);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(pronunciationFeedbackView, "alpha", 0.7f, 0f);
        fadeOut.setDuration(500);
        fadeOut.setStartDelay(1000);
        
        animatorSet.playSequentially(fadeIn, fadeOut);
        animatorSet.start();
    }

    voiceRecognizer.setRecognitionCallback(new VoiceRecognizer.RecognitionCallback() {
        @Override
        public void onSpeechRecognized(String spokenText, String targetWord, boolean isCorrect) {
            int score = calculateSimilarityScore(spokenText, targetWord);
            
            mainHandler.post(() -> {
                lottieConfetti.pauseAnimation();
                lottieConfetti.setVisibility(View.INVISIBLE);
                
                tvWord.setText(targetWord);
                updateProgressText();
                
                if (isCorrect || score > 70) {
                    incrementStreak();
                    incrementDailyCounter();
                    showPronunciationFeedback(score);
                    lottieConfetti.setVisibility(View.VISIBLE);
                    lottieConfetti.playAnimation();
                } else {
                    resetStreak();
                    showPronunciationFeedback(score);
                }
            });
        }
        
        @Override
        public void onError(String errorMessage) {
            mainHandler.post(() -> {
                tvPrompt.setText("Didn't catch that. Please try again.");
                btnSpeak.setEnabled(true);
            });
        }
    });

    private int calculateSimilarityScore(String spoken, String target) {
        spoken = spoken.toLowerCase().trim();
        target = target.toLowerCase().trim();
        
        if (spoken.equals(target)) {
            return 100;
        }
        
        int distance = levenshteinDistance(spoken, target);
        int maxLength = Math.max(spoken.length(), target.length());
        
        int score = (int) (100 * (1 - ((double) distance / maxLength)));
        
        return Math.max(0, score);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    private void speakWord(String word) {
        if (textToSpeech != null) {
            // Add a slight pause for better pronunciation
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_id");
        }
    }

    private void updatePhoneticDisplay(String word) {
        // In a real app, you'd use a dictionary API or database for this
        // This is a simplified example using a small hardcoded dictionary
        Map<String, String> phoneticMap = new HashMap<>();
        phoneticMap.put("hello", "həˈloʊ");
        phoneticMap.put("world", "wɜːrld");
        phoneticMap.put("apple", "ˈæp.əl");
        phoneticMap.put("banana", "bəˈnæn.ə");
        // Add more words as needed
        
        String phonetic = phoneticMap.getOrDefault(word.toLowerCase(), "");
        tvPhonetic.setText(phonetic);
        
        // If phonetic is available, show the view
        tvPhonetic.setVisibility(phonetic.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setCurrentWord(String word) {
        tvWord.setText(word);
        updatePhoneticDisplay(word);
    }

    private void updateStreakDisplay() {
        tvStreak.setText("Streak: " + currentStreak);
    }

    private void incrementStreak() {
        currentStreak++;
        prefs.edit().putInt("current_streak", currentStreak).apply();
        updateStreakDisplay();
        
        // Show a celebration animation for milestone streaks
        if (currentStreak % 5 == 0) {
            Toast.makeText(this, "Amazing! " + currentStreak + " word streak!", Toast.LENGTH_SHORT).show();
            // Perhaps trigger a more elaborate animation here
        }
    }

    private void resetStreak() {
        if (currentStreak > 0) {
            // Save the highest streak
            int highestStreak = prefs.getInt("highest_streak", 0);
            if (currentStreak > highestStreak) {
                prefs.edit().putInt("highest_streak", currentStreak).apply();
            }
            
            currentStreak = 0;
            prefs.edit().putInt("current_streak", 0).apply();
            updateStreakDisplay();
        }
    }

    private void showHint() {
        String currentWord = tvWord.getText().toString();
        if (currentWord.isEmpty()) return;
        
        // In a real app, you'd get real pronunciation tips from a database
        String hint = "Try breaking it down: ";
        for (int i = 0; i < currentWord.length(); i++) {
            if (i > 0) hint += " + ";
            hint += currentWord.charAt(i);
        }
        
        tvHint.setText(hint);
        tvHint.setVisibility(View.VISIBLE);
        
        // Hide hint after a delay
        mainHandler.postDelayed(() -> {
            tvHint.setVisibility(View.GONE);
        }, 10000); // 10 seconds
    }

    private void updateDailyGoalUI() {
        pbDailyGoal.setMax(dailyGoal);
        pbDailyGoal.setProgress(wordsToday);
        tvDailyGoal.setText(wordsToday + "/" + dailyGoal + " words today");
        
        // Check if goal met
        if (wordsToday >= dailyGoal && wordsToday - 1 < dailyGoal) {
            // We just met the goal
            Toast.makeText(this, "Daily goal achieved! 🎉", Toast.LENGTH_LONG).show();
            lottieConfetti.setVisibility(View.VISIBLE);
            lottieConfetti.playAnimation();
        }
    }

    private void incrementDailyCounter() {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        wordsToday++;
        prefs.edit().putInt("words_practiced_" + today, wordsToday).apply();
        updateDailyGoalUI();
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

        // Shutdown text-to-speech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}