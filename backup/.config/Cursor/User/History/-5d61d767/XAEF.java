package com.example.elsa_speak_clone.database;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.elsa_speak_clone.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";
    private final FirebaseAuth mAuth;
    private final GoogleSignInClient mGoogleSignInClient;
    private final Activity activity;
    private final AuthCallback authCallback;
    public static final int RC_SIGN_IN = 9001;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String message);
    }

    public FirebaseAuth MAuth() {
        return mAuth;
    }

    public GoogleSignInHelper(Activity activity, AuthCallback callback) {
        this.activity = activity;
        this.authCallback = callback;
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    // It will return true if have an account already signed in
    public boolean CheckGoogleLoginState() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.activity);
        return account != null; // Return true if account exists, false otherwise
    }
    
    // Check if device has active network connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? 
                connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    // Modified sign-in method with network check
    public void signIn() {
        // First check network availability
        if (!isNetworkAvailable()) {
            // No network connection
            authCallback.onError("No internet connection. Please check your network and try again.");
            return;
        }
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(activity, gso);
        
        // Force account selection even if already signed in
        client.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = client.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }


    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Log the specific error code to diagnose the issue
                Log.e(TAG, "Sign-in failed with error code: " + e.getStatusCode());
                
                // Provide more user-friendly error messages based on error code
                String errorMessage;
                switch (e.getStatusCode()) {
                    case 7:
                    case 8:
                        errorMessage = "Network error. Please check your internet connection and try again.";
                        break;
                    case 12501: // User cancelled
                        errorMessage = "Sign-in cancelled.";
                        break;
                    case 10:
                        errorMessage = "Developer error: Make sure the SHA-1 fingerprint is correctly configured.";
                        Log.e(TAG, "Check that the SHA-1 fingerprint in Firebase console matches your app");
                        break;
                    case 5:
                        errorMessage = "Sign-in request invalid. Please try again.";
                        break;
                    default:
                        errorMessage = "Google sign in failed with code: " + e.getStatusCode();
                }
                
                // Send the error message to the callback
                authCallback.onError(errorMessage);
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        // Check for network again before Firebase authentication
        if (!isNetworkAvailable()) {
            authCallback.onError("Network connection lost. Please check your internet and try again.");
            return;
        }
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        authCallback.onSuccess(user);
                    } else {
                        String errorMessage = "Authentication failed";
                        if (task.getException() != null) {
                            Log.e(TAG, "Firebase auth error: ", task.getException());
                            
                            // Better error message
                            if (task.getException().getMessage().contains("network")) {
                                errorMessage = "Network error during authentication. Please check your connection.";
                            } else {
                                errorMessage = "Authentication failed: " + task.getException().getMessage();
                            }
                        }
                        authCallback.onError(errorMessage);
                    }
                });
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        
        // Only attempt to sign out of Google if network is available
        if (isNetworkAvailable()) {
            mGoogleSignInClient.signOut().addOnCompleteListener(activity,
                    task -> Toast.makeText(activity, "Signed out successfully", Toast.LENGTH_SHORT).show());
        } else {
            // Just show confirmation even if Google sign-out might not have fully completed
            Toast.makeText(activity, "Signed out locally", Toast.LENGTH_SHORT).show();
        }
    }
}
