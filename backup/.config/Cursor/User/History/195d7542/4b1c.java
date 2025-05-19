package com.example.elsa_speak_clone.database.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.UserDao;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.firebase.FirebaseDbHelper;

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
     * Find or create user by email, ensuring consistent user IDs
     */
    public User findOrCreateUserByEmail(String email, boolean isGoogle) {
        try {
            // First check if user exists in local database
            User existingUser = userDao.getUserByEmail(email);
            
            if (existingUser != null) {
                Log.d(TAG, "Found existing user with email: " + email);
                return existingUser;
            }
            
            // If not in local database, check Firebase first before creating new
            FirebaseDbHelper firebaseHelper = new FirebaseDbHelper();
            User firebaseUser = firebaseHelper.getUserByEmail(email).get(); // Using blocking get for simplicity
            
            if (firebaseUser != null) {
                // User exists in Firebase but not locally - use their Firebase ID
                Log.d(TAG, "Found user in Firebase with ID: " + firebaseUser.getUserId());
                userDao.insertUser(firebaseUser);
                return firebaseUser;
            }
            
            // Create new user with next available ID
            int newUserId = getNextUserId();
            User newUser = new User(newUserId, email, email.split("@")[0], 
                                   "", isGoogle, getCurrentDate());
            userDao.insertUser(newUser);
            
            return newUser;
        } catch (Exception e) {
            Log.e(TAG, "Error finding/creating user: " + e.getMessage(), e);
            return null;
        }
    }
}