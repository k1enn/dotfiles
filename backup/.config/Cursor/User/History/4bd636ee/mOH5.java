package com.example.elsa_speak_clone.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.activities.QuizActivity;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.Lesson;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.classes.LessonAdapter;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.activities.SpeechToText;
import com.example.elsa_speak_clone.database.DataInitializer;
import com.example.elsa_speak_clone.database.repositories.QuizRepository;
import com.example.elsa_speak_clone.services.NavigationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LearnFragment extends Fragment {

    private final String TAG = "LearnFragment";

    private RecyclerView recyclerLessons;
    private ProgressBar progressBar;
    private AppDatabase database;
    private ExecutorService executor;
    private Handler mainHandler;
    private NavigationService navigationService;
    private QuizRepository quizRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_learn, container, false);

        recyclerLessons = view.findViewById(R.id.recyclerLessons);
        progressBar = view.findViewById(R.id.progressBar);
        
        Button btnDebugRefresh = view.findViewById(R.id.btnDebugRefresh);
        if (btnDebugRefresh != null) {
            btnDebugRefresh.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Forcing data refresh...", Toast.LENGTH_SHORT).show();
                forceDataUpdate();
            });
        }
        
        initialize();
        loadLessons();
        
        return view;
    }

    private void initialize() {
        navigationService = new NavigationService(requireContext());

        database = AppDatabase.getInstance(requireContext());
        quizRepository = new QuizRepository(requireContext());
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    private void loadLessons() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        executor.execute(() -> {
            try {
                List<Lesson> lessonList = database.lessonDao().getAllLessons();
                Log.d(TAG, "Loaded " + lessonList.size() + " lessons from database");
                
                for (Lesson lesson : lessonList) {
                    Log.d(TAG, "Lesson: " + lesson.getLessonId() + " - " + lesson.getTopic());
                    
                    int quizCount = quizRepository.countQuizzesForLesson(lesson.getLessonId());
                    Log.d(TAG, "Lesson " + lesson.getLessonId() + " has " + quizCount + " quizzes");
                }

                if (lessonList.size() < 3) {
                    forceDataUpdate();
                }

                mainHandler.post(() -> {
                    if (isAdded()) {
                        setupRecyclerView(lessonList);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading lessons", e);
                
                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), 
                                "Error loading lessons: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void setupRecyclerView(List<Lesson> lessonList) {
        if (lessonList.isEmpty()) {
            Toast.makeText(requireContext(), "No lessons available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        LessonAdapter adapter = new LessonAdapter(lessonList, database, lesson -> {
            Toast.makeText(requireContext(), "Selected: " + lesson.getTopic(), Toast.LENGTH_SHORT).show();

            executor.execute(() -> {
                try {
                    List<Quiz> quizzes = quizRepository.getQuizzesForLessonSync(lesson.getLessonId());
                    Log.d(TAG, "Preloaded " + quizzes.size() + " quizzes for lesson " + lesson.getLessonId());
                    
                    mainHandler.post(() -> {
                        navigationService.navigateToQuiz(lesson.getLessonId());
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error preloading quizzes for lesson " + lesson.getLessonId(), e);
                    mainHandler.post(() -> {
                        navigationService.navigateToQuiz(lesson.getLessonId());
                    });
                }
            });
        });

        recyclerLessons.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerLessons.setAdapter(adapter);
    }

    private void forceDataUpdate() {
        executor.execute(() -> {
            try {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        DataInitializer.updateData(database);
                        Log.d(TAG, "Forced data update completed");
                        
                        List<Quiz> allQuizzes = quizRepository.getAllQuizzesSync();
                        Log.d(TAG, "Total quizzes after update: " + allQuizzes.size());
                        
                        mainHandler.post(this::loadLessons);
                    } catch (Exception e) {
                        Log.e(TAG, "Error during forced data update", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to execute data update", e);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}