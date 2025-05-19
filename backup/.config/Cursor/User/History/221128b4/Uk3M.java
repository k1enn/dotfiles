package com.example.elsa_speak_clone.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.GoogleSignInHelper;
import com.example.elsa_speak_clone.services.AuthenticationService;
import com.example.elsa_speak_clone.services.NavigationService;
import com.google.firebase.auth.FirebaseUser;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.UserProgress;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.example.elsa_speak_clone.database.firebase.FirebaseDataManager;
import com.example.elsa_speak_clone.database.repositories.UserProgressRepository;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageButton btnToggleLoginPassword;
    private TextView btnRegister;
    private LinearLayout googleLoginButton;
    private AuthenticationService authService;
    private GoogleSignInHelper googleSignInHelper;
    private NavigationService navigationService;
    private SessionManager sessionManager;
    private View loading;
    private FirebaseDataManager firebaseDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize services
        authService = new AuthenticationService(this);
        navigationService = new NavigationService(this);
        sessionManager = new SessionManager(this);
        firebaseDataManager = FirebaseDataManager.getInstance(this);
        
        // Initialize UI and setup interactions
        initializeUI();
        initializeGoogleLogin();
        setupLoginButton();
        setupRegisterButton();
        setupGoogleLoginButton();
        setupShowPasswordButton();
    }

    private void initializeUI() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        googleLoginButton = findViewById(R.id.btnGoogleLogin);
        btnToggleLoginPassword = findViewById(R.id.btnToggleLoginPassword);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupShowPasswordButton() {
        btnToggleLoginPassword.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide Password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnToggleLoginPassword.setImageResource(R.drawable.ic_eye_closed);
                    etPassword.setTypeface(null, Typeface.NORMAL);
                } else {
                    // Show Password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnToggleLoginPassword.setImageResource(R.drawable.ic_eye_open);
                    etPassword.setTypeface(null, Typeface.NORMAL);
                }
                isPasswordVisible = !isPasswordVisible;
                etPassword.setSelection(etPassword.getText().length()); // Move cursor to the end
            }
        });
    }

    private void initializeGoogleLogin() {
        googleSignInHelper = new GoogleSignInHelper(this, new GoogleSignInHelper.AuthCallback() {
            private Context context;

            @Override
            public void onSuccess(FirebaseUser user) {
                if (user == null || user.getEmail() == null) {
                    Toast.makeText(LoginActivity.this, "Failed to get user data", Toast.LENGTH_SHORT).show();
                    return;
                }

                String email = user.getEmail();
                String displayName = user.getDisplayName() != null ?
                        user.getDisplayName() : email.split("@")[0];

                try {
                    // First try to authenticate, and if that fails, register
                    if (authService.authenticateGoogleUser(email)) {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        handleLoginSuccess(authService.getLocalUser());
                    } else {
                        // User doesn't exist, register new Google user
                        Log.d(TAG, "Registering new Google user: " + email);
                        if (authService.registerGoogleUser(email)) {
                            Toast.makeText(LoginActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            handleLoginSuccess(authService.getLocalUser());
                        } else {
                            Toast.makeText(LoginActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in Google sign-in process", e);
                    Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Google Sign-In Error: " + message);
            }
        });
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(username, password);
        });
    }

    private void loginUser(String username, String password) {
        // Show loading indicator first
        showLoading(true);
        
        firebaseDataManager.authenticateUser(username, password)
            .thenAcceptAsync(user -> {
                if (user != null) {
                    // Login success - Firebase authentication succeeded
                    Log.d(TAG, "Firebase authentication successful for: " + username);
                    
                    // Pull user data and progress from Firebase
                    checkAndPullUserProgress(user);
                    
                    // Create local session
                    sessionManager.createLoginSession(user.getUserId(), user.getName());
                    
                    // Navigate to main screen on UI thread
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    });
                } else {
                    // Try local authentication as fallback
                    try {
                        if (authService.authenticateLocalUser(username, password)) {
                            // Local authentication succeeded
                            User localUser = authService.getLocalUser();
                            Log.d(TAG, "Local authentication successful for: " + username);
                            
                            // Create session
                            sessionManager.createLoginSession(localUser.getUserId(), localUser.getName());
                            
                            runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(this, "Login Successful (Offline Mode)", Toast.LENGTH_SHORT).show();
                                navigateToMainActivity();
                            });
                        } else {
                            // Both Firebase and local authentication failed
                            runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error during local authentication: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(this, "Authentication error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            })
            .exceptionally(e -> {
                Log.e(TAG, "Firebase authentication error: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Network error. Trying local authentication...", Toast.LENGTH_SHORT).show();
                    
                    // Try local authentication as fallback
                    try {
                        if (authService.authenticateLocalUser(username, password)) {
                            User localUser = authService.getLocalUser();
                            sessionManager.createLoginSession(localUser.getUserId(), localUser.getName());
                            navigateToMainActivity();
                        } else {
                            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error during local authentication: " + ex.getMessage(), ex);
                        Toast.makeText(this, "Authentication error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            });
    }

    // Helper method to show/hide loading indicator
    private void showLoading(boolean show) {
        if (loading != null) {
            loading.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupGoogleLoginButton() {
        googleLoginButton.setOnClickListener(v -> {
            googleSignInHelper.signOut();
            googleSignInHelper.signIn();
        });
    }

    private void setupRegisterButton() {
        btnRegister.setOnClickListener(v -> {
            navigationService.navigateToRegister();
        });
    }

    // Handle Google Sign-In Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleSignInHelper.handleActivityResult(requestCode, resultCode, data);
    }

    private void handleLoginSuccess(User user) {
        // Create session
        sessionManager.createSession(user.getName(), user.getUserId());
        
        // Continue with login navigation
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkAndPullUserProgress(User user) {
        // Check if user has any local progress
        UserProgressRepository progressRepo = new UserProgressRepository(getApplication());
        List<UserProgress> localProgress = progressRepo.getUserProgressList(user.getUserId());
        
        // If no local progress or minimal progress, try to pull from Firebase
        if (localProgress == null || localProgress.isEmpty() || shouldPullProgress(localProgress)) {
            Log.d(TAG, "Pulling user progress from Firebase for: " + user.getName());
            
            // Pull progress data from Firebase
            firebaseDataManager.pullUserProgressToLocal(user.getName(), user.getUserId())
                .thenAccept(success -> {
                    if (success) {
                        Log.d(TAG, "Successfully pulled user progress from Firebase");
                    } else {
                        Log.d(TAG, "No progress found in Firebase or pull failed");
                    }
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error pulling user progress: " + e.getMessage(), e);
                    return null;
                });
        } else {
            Log.d(TAG, "Using existing local progress for user: " + user.getName());
        }
        
        // Also pull user stats (XP, streak) - this is separate from lesson progress
        firebaseDataManager.getUserFromFirebase(user.getName())
            .thenAccept(firebaseUser -> {
                if (firebaseUser != null) {
                    Log.d(TAG, "Successfully pulled user data from Firebase");
                    // This will update the local user with Firebase data
                    firebaseDataManager.syncUserToLocal(user, null, null);
                }
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error pulling user data: " + e.getMessage(), e);
                return null;
            });
    }

    private boolean shouldPullProgress(List<UserProgress> progressList) {
        // Pull if total XP is very low (suggesting new installation)
        int totalXp = 0;
        for (UserProgress progress : progressList) {
            totalXp += progress.getXp();
        }
        return totalXp < 10; // Arbitrary threshold
    }
}
