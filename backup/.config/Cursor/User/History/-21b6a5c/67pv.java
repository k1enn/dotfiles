package com.example.elsa_speak_clone.database.repository;

import android.app.Application;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.dao.UserDao;
import com.example.elsa_speak_clone.database.entity.User;

import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserDao userDao;
    private final AppDatabase database;
    private final SessionManager sessionManager;

    public UserRepository(Application application) {
        database = AppDatabase.getInstance(application);
        userDao = database.userDao();
        sessionManager = new SessionManager(application);
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private int generateUniqueId() {
        Random random = new Random();
        int userId;
        do {
            userId = 10000 + random.nextInt(90000); // Generates a 5-digit random number
        } while (doesUserIdExist(userId));
        
        Log.d(TAG, "Generated ID: " + userId);
        return userId;
    }

    private boolean doesUserIdExist(int userId) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.getUserById(userId));
            return future.get() != null;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error checking if user ID exists", e);
            return false;
        }
    }

    public boolean registerLocalUser(String username, String password) {
        return registerUser(username, password, false);
    }

    public boolean registerGoogleUser(String email) {
        return registerUser(email, "", true);
    }

    public boolean registerUser(String username, String password, boolean isGoogleUser) {
        try {
            if (username == null) return false;

            // Check if username/email already exists
            Future<Integer> future;
            if (isGoogleUser) {
                future = AppDatabase.databaseWriteExecutor.submit(() -> 
                    userDao.checkEmailExists(username));
            } else {
                future = AppDatabase.databaseWriteExecutor.submit(() -> 
                    userDao.checkUsernameExists(username));
            }

            if (future.get() > 0) {
                Log.d(TAG, "Username/Email already exists: " + username);
                return false;
            }

            // Create new user
            int userId = generateUniqueId();
            User user;

            if (isGoogleUser) {
                // Google authentication
                String displayName = username.split("@")[0]; // Extract username from email
                user = new User(userId, username, displayName, "", 1, getCurrentDate());
            } else {
                // Local authentication
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                user = new User(userId, "", username, hashedPassword, 0, getCurrentDate());
            }

            // Insert user and create session
            Future<Long> insertFuture = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.insert(user));
            
            long result = insertFuture.get();
            if (result != -1) {
                if (isGoogleUser) {
                    sessionManager.createGoogleSession(user.getName(), userId);
                } else {
                    sessionManager.createSession(username, userId);
                }
                return true;
            } else {
                Log.e(TAG, "Failed to insert user into database");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering user: " + e.getMessage(), e);
            return false;
        }

    }

    public boolean authenticateLocalUser(String username, String password) {
        return authenticateUser(username, password, false);
    

    public boolean authenticateGoogleUser(String email) {
        return authenticateUser(email, "", true);
    }

    public boolean authenticateUser(String usernameOrEmail, String password, boolean isGoogleAuth) {
        try {
            if (usernameOrEmail == null || usernameOrEmail.isEmpty()) {
                return false;
            }

            // For local auth, password cannot be empty
            if (!isGoogleAuth && (password == null || password.isEmpty())) {
                return false;
            }

            // Get user from database
            Callable<User> getUserTask;
            if (isGoogleAuth) {
                getUserTask = () -> userDao.getGoogleUserByEmail(usernameOrEmail);
            } else {
                getUserTask = () -> userDao.getLocalUserByUsername(usernameOrEmail);
            }

            Future<User> future = AppDatabase.databaseWriteExecutor.submit(getUserTask);
            User user = future.get();

            if (user != null) {
                if (isGoogleAuth) {
                    // For Google auth, just check if user exists and is a Google user
                    if (user.getIsGoogleUser() == 1) {
                        sessionManager.createGoogleSession(user.getName(), user.getUserId());
                        return true;
                    } else {
                        Log.d(TAG, "Attempt to use Google auth for non-Google account");
                        return false;
                    }
                } else {
                    // For local auth, check password
                    try {
                        boolean passwordValidate = BCrypt.checkpw(password, user.getPassword());
                        if (passwordValidate) {
                            sessionManager.createSession(user.getName(), user.getUserId());
                            return true;
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Cannot authenticate password", e);
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage(), e);
            return false;
        }
    }

    // Add other methods you need from the original class
} 