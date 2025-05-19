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
import android.widget.ProgressBar;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.adapters.ValidationErrorAdapter;
import com.example.elsa_speak_clone.database.GoogleSignInHelper;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.repositories.UserRepository;
import com.example.elsa_speak_clone.services.AuthenticationService;
import com.example.elsa_speak_clone.services.NavigationService;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
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
    private ProgressBar progressBar;
    
    // Services
    private UserRepository userRepository;
    private AuthenticationService authService;
    private NavigationService navigationService;
    private GoogleSignInHelper googleSignInHelper;
    private FirebaseDataManager firebaseDataManager;
    private SessionManager sessionManager;

    // Add this as a class field
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        sessionManager = new SessionManager(this);
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
                        // Show appropriate error messages
                        progressBar.setVisibility(View.GONE);
                        displayValidationErrors(username, password, rewritePassword);
                        return;
                    }
                    
                    // Continue with improved registration process
                    registerUser(username, password);
                });
            });
        });
    }

    private boolean validateInput(String username, String password, String rewritePassword) {
        // Create a list to store validation errors
        final List<ValidationErrorAdapter> errors = new ArrayList<>();
        
        // Validate username
        String usernameError = validateUsernameLogic(username);
        if (usernameError != null) {
            errors.add(new ValidationErrorAdapter("username", usernameError));
            return false;
        }
        
        // Validate password
        String passwordError = validatePasswordLogic(password);
        if (passwordError != null) {
            errors.add(new ValidationErrorAdapter("password", passwordError));
            return false;
        }
        
        // Validate password match
        if (!Objects.equals(password, rewritePassword)) {
            errors.add(new ValidationErrorAdapter("rewritePassword", "Passwords do not match"));
            return false;
        }
       
        // If we get here, validation succeeded
        return true;
    }

    // Non-UI validation logic for username
    private String validateUsernameLogic(String username) {
        if (TextUtils.isEmpty(username)) {
            return "Please enter a username.";
        }

        if (username.length() < 6 || username.length() > 32) {
            return "Username must be between 6 and 32 characters.";
        }

        // Check for valid characters (letters, numbers, underscore, hyphen)
        if (!username.matches("^[a-zA-Z0-9_-]*$")) {
            return "Username can only contain letters, numbers, underscore and hyphen.";
        }

        // Must start with a letter
        if (!username.matches("^[a-zA-Z].*")) {
            return "Username must start with a letter.";
        }

        // Check for consecutive special characters
        if (username.contains("__") || username.contains("--") || username.contains("-_") || username.contains("_-")) {
            return "Username cannot contain consecutive special characters.";
        }

        try {
            // Check if username already exists locally
            if (userRepository.isUsernameExists(username)) {
                return "Username already exists locally.";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking username availability", e);
            return "Error validating username";
        }

        return null; // Null means no error
    }

    // Non-UI validation logic for password
    private String validatePasswordLogic(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Please enter a password.";
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
            return errorMessage.toString() + ".";
        }

        return null; // Null means no error
    }

    // Helper class to store validation errors


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

    private void registerUser(String username, String password) {
        // Already on UI thread, move to background for database operations
        executorService.execute(() -> {
            try {
                // First register locally
                final boolean localSuccess = authService.registerLocalUser(username, password);
                final User user = localSuccess ? authService.getLocalUser() : null;
                
                if (localSuccess && user != null) {
                    // Now register in Firebase
                    firebaseDataManager.registerUserInFirebase(username, password, user.getUserId())
                        .thenAcceptAsync(success -> {
                            // Return to UI thread to show results
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                
                                if (success) {
                                    // Complete success - local and Firebase
                                    Log.d(TAG, "Registration fully successful for " + username);
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                    
                                    // Create session
                                    sessionManager.createLoginSession(user.getUserId(), user.getName());
                                    navigationService.navigateToMain();
                                } else {
                                    // Only local success, Firebase failed
                                    Log.w(TAG, "Local registration successful but Firebase failed for " + username);
                                    Toast.makeText(this, "Registration partially successful. Cloud sync failed.", 
                                            Toast.LENGTH_SHORT).show();
                                    sessionManager.createLoginSession(user.getUserId(), user.getName());
                                    navigationService.navigateToMain();
                                }
                            });
                        })
                        .exceptionally(e -> {
                            Log.e(TAG, "Error during Firebase registration: " + e.getMessage(), e);
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Registration successful, but cloud sync failed", 
                                        Toast.LENGTH_SHORT).show();
                                sessionManager.createLoginSession(user.getUserId(), user.getName());
                                navigationService.navigateToMain();
                            });
                            return null;
                        });
                } else {
                    // Local registration failed
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Registration error: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Registration Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayValidationErrors(String username, String password, String rewritePassword) {
        if (validateUsernameLogic(username) != null) {
            setTvUsername(validateUsernameLogic(username));
        } else {
            hideTvUsername();
        }
        
        if (validatePasswordLogic(password) != null) {
            setTvPassword(validatePasswordLogic(password));
        } else {
            hideTvPassword();
        }
        
        if (!Objects.equals(password, rewritePassword)) {
            setTvRewritePassword();
        } else {
            hideTvRewritePassword();
        }
    }
}

            setTvRewritePassword();
        } else {
            hideTvRewritePassword();
        }
    }
}
