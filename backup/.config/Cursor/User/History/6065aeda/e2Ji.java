package com.example.elsa_speak_clone.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.activities.DictionaryActivity;
import com.example.elsa_speak_clone.activities.LoginActivity;
import com.example.elsa_speak_clone.activities.MainActivity;
import com.example.elsa_speak_clone.activities.QuizActivity;
import com.example.elsa_speak_clone.activities.RegisterActivity;
import com.example.elsa_speak_clone.activities.SpeechToText;
import com.example.elsa_speak_clone.fragments.HomeFragment;
import com.example.elsa_speak_clone.fragments.LearnFragment;
import com.example.elsa_speak_clone.fragments.ProfileFragment;

/**
 * Service class to handle all navigation within the app
 */
public class NavigationService {
    private static final String TAG = "NavigationService";
    private final Context context;

    public NavigationService(Context context) {
        this.context = context;
    }

    /**
     * Navigate to MainActivity
     */
    public void navigateToMain() {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to MainActivity", e);
        }
    }

    /**
     * Navigate to LoginActivity
     */
    public void navigateToLogin() {
        try {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to LoginActivity", e);
        }
    }

    /**
     * Navigate to RegisterActivity
     */
    public void navigateToRegister() {
        try {
            Intent intent = new Intent(context, RegisterActivity.class);
            context.startActivity(intent);
            
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to RegisterActivity", e);
        }
    }

    /**
     * Navigate to SpeechToText activity
     */
    public void navigateToSpeechToText() {
        try {
            Intent intent = new Intent(context, SpeechToText.class);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to SpeechToText", e);
        }
    }

    /**
     * Navigate to Dictionary activity
     */
    public void navigateToDictionary() {
        try {
            Intent intent = new Intent(context, DictionaryActivity.class);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Dictionary", e);
        }
    }

    /**
     * Navigate to Quiz activity
     * @param lessonId The ID of the lesson for the quiz
     */
    public void navigateToQuiz(int lessonId) {
        try {
            Intent intent = new Intent(context, QuizActivity.class);
            intent.putExtra("LESSON_ID", lessonId);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Quiz", e);
        }
    }

    /**
     * Load a fragment into the fragment container
     * @param activity The activity containing the fragment container
     * @param fragment The fragment to load
     * @param addToBackStack Whether to add the transaction to the back stack
     */
    public void loadFragment(FragmentActivity activity, Fragment fragment, boolean addToBackStack) {
        try {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            
            if (addToBackStack) {
                transaction.addToBackStack(null);
            }
            
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment", e);
        }
    }

    /**
     * Navigate to Home fragment
     * @param activity The activity containing the fragment container
     */
    public void navigateToHomeFragment(FragmentActivity activity) {
        loadFragment(activity, new HomeFragment(), false);
    }

    /**
     * Navigate to Learn fragment
     * @param activity The activity containing the fragment container
     */
    public void navigateToLearnFragment(FragmentActivity activity) {
        loadFragment(activity, new LearnFragment(), false);
    }

    /**
     * Navigate to Profile fragment
     * @param activity The activity containing the fragment container
     */
    public void navigateToProfileFragment(FragmentActivity activity) {
        loadFragment(activity, new ProfileFragment(), false);
    }

    /**
     * Navigate to a specific activity with extras
     * @param activityClass The class of the activity to navigate to
     * @param extras Bundle of extras to pass to the activity
     * @param flags Intent flags to apply
     */
    public void navigateToActivity(Class<?> activityClass, Bundle extras, int... flags) {
        try {
            Intent intent = new Intent(context, activityClass);
            
            if (extras != null) {
                intent.putExtras(extras);
            }
            
            for (int flag : flags) {
                intent.addFlags(flag);
            }
            
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to " + activityClass.getSimpleName(), e);
        }
    }
}