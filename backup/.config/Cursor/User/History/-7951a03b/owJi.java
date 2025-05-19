package com.example.elsa_speak_clone.utilities;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for secure password operations
 */
public class PasswordUtils {
    private static final String TAG = "PasswordUtils";
    
    /**
     * Hash a password using SHA-256
     * Note: In a production app, use a proper password hashing library with salt
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password: " + e.getMessage(), e);
            // Fallback - not recommended in production!
            return password;
        }
    }
    
    /**
     * Verify if a password matches its hash
     */
    public static boolean verifyPassword(String password, String storedHash) {
        String hashOfInput = hashPassword(password);
        return hashOfInput.equals(storedHash);
    }
} 