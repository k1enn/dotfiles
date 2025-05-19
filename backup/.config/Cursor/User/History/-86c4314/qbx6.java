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
import com.example.elsa_speak_clone.services.NavigationService;
import com.example.elsa_speak_clone.services.UserService;
import com.example.elsa_speak_clone.services.SyncService;
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

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    // UI components
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private ImageButton dictionary;
    
    // Services
    private NavigationService navigationService;
    private UserService userService;
    private SyncService syncService;
    private GoogleSignInHelper googleSignInHelper;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeServices();
        initializeUI();
        setupNavigationMenu();
        initializeSharedPreferences();
        checkUserLogin();
        setupDictionaryButton();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        loadFragment(new HomeFragment());
        syncData();
    }

    private void initializeServices() {
        navigationService = new NavigationService(this);
        userService = new UserService(this);
        syncService = new SyncService(this);
        sessionManager = new SessionManager(this);
        
        googleSignInHelper = new GoogleSignInHelper(this, new GoogleSignInHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Handle success
            }

            @Override
            public void onError(String message) {
                // Handle error
            }
        });
    }

    private void initializeUI() {
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

    private void syncData() {
        try {
            syncService.setupOfflineCapability();
            syncService.syncUserData();
            syncService.syncUserProgress();
            syncService.listenForRemoteChanges();
        } catch (Exception e) {
            Log.d(TAG, "Cannot sync with Firebase", e);
        }
    }

    private void initializeSharedPreferences() {
        String username = getIntent().getStringExtra("username");
        int userId = getIntent().getIntExtra("userId", -1);

        if (username != null && userId != -1) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("username", username).putInt("userId", userId).apply();
        }
    }

    private void checkUserLogin() {
        if (!sessionManager.isLoggedIn() || 
            (sessionManager.isGoogleUser() && !googleSignInHelper.CheckGoogleLoginState())) {
            navigationService.navigateToLogin();
            finish();
        }
    }

    private void setupNavigationMenu() {
        navigationView.setNavigationItemSelectedListener(item -> {
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
            finishAffinity();
            System.exit(0);
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

    private void setupDictionaryButton() {
        dictionary.setOnClickListener(v -> navigationService.navigateToDictionary());
    }
}
