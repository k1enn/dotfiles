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
import com.example.elsa_speak_clone.database.firebase.FirebaseManager;

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
                        navigationService.navigateToMain();

                        // If this is a returning user after app reinstall, try to restore data from Firebase
                        SyncManager syncManager = new SyncManager(this.context);
                        syncManager.recoverUserFromFirebase(email)
                            .thenAccept(recovered -> {
                                if (recovered) {
                                    Toast.makeText(this.context, "Your data has been restored", Toast.LENGTH_SHORT).show();
                                }
                                // Continue with login regardless of recovery result
                                navigationService.navigateToMain();
                            })
                            .exceptionally(e -> {
                                Log.e(TAG, "Error recovering user data: " + e.getMessage());
                                // Continue with login even if recovery fails
                                navigationService.navigateToMain();
                                return null;
                            });
                    } else {
                        // User doesn't exist, register new Google user
                        Log.d(TAG, "Registering new Google user: " + email);
                        if (authService.registerGoogleUser(email)) {
                            Toast.makeText(LoginActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            navigationService.navigateToMain();
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

    private void loginUser(String usernameOrEmail, String password) {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        
        // First check local database login
        executor.execute(() -> {
            try {
                // Get user from local database
                User user = userRepository.getUserByEmailOrUsername(usernameOrEmail);
                
                if (user != null) {
                    // Verify password
                    boolean passwordMatches = userRepository.verifyPassword(user, password);
                    
                    if (passwordMatches) {
                        // Local login successful
                        // Try to sync with Firebase in background
                        FirebaseManager.getInstance().getUserByEmail(user.getGmail())
                            .thenAccept(firebaseUser -> {
                                if (firebaseUser == null) {
                                    // User doesn't exist in Firebase, so sync local user
                                    FirebaseManager.getInstance().saveUser(user);
                                }
                            });
                        
                        // Update session
                        sessionManager.createLoginSession(user.getUserId(), user.getName());
                        
                        // Navigate to main activity
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            navigateToMainActivity();
                        });
                    } else {
                        // Invalid password
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            showError("Invalid password");
                        });
                    }
                } else {
                    // User not found in local database
                    // Try Firebase as a fallback
                    FirebaseManager.getInstance().getUserByEmail(usernameOrEmail)
                        .thenAccept(firebaseUser -> {
                            if (firebaseUser != null) {
                                // User found in Firebase, save to local database
                                userRepository.insertUser(firebaseUser);
                                
                                // Update session
                                sessionManager.createLoginSession(firebaseUser.getUserId(), firebaseUser.getName());
                                
                                // Navigate to main activity
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    navigateToMainActivity();
                                });
                            } else {
                                // User not found anywhere
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    showError("User not found. Please register.");
                                });
                            }
                        });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during login: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    showError("Login failed: " + e.getMessage());
                });
            }
        });
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
}
