package com.example.elsa_speak_clone.database;

import android.util.Log;

import com.example.elsa_speak_clone.database.entity.Lesson;
import com.example.elsa_speak_clone.database.entity.Quiz;
import com.example.elsa_speak_clone.database.entity.Vocabulary;

import java.util.ArrayList;
import java.util.List;

public class DataInitializer {
    private static final String TAG = "DataInitializer";

    public static void populateDatabase(AppDatabase db) {
        try {
            insertDefaultLessons(db);
            insertDefaultVocabulary(db);
            insertDefaultQuizzes(db);
            Log.d(TAG, "Database initialized with default data");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database: " + e.getMessage(), e);
        }
    }

    private static void insertDefaultLessons(AppDatabase db) {
        List<Lesson> lessons = new ArrayList<>();
        
        // Add lessons data similar to your original insertDefaultLessons method
        lessons.add(new Lesson(1, "Basic Greetings", "Learn common greetings...", 1));
        lessons.add(new Lesson(2, "Family Members", "Vocabulary about family...", 1));
        // Add all your lessons...
        
        db.lessonDao().insertAll(lessons);
    }

    private static void insertDefaultVocabulary(AppDatabase db) {
        List<Vocabulary> vocabularyList = new ArrayList<>();
        
        // Add vocabulary data similar to your original insertDefaultVocabulary method
        vocabularyList.add(new Vocabulary(1, "Hello", "/həˈloʊ/", 1));
        vocabularyList.add(new Vocabulary(2, "Goodbye", "/ˌɡʊdˈbaɪ/", 1));
        // Add all your vocabulary...
        
        db.vocabularyDao().insertAll(vocabularyList);
    }

    private static void insertDefaultQuizzes(AppDatabase db) {
        List<Quiz> quizzes = new ArrayList<>();
        
        // Add quiz data similar to your original insertDefaultQuizzes method
        quizzes.add(new Quiz(1, "How ___ you?", "are", 1));
        quizzes.add(new Quiz(2, "What ___ your name?", "is", 1));
        // Add all your quizzes...
        
        db.quizDao().insertAll(quizzes);
    }
} 