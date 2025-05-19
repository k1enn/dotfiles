package com.example.elsa_speak_clone.fragments;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.elsa_speak_clone.activities.LoginActivity;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageView ivProfilePicture;
    private TextView tvUsername, tvEmail, tvUserStreak, tvUserXP;
    private SessionManager sessionManager;
    private Button btnLogout;
    private Button btnShare;
    private Button btnSettings;
    private final String TAG = "ProfileFragment";
    
    // Room database components
    private AppDatabase database;
    private ExecutorService executor;
    private Handler mainHandler;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Room database and threading components
        database = AppDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initializeUI(view);
        loadUserProfile();

        setupLogoutButton();
        setupSettingsButton();
        setupShareProfileButton();

        return view;
    }

    private void initializeUI(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvUserStreak = view.findViewById(R.id.tvDayStreak);
        tvUserXP = view.findViewById(R.id.tvXPPoint);
        // Initialize the button variables
        btnLogout = view.findViewById(R.id.btnLogout);
        btnShare = view.findViewById(R.id.btnShare);
        btnSettings = view.findViewById(R.id.btnSettings);
        sessionManager = new SessionManager(requireContext());
    }

    private void setupShareProfileButton() {
        btnShare.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Share button clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSettingsButton() {
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Settings button clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            try {
                // First, check if user is logged in with Google
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    // Sign out from Firebase Auth
                    FirebaseAuth.getInstance().signOut();

                    // Clear Google sign-in credentials
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();
                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
                    googleSignInClient.signOut().addOnCompleteListener(task -> {
                        Log.d(TAG, "Google Sign-Out successful");
                        // Clear local session data after Google sign-out
                        sessionManager.clearSession();
                        navigateToLogin();
                    });
                } else {
                    // Just local authentication, use SessionManager logout
                    sessionManager.logout();
                    navigateToLogin();
                }
            } catch (Exception e) {
                Log.e(TAG, "Logout button error", e);
            }
        });
    }

    private void navigateToLogin() {
        Activity main = null;
        try {
            main = requireActivity();
            Intent intent = new Intent(main, LoginActivity.class);
            startActivity(intent);
            requireActivity().finish(); // Optional: finish the activity to prevent returning to it
        } catch (NullPointerException e) {
            Log.d(TAG, "Activity is NULL", e);
        } finally {
            Log.d(TAG, "Successful navigate to Login");
        }
    }

    private void loadUserProfile() {
        String username = sessionManager.getUserDetails().get("username");
        int userId = sessionManager.getUserId();
        
        // Set initial values
        if (username != null) {
            tvUsername.setText(username);
        }
        
        // Retrieve user data from Room database in background thread
        executor.execute(() -> {
            try {
                // Get user data from Room database
                User user = database.userDao().getUserById(userId);
                
                // Get user stats - assuming you have a UserStatsDao or similar
                // If you have these fields in User entity, you can use the user object directly
                int streak = 0;
                int xp = 0;
                
                try {
                    // Replace with your actual DAO methods to get streak and XP
                    // Example: 
                    // UserStats stats = database.userStatsDao().getStatsByUserId(userId);
                    // streak = stats.getStreak();
                    // xp = stats.getXp();
                    
                    // For now, we'll use placeholder values until your DB structure is clear
                    streak = 5; // Placeholder
                    xp = 100;   // Placeholder
                } catch (Exception e) {
                    Log.e(TAG, "Error retrieving user stats", e);
                }
                
                // Need final variables for lambda
                final int finalStreak = streak;
                final int finalXp = xp;
                final User finalUser = user;
                
                // Update UI on main thread
                mainHandler.post(() -> {
                    if (isAdded()) { // Check if fragment is still attached
                        if (finalUser != null) {
                            tvUsername.setText(finalUser.getName());
                            // If you have email field in your TextView
                            if (tvEmail != null) {
                                tvEmail.setText(finalUser.getGmail());
                            }
                        }
                        
                        tvUserStreak.setText(String.valueOf(finalStreak));
                        tvUserXP.setText(String.valueOf(finalXp));
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
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (executor != null) {
            executor.shutdown();
        }
    }
}