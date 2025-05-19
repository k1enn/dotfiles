package com.example.elsa_speak_clone.database;

import android.content.Context;

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
    version = 1,
    exportSchema = false
)
@TypeConverters({DateConverter.class})
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

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add missing column
            database.execSQL("ALTER TABLE Users ADD COLUMN last_login TEXT");
            // Update constraints as needed
            // Be careful with existing data
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2)
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
            
            // Initialize database
            databaseWriteExecutor.execute(() -> {
                DataInitializer.populateDatabase(INSTANCE);
            });
        }
    };
} 