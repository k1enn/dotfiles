package com.example.elsa_speak_clone.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.GoogleSignInHelper;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.repositories.UserRepository;
import com.example.elsa_speak_clone.services.AuthenticationService;
import com.example.elsa_speak_clone.services.NavigationService;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.elsa_speak_clone.database.firebase.FirebaseDataManager;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    
    // UI elements
    private EditText etNewUsername;
    private EditText etNewPassword;
    private EditText etRewritePassword;
    private Button btnRegisterUser;
    private ImageButton btnTogglePassword;
    private ImageButton btnToggleRewritePassword;
    private LinearLayout btnGoogleRegister;
    private TextView btnLogin;
    private TextView tvPassword;
    private TextView tvRewritePassword;
    private TextView tvUsername;
    
    // Services
    private UserRepository userRepository;
    private AuthenticationService authService;
    private NavigationService navigationService;
    private GoogleSignInHelper googleSignInHelper;
    private FirebaseDataManager firebaseDataManager;

    // Add this as a class field
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeServices();
        initializeViews();
        setupRegisterButton();
        setupLoginButton();
        setupShowPasswordButton(etNewPassword, btnTogglePassword);
        setupShowPasswordButton(etRewritePassword, btnToggleRewritePassword);
        initializeGoogleRegister();
        setupGoogleRegisterButton();
    }

    private void initializeServices() {
        authService = new AuthenticationService(this);
        navigationService = new NavigationService(this);
        userRepository = new UserRepository(getApplication());
        firebaseDataManager = FirebaseDataManager.getInstance(this);
    }

    private void initializeViews() {
        etNewUsername = findViewById(R.id.etUsername);
        etNewPassword = findViewById(R.id.etPassword);
        etRewritePassword = findViewById(R.id.etRewritePassword);
        tvPassword = findViewById(R.id.tvPassword);
        tvRewritePassword = findViewById(R.id.tvRewritePassword);
        tvUsername = findViewById(R.id.tvUsername);
        btnRegisterUser = findViewById(R.id.btnRegisterUser);
        btnLogin = findViewById(R.id.btnLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleRewritePassword = findViewById(R.id.btnToggleLoginPassword);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            navigationService.navigateToLogin();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupShowPasswordButton(EditText editText, ImageButton imageButton) {
        imageButton.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide Password
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imageButton.setImageResource(R.drawable.ic_eye_closed); // Change to closed eye icon
                    editText.setTypeface(null, Typeface.NORMAL); // Set to default font
                } else {
                    // Show Password
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imageButton.setImageResource(R.drawable.ic_eye_open); // Change to open eye icon
                    editText.setTypeface(null, Typeface.NORMAL); // Set to default font
                }
                isPasswordVisible = !isPasswordVisible;
                editText.setSelection(editText.getText().length()); // Move cursor to the end
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleSignInHelper.RC_SIGN_IN) {
            googleSignInHelper.handleActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupGoogleRegisterButton() {
        btnGoogleRegister.setOnClickListener(v -> googleSignInHelper.signIn());
    }

    private void initializeGoogleRegister() {
        googleSignInHelper = new GoogleSignInHelper(this, new GoogleSignInHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "onSuccess called with user: " + (user != null ? user.getEmail() : "null"));

                if (user == null || user.getEmail() == null) {
                    Toast.makeText(RegisterActivity.this, "Failed to get user data", Toast.LENGTH_SHORT).show();
                    return;
                }

                String email = user.getEmail();
                String displayName = user.getDisplayName() != null ?
                        user.getDisplayName() : email.split("@")[0];

                try {
                    // First try to authenticate as a returning Google user
                    if (authService.authenticateGoogleUser(email)) {
                        Toast.makeText(RegisterActivity.this, "Account already exists, logging in...", Toast.LENGTH_SHORT).show();
                        navigationService.navigateToMain();
                    } else {
                        // New user - register
                        Log.d(TAG, "Registering new Google user: " + email);
                        if (authService.registerGoogleUser(email)) {
                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            
                            // Get the user ID from session
                            SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                            int userId = sessionManager.getUserId();
                            
                            navigationService.navigateToMain();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in Google registration process", e);
                    Toast.makeText(RegisterActivity.this, "Registration Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Google Sign-In Error: " + message);
                Toast.makeText(RegisterActivity.this, "Google Sign-In Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRegisterButton() {
        btnRegisterUser.setOnClickListener(v -> {
            // Get input values
            String username = etNewUsername.getText().toString().trim();
            String password = etNewPassword.getText().toString().trim();
            String rewritePassword = etRewritePassword.getText().toString().trim();
            
            // Show loading indicator immediately
            progressBar.setVisibility(View.VISIBLE);
            
            // Move validation to background thread
            executorService.execute(() -> {
                // Validate input in background thread
                final boolean inputValid = validateInput(username, password, rewritePassword);
                
                // Return to UI thread with result
                runOnUiThread(() -> {
                    if (!inputValid) {
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    
                    // Continue with registration in background
                    executorService.execute(() -> {
                        try {
                            final boolean registrationSuccess = authService.registerLocalUser(username, password);
                            final User user = registrationSuccess ? authService.getLocalUser() : null;
                            
                            // Update UI with result
                            runOnUiThread(() -> {
                                if (registrationSuccess && user != null) {
                                    // Firebase sync happens in firebaseSync method
                                    // which handles its own loading indicators
                                    firebaseSync(user);
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    showToast("Registration Failed");
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Registration error: " + e.getMessage(), e);
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                showToast("Registration Error: " + e.getMessage());
                            });
                        }
                    });
                });
            });
        });
    }

    private boolean validateInput(String username, String password, String rewritePassword) {
        // Validate username first
        if (!validateUsername(username)) {
            return false;
        }
        else {
            hideTvUsername();
        }
        // Validate password and rewritePassword
        if (!validatePassword(password, rewritePassword)) {
            return false;
        }
        else {
            hideTvPassword();
            hideTvRewritePassword();
        }
       return true;
    }

    private boolean validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            setTvUsername("Please enter a username.");
            return false;
        }

        if (username.length() < 6 || username.length() > 32) {
            setTvUsername("Username must be between 6 and 32 characters.");
            return false;
        }

        // Check for valid characters (letters, numbers, underscore, hyphen)
        if (!username.matches("^[a-zA-Z0-9_-]*$")) {
            setTvUsername("Username can only contain letters, numbers, underscore and hyphen.");
            return false;
        }

        // Must start with a letter
        if (!username.matches("^[a-zA-Z].*")) {
            setTvUsername("Username must start with a letter.");
            return false;
        }

        // Check for consecutive special characters
        if (username.contains("__") || username.contains("--") || username.contains("-_") || username.contains("_-")) {
            setTvUsername("Username cannot contain consecutive special characters.");
            return false;
        }

        try {
            // Check if username already exists locally
            if (userRepository.isUsernameExists(username)) {
                setTvUsername("Username already exists locally.");
                return false;
            }
            
            // Check if username exists in Firebase
            boolean existsInFirebase = firebaseDataManager.isUsernameExists(username).get();
            if (existsInFirebase) {
                setTvUsername("Username already exists in cloud.");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking username availability", e);
            showToast("Error validating username");
            return false;
        }

        return true;
    }

    private boolean validatePassword(String password, String rewritePassword) {
        if (TextUtils.isEmpty(password)) {
            setTvPassword("Please enter a password.");
            return false;
        }

        // Check for minimum requirements
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?].*");
        boolean hasMinLength = password.length() >= 8;

        // Build error message based on missing requirements
        StringBuilder errorMessage = new StringBuilder("Password must ");
        boolean hasError = false;

        if (!hasMinLength) {
            errorMessage.append("be at least 8 characters");
            hasError = true;
        }

        if (!hasNumber || !hasUpperCase || !hasLowerCase || !hasSpecialChar) {
            if (hasError) {
                errorMessage.append(" and ");
            }
            errorMessage.append("contain ");
            
            boolean isFirst = true;
            if (!hasUpperCase) {
                errorMessage.append("an uppercase letter");
                isFirst = false;
            }
            if (!hasLowerCase) {
                if (!isFirst) errorMessage.append(", ");
                errorMessage.append("a lowercase letter");
                isFirst = false;
            }
            if (!hasNumber) {
                if (!isFirst) errorMessage.append(", ");
                errorMessage.append("a number");
                isFirst = false;
            }
            if (!hasSpecialChar) {
                if (!isFirst) errorMessage.append(", ");
                errorMessage.append("a special character");
            }
            hasError = true;
        }

        if (hasError) {
            setTvPassword(errorMessage + ".");
            return false;
        }
        else {
            hideTvPassword();
        }

        if (!Objects.equals(password, rewritePassword)) {
            setTvRewritePassword();
            return false;
        }
        else {
            hideTvRewritePassword();
        }

        return true;
    }

    private void firebaseSync(User user) {
        // Show progress dialog/indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Syncing account...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // We'll use our updated updateLeaderboard method that handles checking existence
        firebaseDataManager.updateLeaderboard(user.getName(), user.getUserId(), 1, 0)
            .thenAcceptAsync(success -> {
                // This runs in a background thread
                runOnUiThread(() -> {
                    // Dismiss progress dialog first
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    
                    if (success) {
                        Log.d(TAG, "User added to leaderboard successfully");
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        navigationService.navigateToMain();
                    } else {
                        Log.e(TAG, "Failed to add user to leaderboard");
                        // Still continue even if leaderboard sync failed
                        Toast.makeText(this, "Registration successful, but cloud sync failed", 
                                Toast.LENGTH_SHORT).show();
                        navigationService.navigateToMain();
                    }
                });
            })
            .exceptionally(e -> {
                // Handle any exceptions
                Log.e(TAG, "Error during firebase sync: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(this, "Registration successful, but cloud sync failed", 
                            Toast.LENGTH_SHORT).show();
                    navigationService.navigateToMain();
                });
                return null;
            });
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Showing and hiding user input error messages
    private void setTvUsername(String message) {
        tvUsername.setText(message);
        tvUsername.setVisibility(View.VISIBLE);
    }

    private void hideTvUsername() {
        tvUsername.setText("");
        tvUsername.setVisibility(View.GONE);
    }

    private void setTvPassword(String message) {
        tvPassword.setText(message);
        tvPassword.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void setTvRewritePassword() {
        tvRewritePassword.setText("Passwords do not match");
        tvRewritePassword.setVisibility(View.VISIBLE);
    }

    private void hideTvPassword() {
        tvPassword.setText("");
        tvPassword.setVisibility(View.GONE);
    }

    private void hideTvRewritePassword() {
        tvRewritePassword.setText("");
        tvRewritePassword.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
