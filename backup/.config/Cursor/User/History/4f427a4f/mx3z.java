package com.example.elsa_speak_clone.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.elsa_speak_clone.activities.LoginActivity;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.firebase.FirebaseDataManager;
import com.example.elsa_speak_clone.database.repositories.UserProgressRepository;
import com.example.elsa_speak_clone.services.NavigationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.elsa_speak_clone.activities.DictionaryActivity;
import com.example.elsa_speak_clone.activities.NewsActivity;
import com.example.elsa_speak_clone.activities.LeaderboardActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "HomeFragment";

    private BottomNavigationView bottomNavigationView;
    private boolean isLoggedIn;
    private String username;
    private int userId;
    private int userStreak;
    private Button btnLogin;
    private CardView cvVocabulary;
    private CardView cvGrammar;
    private CardView cvPronunciation;
    private TextView tvDayStreak;
    private TextView tvXPPoint;
    private TextView tvWelcome;
    private ImageView ivPronunciation;
    private ImageView profileImage;
    private IconDrawable iconPronunciation;
    private IconDrawable iconProfile;
    private SessionManager sessionManager;
    NavigationService navigationService;

    // Room database components
    private AppDatabase database;
    private UserProgressRepository userProgressRepository;
    private ExecutorService executor;
    private Handler mainHandler;
    private FirebaseDataManager firebaseDataManager;
    private LinearLayout btnLeaderboard;

    private Handler progressRefreshHandler;
    private final int REFRESH_INTERVAL = 1000; // 1 second refresh interval
    private int refreshCount = 0;
    private final int MAX_REFRESHES = 2;
    private Runnable progressChecker;

    private CardView cvDictionary, cvNews, cvLeaderboard;

    public HomeFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initialize (view);
        setupOther();
        // Load user progress
        loadUserProgress();
        // Set display for user progress
        observeUserProgress();
        refreshUserProgress ();
        
        return view;
    }

    private void setupOther() {
        setupLeaderboardButton ();
        setupWelcomeMessage();
        setupSpeechToTextButton();
        setupGrammarButton();
        setupDictionaryButton();
        setupNewsButton();
        setupLeaderboardButton();
    }

    private void initialize(View view) {
        initializeNavigationService();
        initializeDatabase();
        initializeUI(view);
        initializeVariables();
    }
    private void setupLeaderboardButton() {
        cvLeaderboard.setOnClickListener (v -> {
            navigationService.navigateToLeaderboard (this.requireContext ());
        });
    }
    private void initializeNavigationService() {
        navigationService = new NavigationService(requireContext());
    }

    // Initialize database and related components
    private void initializeDatabase() {
        try {
            database = AppDatabase.getInstance(requireContext());
            userProgressRepository = new UserProgressRepository(requireActivity().getApplication());

            // To run things in background
            executor = Executors.newSingleThreadExecutor();
            mainHandler = new Handler(Looper.getMainLooper());

            firebaseDataManager = FirebaseDataManager.getInstance(this.getContext());

            // For reload progress
            progressRefreshHandler = new Handler(Looper.getMainLooper());
            createProgressCheckerRunnable();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database components", e);
        }
    }

    private void createProgressCheckerRunnable() {
        progressChecker = new Runnable() {
            @Override
            public void run() {
                // Check if reach max loop time
                if (refreshCount < MAX_REFRESHES) {
                    // Load the latest user progress
                    loadUserProgress();
                    refreshCount++;
                    Log.d(TAG, "Progress refresh #" + refreshCount + " completed");

                    // Reload twice for sure
                    if (refreshCount < MAX_REFRESHES) {
                        progressRefreshHandler.postDelayed(this, REFRESH_INTERVAL);
                    } else {
                        Log.d(TAG, "Reached maximum number of refreshes");
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Always refresh progress when returning to this fragment
        startProgressRefresh();
        refreshUserProgress();
        
        // Pull latest data from Firebase
        syncFirebaseProgress();
    }

    private void syncFirebaseProgress() {
        // Get current user ID
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", 1);
        
        executor.execute(() -> {
            try {
                // Force sync all user progress from Firebase to local database
                firebaseDataManager.syncAllUserProgressFromFirebase(userId);
                
                // Then refresh UI on main thread after sync completes
                mainHandler.post(this::refreshUserProgress);
                
                Log.d(TAG, "Firebase progress sync completed for user " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error syncing Firebase progress: " + e.getMessage(), e);
            }
        });
    }

    private void startProgressRefresh() {
        // Reset counter
        refreshCount = 0;
        
        // Start the refresh cycle
        if (progressRefreshHandler != null && progressChecker != null) {
            progressRefreshHandler.post(progressChecker);
            Log.d(TAG, "Started progress refresh cycle");
        }
    }

    private void refreshUserProgress() {
        // Get current user ID
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", 1);
        
        // Create a repository instance
        UserProgressRepository repository = new UserProgressRepository(requireContext());
        
        // Load updated metrics
        repository.loadUserMetrics(userId);
        
        // Observe the LiveData
        repository.getUserStreak().observe(getViewLifecycleOwner(), streak -> {
            if (tvDayStreak != null) {
                tvDayStreak.setText(String.valueOf(streak));
            }
        });
        
        repository.getUserXp().observe(getViewLifecycleOwner(), xp -> {
            if (tvXPPoint != null) {
                tvXPPoint.setText(String.valueOf(xp));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressRefreshHandler != null && progressChecker != null) {
            progressRefreshHandler.removeCallbacks(progressChecker);
            Log.d(TAG, "Refresh timer paused in onPause");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressRefreshHandler != null) {
            progressRefreshHandler.removeCallbacksAndMessages(null);
            progressRefreshHandler = null;
            Log.d(TAG, "Refresh timer cleaned up in onDestroy");
        }

        // Clean up executor
        if (executor != null) {
            executor.shutdown();
        }
    }
    public int getRandomNumber() {
        Random random = new Random();
        return random.nextInt(9) + 1;
    }
    private void initializeUI(View view) {
        try {
            btnLogin = view.findViewById(R.id.btnLogin);
            cvPronunciation = view.findViewById(R.id.cvPronunciation);
            cvGrammar = view.findViewById(R.id.cvGrammar);
            tvDayStreak = view.findViewById(R.id.tvDayStreak);
            tvXPPoint = view.findViewById(R.id.tvXpPoint);
            tvWelcome = view.findViewById(R.id.tvWelcome);
            cvDictionary = view.findViewById(R.id.cvDictionary);
            cvNews = view.findViewById(R.id.cvNews);
            cvLeaderboard = view.findViewById(R.id.cvLeaderboard);
            
            if (tvXPPoint == null) {
                Log.e(TAG, "tvXPPoint is null - check ID in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UI components", e);
        }
    }

    private void initializeVariables() {
        try {
            sessionManager = new SessionManager(requireContext());

            // Update user streak using the Room database
            executor.execute(() -> {
                if (sessionManager.isLoggedIn()) {
                   // Update user streak everytime open app
                    userProgressRepository.updateDailyStreak(sessionManager.getUserId());
                }
            });

            if (sessionManager.isLoggedIn()) {
                isLoggedIn = true;
                username = sessionManager.getUserDetails().get("username");
                userId = sessionManager.getUserId();
            } else {
                isLoggedIn = false;
                username = null;
                userId = -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initializeVariables: ", e);
        }
    }

    private void setupWelcomeMessage() {
        try {
            if (username != null) {
                if (userStreak <= 1) {
                    tvWelcome.setText("Welcome " + username + "!");
                } else {
                    tvWelcome.setText("Welcome back " + username + "!");
                }
            } else {
                tvWelcome.setText("Welcome!");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error on setupWelcomeMessage: " + e.getMessage());
        }
    }

    private void observeUserProgress() {
        if (isLoggedIn) {
            userProgressRepository.getUserStreak().observe(getViewLifecycleOwner(), streak -> {
                userStreak = streak != null ? streak : 0;
                tvDayStreak.setText(userStreak + " Days");
                setupWelcomeMessage(); // Update welcome message based on streak
            });

            userProgressRepository.getUserXp().observe(getViewLifecycleOwner(), xp -> {
                int userXp = xp != null ? xp : 0;
                tvXPPoint.setText(userXp + " XP");
            });
        }
    }

    private void loadUserProgress() {
        try {
            if (isLoggedIn) {
                // Use the repository to load progress
                firebaseDataManager.pullUserProgressToLocal (username, userId);
                userProgressRepository.loadUserMetrics(userId);
            } else {
                // Set default values for not logged in users
                tvXPPoint.setText("1 Days");
                tvDayStreak.setText("0 XP");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserProgress: ", e);
            // Set default values in case of any error
            tvXPPoint.setText("1 Days");
            tvDayStreak.setText("0 XP");
        }
    }

    private void setupSpeechToTextButton() {
        cvPronunciation.setOnClickListener(v -> {
            try {
                navigationService.navigateToSpeechToText(getRandomNumber());
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to speech recognition: ", e);
            }
        });
    }

    private void setupGrammarButton() {
        cvGrammar.setOnClickListener(v -> {
            try {
                // In progress
                navigationService.navigateToQuiz(getRandomNumber());
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to grammar: ", e);
            }
        });
    }


    private void setupDictionaryButton() {
        cvDictionary.setOnClickListener(v -> {
            try {
                navigationService.navigateToDictionary();
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to dictionary: ", e);
                Toast.makeText(requireContext(), "Could not open dictionary", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNewsButton() {
        cvNews.setOnClickListener(v -> {
            try {
               navigationService.navigateToNews (this.requireContext ());
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to news: ", e);
                Toast.makeText(requireContext(), "Could not open news", Toast.LENGTH_SHORT).show();
            }
        });
    }
}