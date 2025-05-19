
package com.example.elsa_speak_clone.activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.FirebaseSyncManager;
import com.example.elsa_speak_clone.database.GoogleSignInHelper;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.fragments.HomeFragment;
import com.example.elsa_speak_clone.fragments.LearnFragment;
import com.example.elsa_speak_clone.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    SharedPreferences prefs;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private ImageButton dictionary;
    GoogleSignInHelper googleSignInHelper;
    private FirebaseSyncManager syncManager;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariable();
        setupNavigationMenu();
        initializeSharedPreferences();
        checkUserLogin();
        setupDictionaryButton();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        loadFragment(new HomeFragment());
        firebaseSync();

    }

    private void firebaseSync() {
        try {
            sessionManager = new SessionManager(this);
            syncManager = new FirebaseSyncManager(this);

            // Setup offline capability
            syncManager.setupOfflineCapability();

            // Get current user ID
            HashMap<String, String> userDetails = sessionManager.getUserDetails();
            int userId = Integer.parseInt(userDetails.get(SessionManager.KEY_USER_ID));

            // Sync local data to Firebase
            syncManager.syncUserData(userId);
            syncManager.syncUserProgress(userId);

            // Listen for remote changes
            syncManager.listenForRemoteChanges(userId);
        } catch (Exception e) {
            Log.d("MainActivity", "Can not sync with firebase");
        }

    }
    private void initializeSharedPreferences() {
        String username = getIntent().getStringExtra("username");
        int userId = getIntent().getIntExtra("userId", -1);

        if (username != null && userId != -1) {
            // Save to SharedPreferences here
            prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("username", username).putInt("userId", userId).apply();
        }

    }

    private void checkUserLogin() {

        // Check if user is already logged in
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || sessionManager.isGoogleUser() && !googleSignInHelper.CheckGoogleLoginState() ) {
            // User is not logged in, go to login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }
    private void initializeVariable() {
        googleSignInHelper = new GoogleSignInHelper(MainActivity.this, new GoogleSignInHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {

            }


            @Override
            public void onError(String message) {

            }
        });
        dictionary = findViewById(R.id.btnDictionary);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navListener);
        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set up the ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    private void setupNavigationMenu() {
        // Set up navigation item selection listener
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle navigation view item clicks here
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Handle home action
            } else if (id == R.id.nav_learn) {
                // Handle learn action
            } else if (id == R.id.nav_discover) {
                // Handle discover action
            } else if (id == R.id.nav_leaderboard) {
                // Handle leaderboard action
            } else if (id == R.id.nav_account) {
                // Handle profile action
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            // Show a confirmation dialog
            finishAffinity(); // Close all activities
            System.exit(0); // Exit the app
        }
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_learn) {
            selectedFragment = new LearnFragment();
        } else if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else {
            return false;
        }

        loadFragment(selectedFragment);
        return true;
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commit();
    }
    private void navigateToDictionary() {
        Intent intent = new Intent(MainActivity.this, DictionaryActivity.class);
        startActivity(intent);
    }

    private void setupDictionaryButton() {
        dictionary.setOnClickListener(v -> {
            navigateToDictionary();
        });
    }
}
