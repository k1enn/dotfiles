package com.example.elsa_speak_clone.database.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.UserDao;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.firebase.FirebaseManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository class for handling User data operations
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserDao userDao;
    private final AppDatabase database;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public UserRepository(Application application) {
        this.database = AppDatabase.getInstance(application);
        this.userDao = database.userDao();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    /**
     * Insert a new user
     * @param user The user to insert
     * @return The inserted user's ID, or -1 if insertion failed
     */
    public long insertUser(User user) {
        try {
            // Set current date for new user
            String currentDate = getCurrentDate();

            // Insert into local database
            Future<Long> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userDao.insert(user));
            long userId = future.get();

            // Set the user ID and sync to Firebase
            if (userId > 0) {
                user.setUserId((int) userId);
                currentUser.postValue(user);
            }

            return userId;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error inserting user", e);
            return -1;
        }
    }


    /**
     * Delete a user
     * @param user The user to delete
     * @return true if deletion was successful
     */
    public boolean deleteUser(User user) {
        try {
            AppDatabase.databaseWriteExecutor.execute(() -> 
                userDao.delete(user));
            currentUser.postValue(null);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user", e);
            return false;
        }
    }

    /**
     * Get a user by ID
     * @param userId The user ID
     * @return The user or null if not found
     */
    public User getUserById(int userId) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.getUserById(userId));
            User user = future.get();
            currentUser.postValue(user);
            return user;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting user by ID", e);
            return null;
        }
    }

    /**
     * Get a user by email
     * @param email The email to search for
     * @return The user or null if not found
     */
    public User getUserByEmail(String email) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.getUserByEmail(email));
            User user = future.get();
            currentUser.postValue(user);
            return user;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting user by email", e);
            return null;
        }
    }

    /**
     * Get a user by username
     * @param username The username to search for
     * @return The user or null if not found
     */
    public User getUserByUsername(String username) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.getUserByUsername(username));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting user by username", e);
            return null;
        }
    }

    /**
     * Get a local (non-Google) user by username
     * @param username The username to search for
     * @return The user or null if not found
     */
    public User getLocalUserByUsername(String username) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.getLocalUserByUsername(username));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting local user by username", e);
            return null;
        }
    }

    /**
     * Get a Google user by email
     * @param email The email to search for
     * @return The user or null if not found
     */
    public User getGoogleUserByEmail(String email) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.getGoogleUserByEmail(email));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting Google user by email", e);
            return null;
        }
    }

    /**
     * Check if a username already exists
     * @param username The username to check
     * @return true if the username exists
     */
    public boolean isUsernameExists(String username) {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.checkUsernameExists(username));
            return future.get() > 0;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error checking if username exists", e);
            return false;
        }
    }

    /**
     * Check if an email already exists
     * @param email The email to check
     * @return true if the email exists
     */
    public boolean isEmailExists(String email) {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.checkEmailExists(email));
            return future.get() > 0;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error checking if email exists", e);
            return false;
        }
    }

    /**
     * Delete a user by ID
     * @param userId The user ID to delete
     * @return true if deletion was successful
     */
    public boolean deleteUserById(int userId) {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.deleteUserById(userId));
            return future.get() > 0;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error deleting user by ID", e);
            return false;
        }
    }


    /**
     * Check if username exists in local database
     */
    public boolean isUsernameRegistered(String username) {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userDao.checkUsernameExists(username));
            return future.get() > 0;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error checking if username exists", e);
            return false;
        }
    }

    /**
     * Find user by username in local database
     */
    public User findUserByUsername(String username) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userDao.getUserByUsername(username));
            User user = future.get();
            if (user != null) {
                currentUser.postValue(user);
            }
            return user;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error finding user by username", e);
            return null;
        }
    }

    /**
     * Get current date as formatted string
     */
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    /**
     * Get user by email or username
     */
    public User getUserByEmailOrUsername(String usernameOrEmail) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userDao.getUserByEmailOrUsername(usernameOrEmail));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting user by email or username", e);
            return null;
        }
    }

    /**
     * Verify user password
     */
    public boolean verifyPassword(User user, String password) {
        try {
            // Use a secure password verification method (like BCrypt)
            // For simplicity, we'll just compare the values directly
            // In a real app, use a proper password hashing library
            return user.getPassword().equals(hashPassword(password));
        } catch (Exception e) {
            Log.e(TAG, "Error verifying password", e);
            return false;
        }
    }

    /**
     * Hash a password (simplified - use a proper library in production)
     */
    private String hashPassword(String password) {
        // In a real app, use a proper password hashing library like BCrypt
        // This is just a simplified example
        return password;
    }

    /**
     * Sync a user with Firebase
     */
    public boolean syncWithFirebase(User user) {
        try {
            FirebaseManager manager = FirebaseManager.getInstance();
            return manager.saveUser(user).get();
        } catch (Exception e) {
            Log.e(TAG, "Error syncing user with Firebase", e);
            return false;
        }
    }
}