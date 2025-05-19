package com.example.elsa_speak_clone.classes;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.speech.RecognitionListener;

import com.airbnb.lottie.LottieAnimationView;
import com.example.elsa_speak_clone.activities.QuizActivity;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceRecognizer {
    private static final String TAG = "VoiceRecognizer";
    private final TextView tvPrompt;
    private final TextView tvWord;
    private final Button btnSpeak, btnRandomWord;
    private final SpeechRecognizer speechRecognizer;
    private final LottieAnimationView lottieConfetti;
    private String randomWord;
    private final Handler errorHandler = new Handler(Looper.getMainLooper());
    private boolean isProcessingError = false; // Prevent multiple triggers
    // Add these new fields
    private int currentLessonId = 1; // Default lesson ID
    private ArrayList<String> usedWords = new ArrayList<>();
    private ArrayList<String> availableWords = new ArrayList<>();
    private final AppDatabase database;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ExecutorService executor;
    // Add flag to track if current word is pronounced correctly
    private boolean currentWordPronounced = false;

    // Update constructor to NOT load vocabulary immediately
    public VoiceRecognizer(TextView tvPrompt, TextView tvWord, Button btnSpeak,
                       Button btnRandomWord, SpeechRecognizer speechRecognizer,
                       LottieAnimationView lottieConfetti, AppDatabase database) {
        this.tvPrompt = tvPrompt;
        this.tvWord = tvWord;
        this.btnSpeak = btnSpeak;
        this.btnRandomWord = btnRandomWord;
        this.speechRecognizer = speechRecognizer;
        this.lottieConfetti = lottieConfetti;
        this.database = database;
        this.executor = Executors.newSingleThreadExecutor();
        
        // Don't load vocabulary here - it will be loaded separately
    }

    // Make this method public and modify to use background thread
    public void loadVocabularyForLesson() {
        loadVocabularyForLesson(currentLessonId);
    }
    
    // Modified method to use background thread
    public void loadVocabularyForLesson(int lessonId) {
        // Update the current lesson ID
        this.currentLessonId = lessonId;
        
        // Clear available words before loading new ones
        availableWords.clear();
        
        try {
            // Use the correct method that already returns List<String>
            List<String> vocabularyList = database.vocabularyDao().getWordsByLessonId(lessonId);
            
            // Add all words to available words list
            availableWords.addAll(vocabularyList);
            
            // Update UI on main thread
            mainHandler.post(() -> {
                // Optionally add UI feedback if no words available
                if (availableWords.isEmpty()) {
                    tvPrompt.setText("No vocabulary words available for this lesson");
                    btnRandomWord.setEnabled(false);
                } else {
                    Log.d(TAG, "Loaded " + availableWords.size() + " words for lesson " + lessonId);
                    // Generate first random word
                    generateAndDisplayNewWord();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading vocabulary: " + e.getMessage(), e);
            mainHandler.post(() -> {
                tvPrompt.setText("Error loading vocabulary. Please try again.");
                btnRandomWord.setEnabled(false);
            });
        }
    }

    // Update the setCurrentLessonId method to not load vocabulary immediately
    public void setCurrentLessonId(int lessonId) {
        if (this.currentLessonId != lessonId) {
            this.currentLessonId = lessonId;
            // Reset used words when changing lessons
            usedWords.clear();
            // Don't load vocabulary here - it will be loaded separately
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                tvPrompt.setText("Processing...");
            }

            @Override
            public void onError(int error) {
                // Delay error output until user stops speaking
                if (!isProcessingError) {
                    isProcessingError = true;
                    errorHandler.postDelayed(() -> {
                        // Remove setText when deploy
                        tvPrompt.setText("Error: " + getErrorMessage(error));
                        isProcessingError = false; // Reset flag after delay
                    }, 5000); // 5-second delay before showing error
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && evaluateRecognition(matches.get(0))) {
                    showConfetti();
                    // Enable the random word button after correct pronunciation
                    enableRandomWordButton();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    private boolean evaluateRecognition(String recognizedText) {
        if (recognizedText != null) {
            recognizedText = recognizedText.toLowerCase(Locale.US);
            if (recognizedText.equals(randomWord.toLowerCase(Local.US))) {
                tvPrompt.setText("Correct! You said: " + recognizedText);
                currentWordPronounced = true;
                return true;
            } else {
                tvPrompt.setText("Incorrect! You said: " + recognizedText);
            }
        } else {
            tvPrompt.setText("No word recognized");
        }
        return false;
    }

    private boolean allWordsGenerated() {
        return usedWords.size() >= availableWords.size();
    }

    // Update the generateRandomWord method
    private String generateRandomWord() {
        // If all words have been used
        if (usedWords.size() >= availableWords.size()) {
            // Trigger activity switch
            navigateToQuiz();
            // Return the last word or an empty string
            return randomWord != null ? randomWord : "";
        }

        // Create a list of words that haven't been used yet
        ArrayList<String> unusedWords = new ArrayList<>(availableWords);
        unusedWords.removeAll(usedWords);

        // Select a random word from the unused words
        int randomIndex = (int) (Math.random() * unusedWords.size());
        randomWord = unusedWords.get(randomIndex);

        // Add the word to the used words list
        usedWords.add(randomWord);
        
        // Reset the pronunciation flag for the new word
        currentWordPronounced = false;
        
        return randomWord;
    }
    
    // New method to generate and display a new word
    private void generateAndDisplayNewWord() {
        String word = generateRandomWord();
        tvWord.setText("Say this word: " + word);
        // Disable random word button until this word is pronounced correctly
        disableRandomWordButton();
    }
    
    // Method to disable the random word button
    private void disableRandomWordButton() {
        btnRandomWord.setEnabled(false);
        btnRandomWord.setAlpha(0.5f); // Visual feedback that button is disabled
    }
    
    // Method to enable the random word button
    private void enableRandomWordButton() {
        btnRandomWord.setEnabled(true);
        btnRandomWord.setAlpha(1.0f); // Restore normal appearance
    }

    private void navigateToQuiz() {
        Context context = tvPrompt.getContext();
        Intent intent = new Intent(context, QuizActivity.class);
        // You can pass data about the completed lesson
        intent.putExtra("LESSON_ID", currentLessonId);
        context.startActivity(intent);
    }

    public void setupRandomWordButton() {
        // Initially disable the button until first word is pronounced correctly
        disableRandomWordButton();
        
        btnRandomWord.setOnClickListener(v -> {
            // Only proceed if the current word has been pronounced correctly
            if (currentWordPronounced) {
                generateAndDisplayNewWord();
            } else {
                // Provide feedback that user needs to pronounce current word first
                tvPrompt.setText("Please pronounce the current word correctly first");
            }
        });
    }

    private void showConfetti() {
        lottieConfetti.setAnimation(R.raw.confetti);
        lottieConfetti.setVisibility(View.VISIBLE);
        lottieConfetti.playAnimation();
        lottieConfetti.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                lottieConfetti.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
    }

    private String getErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT: return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No speech input";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Speech recognizer is busy";
            case SpeechRecognizer.ERROR_SERVER: return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "Speech timeout";
            default: return "Unknown error";
        }
    }

    public void release() {
        speechRecognizer.destroy();
    }
}
