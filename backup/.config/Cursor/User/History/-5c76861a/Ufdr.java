package com.example.elsa_speak_clone;

import android.app.Application;
import android.util.Log;

import com.example.elsa_speak_clone.database.firebase.FirebaseManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class ElsaSpeakApp extends Application {
    private static final String TAG = "ElsaSpeakApp";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase initialized successfully");
            
            // Initialize FirebaseManager
            FirebaseManager.initialize(this);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }
} 