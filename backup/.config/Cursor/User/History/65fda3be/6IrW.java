package com.example.elsa_speak_clone.services;

import android.content.Context;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.dao.UserDao;
import com.example.elsa_speak_clone.database.entities.User;

import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AuthenticationService {
    private static final String TAG = "AuthenticationService";
    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final AppDatabase database;

    public AuthenticationService(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.userDao = database.userDao();
        this.sessionManager = new SessionManager(context);
    }

    /**
     * Register a local user with username and password
     * @param username The username
     * @param password The password
     * @return True if registration was successful
     */
    public boolean registerLocalUser(String username, String password) {
        return registerUser(username, password, false);
    }

    /**
     * Register a Google user with email and display name
     * @param email The Google email
     * @param displayName The Google account display name
     * @return True if registration was successful
     */
    public boolean registerGoogleUser(String email, String displayName) {
        return registerUser(email, "", displayName, true);
    }

    /**
     * Register a user with the appropriate authentication type
     * @param username The username or email
     * @param password The password (empty for Google users)
     * @param displayName The display name (for Google users)
     * @param isGoogleUser Whether this is a Google user
     * @return True if registration was successful
     */
    public boolean registerUser(String username, String password, String displayName, boolean isGoogleUser) {
        try {
            if (username == null || username.isEmpty()) {
                Log.e(TAG, "Cannot register with empty username");
                return false;
            }

            // For local auth, password cannot be empty
            if (!isGoogleUser && (password == null || password.isEmpty())) {
                Log.e(TAG, "Cannot register local user with empty password");
                return false;
            }

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
                // Google authentication - use provided display name or fallback to email username
                String finalDisplayName = displayName;
                if (finalDisplayName == null || finalDisplayName.isEmpty()) {
                    finalDisplayName = username.split("@")[0]; // Extract username from email as fallback
                }
                user = new User(userId, username, finalDisplayName, "", true, getCurrentDate());
                Log.d(TAG, "Creating Google user with display name: " + finalDisplayName);
            } else {
                // Local authentication
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                user = new User(userId, "", username, hashedPassword, false, getCurrentDate());
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

    /**
     * Authenticate a local user with username and password
     * @param username The username
     * @param password The password
     * @return True if authentication was successful
     */
    public boolean authenticateLocalUser(String username, String password) {
        return authenticateUser(username, password, false);
    }

    /**
     * Authenticate a Google user with email
     * @param email The Google email
     * @return True if authentication was successful
     */
    public boolean authenticateGoogleUser(String email) {
        return authenticateUser(email, "", true);
    }

    /**
     * Authenticate a user with the appropriate authentication type
     * @param usernameOrEmail The username or email
     * @param password The password (ignored for Google users)
     * @param isGoogleAuth Whether this is Google authentication
     * @return True if authentication was successful
     */
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
                    if (user.getGoogle()) {
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
                        Log.e(TAG, "Cannot authenticate password", e);
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage(), e);
            return false;
        }
    }


    public int getCurrentUserId() {
        return sessionManager.getUserId();
    }

    /**
     * Get the current user's username
     * @return The username or empty string if not logged in
     */
    public String getCurrentUsername() {
        return sessionManager.getUserDetails().get(SessionManager.KEY_USERNAME);
    }

    /**
     * Check if the current user is a Google user
     * @return True if the user is authenticated via Google
     */
    public boolean isGoogleUser() {
        return sessionManager.isGoogleUser();
    }

    /**
     * Generate a unique user ID
     * @return A unique user ID
     */
    private int generateUniqueId() {
        Random random = new Random();
        int userId;
        do {
            userId = 10000 + random.nextInt(90000); // Generates a 5-digit random number
        } while (doesUserIdExist(userId));
        
        Log.d(TAG, "Generated ID: " + userId);
        return userId;
    }

    /**
     * Check if a user ID already exists
     * @param userId The user ID to check
     * @return True if the user ID exists
     */
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

    /**
     * Get the current date as a string
     * @return The current date in yyyy-MM-dd format
     */
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}