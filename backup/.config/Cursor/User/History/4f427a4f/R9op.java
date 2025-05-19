package com.example.elsa_speak_clone.fragments;

import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.elsa_speak_clone.activities.LoginActivity;
import com.example.elsa_speak_clone.activities.QuizActivity;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.repositories.UserProgressRepository;
import com.example.elsa_speak_clone.activities.SpeechToText;
import com.example.elsa_speak_clone.services.NavigationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;

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


    public HomeFragment() {
        // Required empty public constructor
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

    private Handler progressRefreshHandler;
    private final int REFRESH_INTERVAL = 1000; // 1 second refresh interval
    private int refreshCount = 0;
    private final int MAX_REFRESHES = 2;
    private Runnable progressChecker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize navigation service
        navigationService = new NavigationService(requireContext());
        
        // Initialize database and repository
        database = AppDatabase.getInstance(requireContext());
        userProgressRepository = new UserProgressRepository(requireActivity().getApplication());
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize UI components
        initializeUI(view);
        
        // Initialize other components
        initializeVariables();
        
        // Setup click listeners and UI updates
        setupWelcomeMessage();
        setupSpeechToTextButton();
        setupGrammarButton();
        setupVocabularyButton();
        
        // Only setup login button if it exists
        if (btnLogin != null) {
            setupLoginButton();
        }
        
        // Load user progress
        loadUserProgress();
        observeUserProgress();
        
        return view;
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

                    // Ready for next loop
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
        if (progressRefreshHandler != null && progressChecker != null) {
            refreshCount = 0;
            progressRefreshHandler.removeCallbacks(progressChecker);
            progressRefreshHandler.postDelayed(progressChecker, REFRESH_INTERVAL);
            Log.d(TAG, "Refresh timer restarted in onResume");
        }
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

    private void initializeUI(View view) {
        try {
            // Find all the views from the inflated layout
            btnLogin = view.findViewById(R.id.btnLogin);
            cvPronunciation = view.findViewById(R.id.cvPronunciation);
            cvGrammar = view.findViewById(R.id.cvGrammar);
            cvVocabulary = view.findViewById(R.id.cvVocabulary);
            tvDayStreak = view.findViewById(R.id.tvDayStreak);
            tvXPPoint = view.findViewById(R.id.tvXPPoint);
            tvWelcome = view.findViewById(R.id.tvWelcome);
 
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UI components", e);
        }

        try {
            ivPronunciation = view.findViewById(R.id.ivPronunciation);
            if (ivPronunciation != null) {
                iconPronunciation = new IconDrawable(requireContext(), Iconify.IconValue.zmdi_volume_up)
                        .colorRes(R.color.real_purple)  // Set color
                        .sizeDp(48); // Set size
                iconPronunciation.setStyle(Paint.Style.FILL);
                ivPronunciation.setImageDrawable(iconPronunciation);
            }

            profileImage = view.findViewById(R.id.profileImage);
            if (profileImage != null) {
                iconProfile = new IconDrawable(requireContext(), Iconify.IconValue.zmdi_account_circle)
                        .colorRes(R.color.gray)  // Set color
                        .sizeDp(70); // Set size
                iconProfile.setStyle(Paint.Style.FILL);
                profileImage.setImageDrawable(iconProfile);
            }
        } catch (NullPointerException npe) {
            Log.d(TAG, "Can't find icons: " + npe);
        }
    }

    private void initializeVariables() {
        try {
            // Use SessionManager for user information
            sessionManager = new SessionManager(requireContext());

            // Update user streak using the Room database
            executor.execute(() -> {
                if (sessionManager.isLoggedIn()) {
                    // Using UserProgressRepository to update streak
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
            // Observe streak changes
            userProgressRepository.getUserStreak().observe(getViewLifecycleOwner(), streak -> {
                userStreak = streak != null ? streak : 0;
                tvDayStreak.setText(String.valueOf(userStreak));
                setupWelcomeMessage(); // Update welcome message based on streak
            });

            // Observe XP changes
            userProgressRepository.getUserXp().observe(getViewLifecycleOwner(), xp -> {
                int userXp = xp != null ? xp : 0;
                tvXPPoint.setText(String.valueOf(userXp));
            });
        }
    }

    private void loadUserProgress() {
        try {
            if (isLoggedIn) {
                // Use the repository to load metrics
                userProgressRepository.loadUserMetrics(userId);
            } else {
                // Set default values for not logged in users
                tvXPPoint.setText("0");
                tvDayStreak.setText("0");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserProgress: ", e);
            // Set default values in case of any error
            tvXPPoint.setText("0");
            tvDayStreak.setText("0");
        }
    }

    private void setupLoginButton() {
        // Add null check before setting OnClickListener
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                try {
                    if (sessionManager.isLoggedIn()) {
                        sessionManager.logout();
                        navigateToLogin();
                    } else {
                        navigateToLogin();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error in setupLoginButton", e);
                }
            });
        } else {
            Log.e(TAG, "Login button not found in layout");
        }
    }

    private void navigateToLogin() {
        try {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to login: ", e);
        }
    }

    private void setupSpeechToTextButton() {
        cvPronunciation.setOnClickListener(v -> {
            try {
                navigationService.navigateToSpeechToText();
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to speech recognition: ", e);
            }
        });
    }

    private void setupGrammarButton() {
        cvGrammar.setOnClickListener(v -> {
            try {
                // In progress
                navigationService.navigateToQuiz(1);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to grammar: ", e);
            }
        });
    }

    private void setupVocabularyButton() {
        cvVocabulary.setOnClickListener(v -> {
            // Launch vocabulary activity or fragment
            try {
                navigationService.navigateToDictionary();
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to vocabulary: ", e);
            }
        });
    }
}