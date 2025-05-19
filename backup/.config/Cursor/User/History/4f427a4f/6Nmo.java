package com.example.elsa_speak_clone.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.firebase.FirebaseDataManager;
import com.example.elsa_speak_clone.database.repositories.UserProgressRepository;
import com.example.elsa_speak_clone.services.NavigationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.malinskiy.materialicons.IconDrawable;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private boolean isLoggedIn;
    private String username;
    private int userId;
    private int userStreak;
    private Button btnLogin;
    private CardView cvGrammar;
    private CardView cvPronunciation;
    private TextView tvDayStreak;
    private TextView tvXPPoint;
    private TextView tvWelcome;
    private SessionManager sessionManager;
    NavigationService navigationService;

    // Room database components
    private UserProgressRepository userProgressRepository;
    private ExecutorService executor;
    private Handler mainHandler;
    private FirebaseDataManager firebaseDataManager;
    private AppDatabase database;
    private LinearLayout btnLeaderboard;
    private LinearLayout btnChatbot;

    private Handler progressRefreshHandler;
    private final int REFRESH_INTERVAL = 1000; // 1 second refresh interval
    private int refreshCount = 0;
    private final int MAX_REFRESHES = 2;
    private Runnable progressChecker;

    private CardView cvDictionary, cvNews;

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

        // Initialize things
        initializeNavigationService();
        initializeDatabase();
        initializeUI(view);
        initializeVariables();

        // Load user progress
        loadUserProfile();

        // Setup things
        setupWelcomeMessage();
        setupSpeechToTextButton();
        setupGrammarButton();
        setupDictionaryButton();
        setupNewsButton();
        setupChatbotButton();
        setupLeaderboardButton();
        return view;
    }


    // Initialize database and related components
    private void initializeDatabase() {
        try {
            userProgressRepository = new UserProgressRepository(requireActivity().getApplication());

            // To run things in background
            executor = Executors.newSingleThreadExecutor();
            mainHandler = new Handler(Looper.getMainLooper());

            firebaseDataManager = FirebaseDataManager.getInstance(this.getContext());
            database = AppDatabase.getInstance(this.requireContext());
            // For reload progress
            progressRefreshHandler = new Handler(Looper.getMainLooper());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database components", e);
        }
    }

    private void initializeUI(View view) {
        try {
            btnChatbot = view.findViewById(R.id.btnChatbot);
            btnLogin = view.findViewById(R.id.btnLogin);
            cvPronunciation = view.findViewById(R.id.cvPronunciation);
            cvGrammar = view.findViewById(R.id.cvGrammar);
            tvDayStreak = view.findViewById(R.id.tvDayStreak);
            tvXPPoint = view.findViewById(R.id.tvXpPoint);
            tvWelcome = view.findViewById(R.id.tvWelcome);
            cvDictionary = view.findViewById(R.id.cvDictionary);
            cvNews = view.findViewById(R.id.cvNews);
            btnLeaderboard = view.findViewById(R.id.btnLeaderboard);
        } catch (Exception e) {
            Log.d(TAG, "Error in initializeUI()", e);
        }
        if (tvXPPoint == null) {
            Log.e(TAG, "tvXPPoint is null - check ID in layout");
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

    public void setupChatbotButton() {
        btnChatbot.setOnClickListener(v -> {
            navigationService.navigateToChatbot(this.requireContext());
        });
    }

    private void setupLeaderboardButton() {
        btnLeaderboard.setOnClickListener(v -> {
            navigationService.navigateToLeaderboard(this.requireContext());
        });
    }

    private void initializeNavigationService() {
        navigationService = new NavigationService(requireContext());
    }

    private void setupSpeechToTextButton() {
        cvPronunciation.setOnClickListener(v -> {
                navigationService.navigateToSpeechToText(getRandomNumber());
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
                navigationService.navigateToNews(this.requireContext());
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to news: ", e);
                Toast.makeText(requireContext(), "Could not open news", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadUserProfile() {
        int userId = sessionManager.getUserId();

        // Retrieve user data from Room database in background thread
        executor.execute(() -> {
            try {
                // Load user progress
                // Using main thread because using LiveData
                mainHandler.post(() -> {
                    if (isAdded()) {

                        userProgressRepository.loadUserMetrics(userId);
                        // Set up LiveData observers
                        userProgressRepository.getUserStreak().observe(getViewLifecycleOwner(), streak -> {
                            int userStreak = streak != null ? streak : 0;
                            tvDayStreak.setText(String.valueOf(userStreak) + " Days");
                        });

                        userProgressRepository.getUserXp().observe(getViewLifecycleOwner(), xp -> {
                            int userXp = xp != null ? xp : 0;
                            tvXPPoint.setText(String.valueOf(userXp) + " XP");
                        });

                        UserProgress progress = userProgressRepository.getCurrentProgress().getValue();
                        if (progress != null) {
                            tvDayStreak.setText(progress.getStreak());
                            tvXPPoint.setText(progress.getXp());
                        }

                        // Trigger the loading of metrics
                        userProgressRepository.loadUserMetrics(userId);
                        firebaseDataManager.syncUserProgress(userId, 1);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading user profile", e);

                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(),
                                "Error loading profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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
                mainHandler.post(this::loadUserProgress);

                Log.d(TAG, "Firebase progress sync completed for user " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error syncing Firebase progress: " + e.getMessage(), e);
            }
        });
    }

    public int getRandomNumber() {
        Random random = new Random();
        return random.nextInt(9) + 1;
    }


    @Override
    public void onResume() {
        super.onResume();

        refreshUserData();
    }

    private void refreshUserData() {
    try {
        if (!isAdded() || !isLoggedIn) {
            // Set default values for not logged in users or if fragment is detached
            if (tvXPPoint != null) tvXPPoint.setText("0 XP");
            if (tvDayStreak != null) tvDayStreak.setText("1 Days");
            return;
        }
        
        Log.d(TAG, "Refreshing user data for user: " + username);
        
        // First pull from Firebase to ensure we have the latest data
        executor.execute(() -> {
            try {
                // Sync data from Firebase to local database
                firebaseDataManager.syncAllUserProgressFromFirebase(userId);
                
                // After sync completes, load from local database on main thread
                mainHandler.post(() -> {
                    if (isAdded()) {
                        // Load metrics via LiveData observers
                        userProgressRepository.loadUserMetrics(userId);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error syncing Firebase data: " + e.getMessage(), e);
                
                // If Firebase sync fails, still try to load local data
                mainHandler.post(() -> {
                    if (isAdded()) {
                        userProgressRepository.loadUserMetrics(userId);
                    }
                });
            }
        });
    } catch (Exception e) {
        Log.e(TAG, "Error in refreshUserData: ", e);
        // Set default values in case of any error
        if (tvXPPoint != null) tvXPPoint.setText("0 XP");
        if (tvDayStreak != null) tvDayStreak.setText("1 Days");
    }
}

    @Override
    public void onPause() {
        super.onPause();
        // Remove any pending refresh callbacks
    if (mainHandler != null) {
        mainHandler.removeCallbacksAndMessages(null);
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

}