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
import com.example.elsa_speak_clone.database.dao.UserDao;
import com.example.elsa_speak_clone.database.repositories.UserRepository;
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
        loading = findViewById(R.id.loading);
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
        // Hiển thị loading và vô hiệu hóa nút đăng nhập
        loading.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        
        // Bước 1: Kiểm tra xem user có tồn tại trên Firebase không
        firebaseDataManager.isUsernameExistsInUserTable(username)
            .thenAccept(existsInFirebase -> {
                if (existsInFirebase) {
                    // Bước 2: Nếu user tồn tại trên Firebase, xác thực với Firebase
                    Log.d(TAG, "User tồn tại trên Firebase, đang xác thực...");
                    
                    firebaseDataManager.authenticateUser(username, password)
                        .thenAccept(firebaseUser -> {
                            if (firebaseUser != null) {
                                // Bước 3: Xác thực Firebase thành công
                                Log.d(TAG, "Xác thực Firebase thành công cho: " + username);
                                
                                // Tạo session
                                sessionManager.createSession(firebaseUser.getName(), firebaseUser.getUserId());
                                
                                // Tải dữ liệu từ Firebase về local
                                firebaseDataManager.pullUserProgressToLocal(username, firebaseUser.getUserId())
                                    .thenAccept(syncSuccess -> {
                                        Log.d(TAG, "Đồng bộ dữ liệu từ Firebase: " + (syncSuccess ? "thành công" : "thất bại"));
                                        
                                        // Cập nhật UI trên main thread
                                        runOnUiThread(() -> {
                                            loading.setVisibility(View.GONE);
                                            btnLogin.setEnabled(true);
                                            
                                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                            
                                            // Chuyển đến màn hình chính
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        });
                                    });
                            } else {
                                // Xác thực Firebase thất bại, thử xác thực local
                                Log.d(TAG, "Xác thực Firebase thất bại, đang thử xác thực local...");
                                authenticateLocally(username, password);
                            }
                        });
                } else {
                    // User không tồn tại trên Firebase, thử xác thực local
                    Log.d(TAG, "User không tồn tại trên Firebase, đang thử xác thực local...");
                    authenticateLocally(username, password);
                }
            })
            .exceptionally(e -> {
                // Xử lý lỗi khi kiểm tra Firebase
                Log.e(TAG, "Lỗi khi kiểm tra Firebase: " + e.getMessage());
                authenticateLocally(username, password);
                return null;
            });
    }

    // Phương thức hỗ trợ để xác thực local
    private void authenticateLocally(String username, String password) {
        // Thực hiện xác thực trên background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean success = false;
            User user = null;
            
            try {
                success = authService.authenticateLocalUser(username, password);
                if (success) {
                    UserRepository userRepository = new UserRepository(getApplication());
                    user = userRepository.getUserByName(username);
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi xác thực: " + e.getMessage(), e);
            }
            
            final boolean loginSuccess = success;
            final User finalUser = user;
            
            // Cập nhật UI trên main thread
            runOnUiThread(() -> {
                loading.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                
                if (loginSuccess && finalUser != null) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    
                    // Tạo session
                    sessionManager.createSession(finalUser.getName(), finalUser.getUserId());
                    
                    // Chuyển đến màn hình chính
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Thông tin đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
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
            firebaseDataManager.pullUserProgressToLocal(user.getName(), user.getUserId())
                    .thenAccept(success -> {
                        if (success) {
                            Log.d(TAG, "Successfully pulled user progress from Firebase");
                        } else {
                            Log.d(TAG, "No progress found in Firebase or pull failed");
                        }
                    });
        }
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
