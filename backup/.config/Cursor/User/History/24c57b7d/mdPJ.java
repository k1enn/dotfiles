package com.example.elsa_speak_clone.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.content.SharedPreferences;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.adapters.VoiceRecognizer;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.VocabularyDao;
import com.example.elsa_speak_clone.services.NavigationService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PronunciationFragment extends Fragment implements VoiceRecognizer.RecognitionCallback {

    private static final String TAG = "PronunciationFragment";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final int MAX_DB_INIT_RETRIES = 3;

    private AppDatabase database;
    private VocabularyDao vocabularyDao;
    private ExecutorService executor;
    private Handler mainHandler;
    private final AtomicBoolean databaseInitialized = new AtomicBoolean(false);

    // UI elements
    private TextView tvPrompt;
    private TextView tvWord;
    private TextView tvProgress;
    private TextView tvLessonTitle;
    private TextView tvPhonetic;
    private TextView tvPronunciationScore;
    private TextView tvHint;
    private TextView tvStreak;
    private View pronunciationFeedbackView;
    private Button btnSpeak;
    private Button btnRandomWord;
    private Button btnListen;
    private Button btnShowHint;
    private ProgressBar pbDailyGoal;
    private TextView tvDailyGoal;
    private LottieAnimationView lottieConfetti;
    
    // Speech recognizer
    private SpeechRecognizer speechRecognizer;
    private VoiceRecognizer voiceRecognizer;
    
    // State variables
    private int lessonId;
    private boolean permissionToRecordAccepted = false;
    private int currentStreak = 0;
    private SharedPreferences prefs;
    private int wordsToday = 0;
    private int dailyGoal = 10;
    private int lastScore = 0;
    private TextToSpeech textToSpeech;

    // Required empty constructor
    public PronunciationFragment() {}

    // Factory method to create a new instance with arguments
    public static PronunciationFragment newInstance(int lessonId) {
        PronunciationFragment fragment = new PronunciationFragment();
        Bundle args = new Bundle();
        args.putInt("lesson_id", lessonId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lessonId = getArguments().getInt("lesson_id", 1);
        }
        
        // Initialize services and handlers that don't require views
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize preferences
        prefs = requireActivity().getSharedPreferences("pronunciation_prefs", Context.MODE_PRIVATE);
        currentStreak = prefs.getInt("current_streak", 0);
        
        // Get today's practice count
        String today = new java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new java.util.Date());
        wordsToday = prefs.getInt("words_practiced_" + today, 0);
        
        // Request permissions
        requestPermissions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pronunciation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize UI elements
        initializeUI(view);
        
        // Initialize database and voice recognizer
        initializeDatabase();
        
        // Update lesson title
        updateLessonTitle(lessonId);
        
        // Set up button listeners
        setupButtonListeners();
        
        // Initialize text-to-speech
        initializeTextToSpeech();
        
        // Update UI displays
        updateStreakDisplay();
        updateDailyGoalUI();
    }

    private void initializeUI(View view) {
        // Find all UI elements
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        
        tvPrompt = view.findViewById(R.id.tvPrompt);
        tvWord = view.findViewById(R.id.tvWord);
        tvProgress = view.findViewById(R.id.tvProgress);
        tvLessonTitle = view.findViewById(R.id.tvLessonTitle);
        tvPhonetic = view.findViewById(R.id.tvPhonetic);
        tvPronunciationScore = view.findViewById(R.id.tvPronunciationScore);
        tvHint = view.findViewById(R.id.tvHint);
        tvStreak = view.findViewById(R.id.tvStreak);
        pronunciationFeedbackView = view.findViewById(R.id.pronunciationFeedbackView);
        btnSpeak = view.findViewById(R.id.btnSpeak);
        btnRandomWord = view.findViewById(R.id.btnRandomWord);
        btnListen = view.findViewById(R.id.btnListen);
        btnShowHint = view.findViewById(R.id.btnShowHint);
        pbDailyGoal = view.findViewById(R.id.pbDailyGoal);
        tvDailyGoal = view.findViewById(R.id.tvDailyGoal);
        lottieConfetti = view.findViewById(R.id.lottieConfetti);
        
        // Initial UI setup
        pronunciationFeedbackView.setAlpha(0f);
        btnSpeak.setEnabled(false);
        btnRandomWord.setEnabled(false);
        tvPronunciationScore.setText("");
    }

    private void initializeDatabase() {
        executor.execute(() -> {
            try {
                int retryCount = 0;
                
                while (!databaseInitialized.get() && retryCount < MAX_DB_INIT_RETRIES) {
                    try {
                        database = AppDatabase.getInstance(requireContext());
                        vocabularyDao = database.vocabularyDao();
                        databaseInitialized.set(true);
                        
                        // Initialize voice recognizer once database is available
                        mainHandler.post(() -> {
                            initializeVoiceRecognizer();
                            btnRandomWord.setEnabled(true);
                            
                            // Show a random word when ready
                            btnRandomWord.performClick();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to initialize database: " + e.getMessage());
                        retryCount++;
                        
                        try {
                            Thread.sleep(500); // Wait before retry
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                
                if (!databaseInitialized.get()) {
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "Failed to initialize database", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing database: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void initializeVoiceRecognizer() {
        if (permissionToRecordAccepted) {
            // Initialize voice recognizer and speech recognizer
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
            voiceRecognizer = new VoiceRecognizer(requireContext(), speechRecognizer, lessonId, this);
            
            // Update progress text
            updateProgressText();
        } else {
            requestPermissions();
        }
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(requireContext(), status -> {
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

    private void setupButtonListeners() {
        // Set up speak button
        btnSpeak.setOnClickListener(v -> {
            if (permissionToRecordAccepted) {
                if (voiceRecognizer != null) {
                    tvPrompt.setText("Listening...");
                    btnSpeak.setEnabled(false);
                    voiceRecognizer.startListening();
                } else {
                    Toast.makeText(requireContext(), "Voice recognizer not initialized", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestPermissions();
            }
        });
        
        // Set up random word button
        btnRandomWord.setOnClickListener(v -> {
            if (voiceRecognizer != null) {
                String word = voiceRecognizer.getRandomWord();
                setCurrentWord(word);
                btnSpeak.setEnabled(true);
                tvPrompt.setText("Tap 'Speak Now' and pronounce the word");
            } else {
                Toast.makeText(requireContext(), "Voice recognizer not initialized", Toast.LENGTH_SHORT).show();
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
    
    private void setCurrentWord(String word) {
        tvWord.setText(word);
        updatePhoneticDisplay(word);
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
    
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            permissionToRecordAccepted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            
            if (permissionToRecordAccepted) {
                initializeVoiceRecognizer();
            } else {
                Toast.makeText(requireContext(), "Permission to record audio is required", Toast.LENGTH_SHORT).show();
                tvPrompt.setText("Permission to record audio is required");
            }
        }
    }

    @Override
    public void onSpeechRecognized(String spokenText, String targetWord, boolean isCorrect) {
        int score = calculateSimilarityScore(spokenText, targetWord);
        
        mainHandler.post(() -> {
            btnSpeak.setEnabled(true);
            
            if (isCorrect || score > 70) {
                tvPrompt.setText("Correct! You pronounced it well.");
                incrementStreak();
                incrementDailyCounter();
                showPronunciationFeedback(score);
            } else {
                tvPrompt.setText("Not quite right. Try again.");
                resetStreak();
                showPronunciationFeedback(score);
            }
            
            updateProgressText();
        });
    }

    @Override
    public void onSpeechError(int errorCode) {
        mainHandler.post(() -> {
            tvPrompt.setText("Didn't catch that. Please try again.");
            btnSpeak.setEnabled(true);
        });
    }
    
    private void showPronunciationFeedback(int score) {
        // Score is from 0-100
        lastScore = score;
        
        // Update the score text
        tvPronunciationScore.setText(score + "%");
        
        // Determine color based on score
        int color;
        if (score >= 80) {
            color = Color.parseColor("#4CAF50"); // Green
            tvPrompt.setText("Excellent pronunciation!");
        } else if (score >= 60) {
            color = Color.parseColor("#FFC107"); // Yellow/amber
            tvPrompt.setText("Good pronunciation. Keep practicing!");
        } else {
            color = Color.parseColor("#F44336"); // Red
            tvPrompt.setText("Try again to improve your pronunciation");
        }
        
        // Animate the feedback view
        pronunciationFeedbackView.setBackgroundColor(color);
        pronunciationFeedbackView.setAlpha(0.7f);
        
        // Create animation
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(pronunciationFeedbackView, "alpha", 0f, 0.7f);
        fadeIn.setDuration(300);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(pronunciationFeedbackView, "alpha", 0.7f, 0f);
        fadeOut.setDuration(500);
        fadeOut.setStartDelay(1000);
        
        animatorSet.playSequentially(fadeIn, fadeOut);
        animatorSet.start();
    }
    
    private int calculateSimilarityScore(String spoken, String target) {
        // This is a simple implementation - you'd want a more sophisticated algorithm
        spoken = spoken.toLowerCase().trim();
        target = target.toLowerCase().trim();
        
        // If exact match
        if (spoken.equals(target)) {
            return 100;
        }
        
        // Calculate Levenshtein distance
        int distance = levenshteinDistance(spoken, target);
        int maxLength = Math.max(spoken.length(), target.length());
        
        // Convert to a score (0-100)
        int score = (int) (100 * (1 - ((double) distance / maxLength)));
        
        // Ensure minimum score is 0
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
    
    private void updateStreakDisplay() {
        tvStreak.setText("Streak: " + currentStreak);
    }
    
    private void incrementStreak() {
        currentStreak++;
        prefs.edit().putInt("current_streak", currentStreak).apply();
        updateStreakDisplay();
        
        // Show a celebration animation for milestone streaks
        if (currentStreak % 5 == 0) {
            Toast.makeText(requireContext(), "Amazing! " + currentStreak + " word streak!", Toast.LENGTH_SHORT).show();
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
    
    private void updateDailyGoalUI() {
        pbDailyGoal.setMax(dailyGoal);
        pbDailyGoal.setProgress(wordsToday);
        tvDailyGoal.setText(wordsToday + "/" + dailyGoal + " words today");
        
        // Check if goal met
        if (wordsToday >= dailyGoal && wordsToday - 1 < dailyGoal) {
            // We just met the goal
            Toast.makeText(requireContext(), "Daily goal achieved! 🎉", Toast.LENGTH_LONG).show();
            if (lottieConfetti != null) {
                lottieConfetti.setVisibility(View.VISIBLE);
                lottieConfetti.playAnimation();
            }
        }
    }
    
    private void incrementDailyCounter() {
        String today = new java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new java.util.Date());
        wordsToday++;
        prefs.edit().putInt("words_practiced_" + today, wordsToday).apply();
        updateDailyGoalUI();
    }

    private void updateProgressText() {
        if (voiceRecognizer == null) {
            tvProgress.setText("Loading words...");
            mainHandler.post(this::updateProgressText);
            return;
        }
        
        int used = voiceRecognizer.getUsedWordsCount();
        int total = voiceRecognizer.getTotalWordsCount();
        tvProgress.setText("Words count: " + used + "/" + total);
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
                        tvLessonTitle.setText(String.valueOf(lessonId));
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching lesson title", e);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Release speech recognizer
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        
        // Release voice recognizer
        if (voiceRecognizer != null) {
            voiceRecognizer.release();
        }
        
        // Shutdown text-to-speech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Shutdown executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
} 