package com.example.elsa_speak_clone;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

public class ElsaSpeakApp extends Application {
    private static final String TAG = "ElsaSpeakApp";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase and enable persistence
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase persistence enabled");
        } catch (Exception e) {
            Log.e(TAG, "Error enabling Firebase persistence: " + e.getMessage(), e);
        }
        
    }
} 