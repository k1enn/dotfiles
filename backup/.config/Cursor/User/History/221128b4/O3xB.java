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
import com.example.elsa_speak_clone.database.firebase.FirebaseUserManager;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.UserProgress;

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
        try {
            if (authService.authenticateLocalUser(username, password)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                handleLoginSuccess(authService.getLocalUser());
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Login failed: ", e);
            Toast.makeText(this, "Login Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
        
        // Get Firebase manager
        FirebaseUserManager firebaseManager = FirebaseUserManager.getInstance(this);
        
        // First, sync user account data
        firebaseManager.syncUserData(user)
            .thenCompose(success -> {
                // Check if user has local progress
                UserProgress localProgress = null;
                try {
                    localProgress = AppDatabase.getInstance(this)
                        .userProgressDao().getUserProgressById(user.getUserId());
                } catch (Exception e) {
                    Log.e(TAG, "Error checking local progress", e);
                }
                
                if (localProgress == null) {
                    // No local progress found - try to restore from Firebase first
                    Log.d(TAG, "No local progress found, attempting to restore from Firebase");
                    return firebaseManager.restoreUserProgress(user.getUserId())
                        .thenCompose(restored -> {
                            if (restored) {
                                Log.d(TAG, "Progress restored from Firebase");
                            } else {
                                Log.d(TAG, "No progress found in Firebase, using defaults");
                            }
                            // Now sync any changes back to Firebase
                            return firebaseManager.syncUserProgress(user.getUserId());
                        });
                } else {
                    // Local progress exists, just sync it to Firebase
                    Log.d(TAG, "Local progress found, syncing to Firebase");
                    return firebaseManager.syncUserProgress(user.getUserId());
                }
            })
            .thenAccept(result -> {
                Log.d(TAG, "User data synchronization completed");
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error during sync process: " + e.getMessage(), e);
                return null;
            });
        
        // Continue with login navigation
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
