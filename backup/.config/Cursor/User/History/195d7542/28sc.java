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
    private final FirebaseDbHelper firebaseDb;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public UserRepository(Application application) {
        this.database = AppDatabase.getInstance(application);
        this.userDao = database.userDao();
        this.firebaseDb = FirebaseDbHelper.getInstance();
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
            user.setDateCreated(currentDate);
            user.setLastLogin(currentDate);

            // Insert into local database
            Future<Long> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userDao.insert(user));
            long userId = future.get();

            // Set the user ID and sync to Firebase
            if (userId > 0) {
                user.setUserId((int) userId);
                currentUser.postValue(user);
                
                // Sync to Firebase in background
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    firebaseDb.saveUser(user);
                });
            }

            return userId;
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
            // Update last login date
            user.setLastLogin(getCurrentDate());

            // Update in local database
            AppDatabase.databaseWriteExecutor.submit(() ->
                    userDao.update(user)).get();
            currentUser.postValue(user);
            
            // Sync to Firebase in background
            AppDatabase.databaseWriteExecutor.execute(() -> {
                firebaseDb.saveUser(user);
            });
            
            return true;
        } catch (ExecutionException | InterruptedException e) {
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
     * Authenticate user by username and password
     */
    public User authenticateUser(String usernameOrEmail, String password) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userDao.getUserByEmailOrUsername(usernameOrEmail));
            User user = future.get();
            
            if (user != null) {
                // Update last login time and sync to Firebase
                user.setLastLogin(getCurrentDate());
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    userDao.update(user);
                    firebaseDb.saveUser(user);
                });
                
                currentUser.postValue(user);
            }
            
            return user;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error authenticating user", e);
            return null;
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
     * Sync user with Firebase - pulls user data from Firebase if it exists
     */
    public boolean syncUserWithFirebase(String email) {
        try {
            // Check if user exists in Firebase
            User firebaseUser = firebaseDb.getUserByEmail(email).get();
            
            if (firebaseUser != null) {
                // Check if user exists locally
                User localUser = getUserByEmail(email);
                
                if (localUser == null) {
                    // User exists in Firebase but not locally, insert into local DB
                    insertUser(firebaseUser);
                    return true;
                } else {
                    // User exists in both, compare last login dates and use the most recent data
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date localDate = format.parse(localUser.getLastLogin());
                    Date firebaseDate = format.parse(firebaseUser.getLastLogin());
                    
                    if (firebaseDate.after(localDate)) {
                        // Firebase data is newer, update local
                        updateUser(firebaseUser);
                        return true;
                    }
                }
            } else {
                // User doesn't exist in Firebase, but might exist locally
                User localUser = getUserByEmail(email);
                if (localUser != null) {
                    // Sync local user to Firebase
                    firebaseDb.saveUser(localUser).get();
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error syncing user with Firebase", e);
            return false;
        }
    }
} 