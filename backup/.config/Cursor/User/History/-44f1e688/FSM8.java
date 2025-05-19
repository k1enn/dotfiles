package com.example.elsa_speak_clone.database.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.QuizDao;
import com.example.elsa_speak_clone.database.entities.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository class for handling Quiz-related database operations
 */
public class QuizRepository {
    private static final String TAG = "QuizRepository";
    private final QuizDao quizDao;
    private final AppDatabase database;
    
    // LiveData properties for observing quiz data
    private final MutableLiveData<List<Quiz>> allQuizzes = new MutableLiveData<>();
    private final MutableLiveData<List<Quiz>> lessonQuizzes = new MutableLiveData<>();
    private final MutableLiveData<Quiz> currentQuiz = new MutableLiveData<>();

    public QuizRepository(Context context) {
        database = AppDatabase.getInstance(context);
        quizDao = database.quizDao();
    }

    /**
     * Get LiveData for observing all quizzes
     */
    public LiveData<List<Quiz>> getAllQuizzes() {
        refreshAllQuizzes();
        return allQuizzes;
    }

    /**
     * Get LiveData for observing quizzes for a specific lesson
     */
    public LiveData<List<Quiz>> getQuizzesForLesson(int lessonId) {
        refreshLessonQuizzes(lessonId);
        return lessonQuizzes;
    }

    /**
     * Get LiveData for observing the current quiz
     */
    public LiveData<Quiz> getCurrentQuiz() {
        return currentQuiz;
    }

    /**
     * Refresh the LiveData with all quizzes
     */
    private void refreshAllQuizzes() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Quiz> quizzes = quizDao.getAllQuizzes();
            allQuizzes.postValue(quizzes);
        });
    }

    /**
     * Refresh the LiveData with quizzes for a specific lesson
     */
    private void refreshLessonQuizzes(int lessonId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Quiz> quizzes = quizDao.getQuizzesForLesson(lessonId);
            lessonQuizzes.postValue(quizzes);
        });
    }

    /**
     * Get a list of all quizzes (non-LiveData)
     */
    public List<Quiz> getAllQuizzesSync() {
        try {
            Future<List<Quiz>> future = AppDatabase.databaseWriteExecutor.submit(quizDao::getAllQuizzes);
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting all quizzes", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get a list of quizzes for a specific lesson (non-LiveData)
     */
    public List<Quiz> getQuizzesForLessonSync(int lessonId) {
        try {
            Future<List<Quiz>> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.getQuizzesForLesson(lessonId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting quizzes for lesson " + lessonId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get a specific quiz by ID
     */
    public Quiz getQuizById(int quizId) {
        try {
            Future<Quiz> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.getQuizById(quizId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting quiz " + quizId, e);
            return null;
        }
    }

    /**
     * Get a random quiz for a specific lesson
     * This is the method used by UserScoreRepository
     */
    public Quiz getRandomQuizForLesson(int lessonId) {
        try {
            Future<Quiz> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.getRandomQuizForLesson(lessonId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting random quiz for lesson " + lessonId, e);
            return null;
        }
    }

    /**
     * Get a random quiz for a specific lesson excluding specified quiz IDs
     */
    public Quiz getRandomQuizForLessonExcluding(int lessonId, List<Integer> excludedIds) {
        try {
            Future<Quiz> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.getRandomQuizForLessonExcluding(lessonId, excludedIds));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting random quiz for lesson " + lessonId + " excluding IDs", e);
            return null;
        }
    }

    /**
     * Insert a new quiz
     */
    public long insertQuiz(Quiz quiz) {
        try {
            Future<Long> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.insert(quiz));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error inserting quiz", e);
            return -1;
        }
    }

    /**
     * Update an existing quiz
     */
    public void updateQuiz(Quiz quiz) {
        AppDatabase.databaseWriteExecutor.execute(() -> quizDao.update(quiz));
    }

    /**
     * Delete a quiz
     */
    public void deleteQuiz(Quiz quiz) {
        AppDatabase.databaseWriteExecutor.execute(() -> quizDao.delete(quiz));
    }
    
    /**
     * Count quizzes for a lesson
     */
    public int countQuizzesForLesson(int lessonId) {
        try {
            Future<List<Quiz>> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.getQuizzesForLesson(lessonId));
            List<Quiz> quizzes = future.get();
            return quizzes != null ? quizzes.size() : 0;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error counting quizzes for lesson " + lessonId, e);
            return 0;
        }
    }

    /**
     * Check if all quizzes in a lesson have been attempted by a user
     * This requires integration with UserScoreRepository but is useful for progress tracking
     */
    public boolean hasUserCompletedAllQuizzes(int userId, int lessonId, UserScoreRepository userScoreRepository) {
        List<Quiz> quizzes = getQuizzesForLessonSync(lessonId);
        if (quizzes == null || quizzes.isEmpty()) {
            return false;
        }
        
        for (Quiz quiz : quizzes) {
            if (userScoreRepository.getUserHighestScoreForQuiz(userId, quiz.getQuizId()) == 0) {
                // User hasn't attempted this quiz
                return false;
            }
        }
        
        return true;
    }
} 