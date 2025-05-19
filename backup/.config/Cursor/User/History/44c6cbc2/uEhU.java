package com.example.elsa_speak_clone.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.elsa_speak_clone.database.dao.LessonDao;
import com.example.elsa_speak_clone.database.dao.QuizDao;
import com.example.elsa_speak_clone.database.dao.SharedResultDao;
import com.example.elsa_speak_clone.database.dao.UserDao;
import com.example.elsa_speak_clone.database.dao.UserProgressDao;
import com.example.elsa_speak_clone.database.dao.UserScoreDao;
import com.example.elsa_speak_clone.database.dao.VocabularyDao;
import com.example.elsa_speak_clone.database.entity.Lesson;
import com.example.elsa_speak_clone.database.entity.Quiz;
import com.example.elsa_speak_clone.database.entity.SharedResult;
import com.example.elsa_speak_clone.database.entity.User;
import com.example.elsa_speak_clone.database.entity.UserProgress;
import com.example.elsa_speak_clone.database.entity.UserScore;
import com.example.elsa_speak_clone.database.entity.Vocabulary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        User.class,
        Lesson.class,
        Vocabulary.class,
        UserProgress.class,
        Quiz.class,
        UserScore.class,
        SharedResult.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "elsa_speak_clone.db";
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // DAOs
    public abstract UserDao userDao();
    public abstract LessonDao lessonDao();
    public abstract VocabularyDao vocabularyDao();
    public abstract UserProgressDao userProgressDao();
    public abstract QuizDao quizDao();
    public abstract UserScoreDao userScoreDao();
    public abstract SharedResultDao sharedResultDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            
            // Populate the database in the background
            databaseWriteExecutor.execute(() -> {
                // Insert default data here
                // For example:
                // LessonDao lessonDao = INSTANCE.lessonDao();
                // Insert default lessons...
                
                // Populate with default data using DataInitializer
                DataInitializer.populateDatabase(INSTANCE);
            });
        }
    };
} 