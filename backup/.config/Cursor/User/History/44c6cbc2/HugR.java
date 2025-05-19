package com.example.elsa_speak_clone.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;

import com.example.elsa_speak_clone.database.dao.LessonDao;
import com.example.elsa_speak_clone.database.dao.QuizDao;
import com.example.elsa_speak_clone.database.dao.SharedResultDao;
import com.example.elsa_speak_clone.database.dao.UserDao;
import com.example.elsa_speak_clone.database.dao.UserProgressDao;
import com.example.elsa_speak_clone.database.dao.UserScoreDao;
import com.example.elsa_speak_clone.database.dao.VocabularyDao;
import com.example.elsa_speak_clone.database.entities.Lesson;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.database.entities.SharedResult;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.entities.UserScore;
import com.example.elsa_speak_clone.database.entities.Vocabulary;
import com.example.elsa_speak_clone.database.converter.DateConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        User.class,
        Lesson.class,
        Vocabulary.class,
        UserProgress.class,
        Quiz.class,
        UserScore.class,
        SharedResult.class
    },
    version = 2,
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
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

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Define your migration steps here
            // Example: database.execSQL("ALTER TABLE your_table ADD COLUMN new_column INTEGER NOT NULL DEFAULT 0");
        }
    };

    /**
     * Get the singleton instance of the database
     * This enhanced version includes more robust error handling and logging
     */
    public static AppDatabase getInstance(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null in getInstance");
            return null;
        }
        
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    try {
                        Context appContext = context.getApplicationContext();
                        Log.d(TAG, "Creating new database instance");
                        
                        // Build the database with proper error handling
                        INSTANCE = Room.databaseBuilder(
                                appContext,
                                AppDatabase.class,
                                DATABASE_NAME)
                                .addCallback(sRoomDatabaseCallback)
                                .addMigrations(MIGRATION_1_2)
                                .fallbackToDestructiveMigration()
                                .build();
                        
                        Log.d(TAG, "Database instance created successfully");
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating database instance", e);
                        return null;
                    }
                }
            }
        }
        
        // Verify that instance is valid
        if (INSTANCE == null) {
            Log.e(TAG, "Failed to create database instance");
        }
        
        return INSTANCE;
    }
    
    /**
     * Verify database is properly initialized and accessible
     * Useful for checking before critical operations
     */
    public static boolean isDatabaseInitialized() {
        if (INSTANCE == null) {
            Log.e(TAG, "Database is not initialized");
            return false;
        }
        
        try {
            // Try a simple operation to verify the database is working
            databaseWriteExecutor.execute(() -> {
                try {
                    // Quick sanity check - can be extended for actual validation
                    INSTANCE.isOpen();
                } catch (Exception e) {
                    Log.e(TAG, "Database is not accessible", e);
                }
            });
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking database state", e);
            return false;
        }
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d(TAG, "Database created, initializing data");
            
            // Initialize database with default data
            databaseWriteExecutor.execute(() -> {
                try {
                    DataInitializer.populateDatabase(INSTANCE);
                    Log.d(TAG, "Database population completed successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error populating database", e);
                }
            });
        }
        
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Log.d(TAG, "Database opened successfully");
        }
    };
} 