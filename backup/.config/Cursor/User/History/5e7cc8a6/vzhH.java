package com.example.elsa_speak_clone.services;

import android.content.Context;
import android.util.Log;

import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.repositories.UserRepository;
import com.example.elsa_speak_clone.database.room.entities.User;

import org.mindrot.jbcrypt.BCrypt;

public class AuthenticationService {
    private static final String TAG = "AuthenticationService";
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthenticationService(Context context) {
        this.userRepository = new UserRepository(context);
        this.sessionManager = new SessionManager(context);
    }

    public boolean authenticateLocalUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        try {
            User user = userRepository.getUserByUsername(username);
            if (user != null && BCrypt.checkpw(password, user.getPassword())) {
                sessionManager.createSession(user.getName(), user.getUserId());
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating local user", e);
            return false;
        }
    }

    public boolean authenticateGoogleUser(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        try {
            User user = userRepository.getUserByEmail(email);
            if (user != null && user.isGoogleUser()) {
                sessionManager.createGoogleSession(user.getName(), user.getUserId());
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating Google user", e);
            return false;
        }
    }

    public int registerLocalUser(String username, String password) {
        int userId = userRepository.registerLocalUser(username, password);
        if (userId != -1) {
            sessionManager.createSession(username, userId);
        }
        return userId;
    }

    public int registerGoogleUser(String email) {
        int userId = userRepository.registerGoogleUser(email);
        if (userId != -1) {
            String username = email.split("@")[0];
            sessionManager.createGoogleSession(username, userId);
        }
        return userId;
    }
}