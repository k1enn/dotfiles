package com.example.elsa_speak_clone.database.repositories;

import android.content.Context;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.UserDao;
import com.example.elsa_speak_clone.database.entities.User;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository class for handling User data operations
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserDao userDao;
    private final AppDatabase database;

    public UserRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.userDao = database.userDao();
    }

    /**
     * Insert a new user
     * @param user The user to insert
     * @return The inserted user's ID, or -1 if insertion failed
     */
    public long insertUser(User user) {
        try {
            Future<Long> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.insert(user));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error inserting user", e);
            return -1;
        }
    }

    /**
     * Update an existing user
     * @param user The user to update
     * @return true if update was successful
     */
    public boolean updateUser(User user) {
        try {
            AppDatabase.databaseWriteExecutor.execute(() -> 
                userDao.update(user));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user", e);
            return false;
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
            return future.get();
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
            return future.get();
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
} 