package com.example.elsa_speak_clone;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

public class ElsaSpeakApplication extends Application {
    private static final String TAG = "ElsaSpeakApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase persistence before any other Firebase usage
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase persistence enabled successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error enabling Firebase persistence: " + e.getMessage(), e);
        }
    }
} 