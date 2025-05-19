package com.example.elsa_speak_clone.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
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
import com.example.elsa_speak_clone.database.dao.VocabularyDao;
import com.example.elsa_speak_clone.database.entities.UserProgress;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class VoiceRecognizer {
    private static final String TAG = "VoiceRecognizer";
    private final TextView tvPrompt;
    private final TextView tvWord;
    private final Context context;
    private MediaPlayer correctSoundPlayer;
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
    private RecognitionCallback callback;
    private int lessonId;
    private List<String> wordList;
    private Random random;
    private String currentWord;

    // Add this interface at the top of the class
    public interface ProgressUpdateListener {
        void onProgressUpdated();
    }
    
    private ProgressUpdateListener progressUpdateListener;
    
    // Set the listener to update progress
    public void setProgressUpdateListener(ProgressUpdateListener listener) {
        this.progressUpdateListener = listener;
    }

    // Add this interface inside the VoiceRecognizer class
    public interface RecognitionCallback {
        void onSpeechRecognized(String spokenText, String targetWord, boolean isCorrect);
        void onRecognitionError(int errorCode);
        void onReadyForSpeech();
        void onEndOfSpeech();
    }

    // Update constructor to include all required parameters
    public VoiceRecognizer(Context context, SpeechRecognizer speechRecognizer, 
                           int lessonId, RecognitionCallback callback) {
        this.context = context;
        this.speechRecognizer = speechRecognizer;
        this.lessonId = lessonId;
        this.callback = callback;
        this.random = new Random();
        this.usedWords = new HashSet<>();
        
        // Initialize the word list
        initializeWordList();
    }

    // Add this helper method
    private void initializeWordList() {
        // Fetch words for the lesson from database
        AppDatabase db = AppDatabase.getInstance(context);
        wordList = db.vocabularyDao().getWordsByLessonId(lessonId);
        
        if (wordList == null || wordList.isEmpty()) {
            // Fallback to sample words if none found in database
            wordList = Arrays.asList("hello", "world", "practice", "speak", "language");
        }
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

    public void setCurrentLessonId(int lessonId) {
        if (this.currentLessonId != lessonId) {
            this.currentLessonId = lessonId;
            // Reset used words when changing lessons
            usedWords.clear();
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
        if (speechRecognizer != null) {
            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            
            try {
                speechRecognizer.startListening(recognizerIntent);
            } catch (Exception e) {
                Log.e("VoiceRecognizer", "Error starting speech recognition", e);
                if (callback != null) {
                    callback.onRecognitionError(SpeechRecognizer.ERROR_CLIENT);
                }
            }
        }
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    public void release() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
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
    private boolean evaluateRecognition(String recognizedText) {
        if (recognizedText != null) {
            recognizedText = recognizedText.toLowerCase(Locale.US);
            if (recognizedText.equals(randomWord.toLowerCase(Locale.US))) {
                tvPrompt.setText("Correct! You said: " + recognizedText);
                currentWordPronounced = true;
                
                // Play correct sound
                playCorrectSound();
                
                // Notify listener that progress has updated
                if (progressUpdateListener != null) {
                    progressUpdateListener.onProgressUpdated();
                }
                
                return true;
            } else {
                tvPrompt.setText("Incorrect! You said: " + recognizedText);
            }
        } else {
            tvPrompt.setText("No word recognized");
        }
        return false;
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

        // Get pronunciation using the existing DAO method
        executor.execute(() -> {
            String pronunciation = database.vocabularyDao().getWordPronunciation(word);

            // Update UI on the main thread
            mainHandler.post(() -> {
                // Display word with pronunciation
                tvWord.setText("Say this word: \n " + word + "\n" +
                        "[" + pronunciation + "]");

                // Disable random word button until this word is pronounced correctly
                disableRandomWordButton();

                // Notify listener that progress has updated
                if (progressUpdateListener != null) {
                    progressUpdateListener.onProgressUpdated();
                }
            });
        });
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

    public int getUsedWordsCount() {
        return usedWords.size();
    }

    public int getTotalWordsCount() {
        return availableWords.size();
    }

    /**
     * Updates progress when a word is pronounced correctly
     * @param userId The user ID
     */
    private void updatePronunciationProgress(int userId) {
        executor.execute(() -> {
            try {
                // Award XP points for each correctly pronounced word
                // You could adjust the points based on difficulty
                int pointsPerWord = 10;
                
                // Add XP to the user's progress
                UserProgress progress = database.userProgressDao().getUserLessonProgress(userId, currentLessonId);
                
                if (progress != null) {
                    // Update existing progress
                    progress.setXp(progress.getXp() + pointsPerWord);
                    progress.setLastStudyDate(getCurrentDate());
                    database.userProgressDao().update(progress);
                } else {
                    // Create new progress entry if none exists
                    int progressId = getNextProgressId(database, userId);
                    UserProgress newProgress = new UserProgress(
                        progressId,
                        userId,
                        currentLessonId,
                        0, // Default difficulty level
                        null, // No completion time yet
                        1, // Start with streak of 1
                        pointsPerWord, // Initial XP points
                        getCurrentDate() // Current date
                    );
                    database.userProgressDao().insert(newProgress);
                }
                
                Log.d(TAG, "Updated pronunciation progress for user " + userId + 
                       " in lesson " + currentLessonId);
                   
                // Check if all words have been pronounced correctly
                if (usedWords.size() >= availableWords.size()) {
                    // Mark the pronunciation exercise as completed if all words done
                    markPronunciationCompleted(userId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating pronunciation progress: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Marks the pronunciation exercise as completed
     */
    private void markPronunciationCompleted(int userId) {
        executor.execute(() -> {
            try {
                UserProgress progress = database.userProgressDao().getUserLessonProgress(userId, currentLessonId);
                
                if (progress != null && progress.getCompletionTime() == null) {
                    // Set completion time
                    progress.setCompletionTime(getCurrentDate() + " " + getCurrentTime());
                    
                    // Add completion bonus
                    progress.setXp(progress.getXp() + 50); // Bonus for completing all words
                    
                    // Update the progress
                    database.userProgressDao().update(progress);
                    Log.d(TAG, "Marked pronunciation exercise as completed for lesson " + currentLessonId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error marking pronunciation as completed: " + e.getMessage(), e);
            }
        });
    }

    // Helper method to get the current date
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Helper method to get the current time
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Helper method to get the next available progress ID
    private int getNextProgressId(AppDatabase database, int userId) {
        List<UserProgress> existingProgress = database.userProgressDao().getUserProgress(userId);
        int maxId = 0;
        for (UserProgress progress : existingProgress) {
            if (progress.getProgressId() > maxId) {
                maxId = progress.getProgressId();
            }
        }
        return maxId + 1;
    }

    private void onRecognizeSuccess() {
        if (!currentWordPronounced) {
            currentWordPronounced = true;
            
            // Play success sound
            playCorrectSound();
            
            // Show success animation
            lottieConfetti.setVisibility(View.VISIBLE);
            lottieConfetti.playAnimation();
            
            // Update the prompt
            tvPrompt.setText("Correct! Great pronunciation.");
            
            // Enable random word button for next word
            enableRandomWordButton();
            
            // Update progress in the database
            updatePronunciationProgress(getCurrentUserId());
            
            // Notify listener that progress has updated
            if (progressUpdateListener != null) {
                progressUpdateListener.onProgressUpdated();
            }
        }
    }

    private int getCurrentUserId() {
        SharedPreferences sharedPreferences = 
            context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("USER_ID", 1); // Default to 1 if not found
    }

    // Add this method to get a random word
    public String getRandomWord() {
        if (wordList == null || wordList.isEmpty()) {
            return "hello"; // Default fallback word
        }
        
        // If all words have been used, reset the used words set
        if (usedWords.size() >= wordList.size()) {
            usedWords.clear();
        }
        
        // Get a random word that hasn't been used yet
        List<String> availableWords = new ArrayList<>(wordList);
        availableWords.removeAll(usedWords);
        
        if (availableWords.isEmpty()) {
            // All words used, reset and use any word
            usedWords.clear();
            currentWord = wordList.get(random.nextInt(wordList.size()));
        } else {
            // Use a new word
            currentWord = availableWords.get(random.nextInt(availableWords.size()));
        }
        
        // Add to used words and return
        usedWords.add(currentWord);
        return currentWord;
    }

    // Methods to get used and total word counts
    public int getUsedWordsCount() {
        return usedWords.size();
    }

    public int getTotalWordsCount() {
        return wordList != null ? wordList.size() : 0;
    }
}
