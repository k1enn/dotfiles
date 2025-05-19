package com.example.elsa_speak_clone.activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.GoogleSignInHelper;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.fragments.HomeFragment;
import com.example.elsa_speak_clone.fragments.LearnFragment;
import com.example.elsa_speak_clone.fragments.ProfileFragment;
import com.example.elsa_speak_clone.fragments.PronunciationFragment;
import com.example.elsa_speak_clone.services.NavigationService;
import com.example.elsa_speak_clone.utilities.ConfigManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    // UI components
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private ImageButton chatbot;

    private NavigationService navigationService;
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

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_learn) {
               loadFragment (new LearnFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        // Check if we should navigate to a specific tab
        if (getIntent().hasExtra("NAVIGATE_TO_TAB")) {
            String tabToOpen = getIntent().getStringExtra("NAVIGATE_TO_TAB");
            if ("learn".equals(tabToOpen)) {
                // Navigate to Learn tab
                bottomNavigationView.setSelectedItemId(R.id.nav_learn);
            }
        }
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        navigationService = new NavigationService(this);

        // Adding this so it doesn't crash
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
        chatbot = findViewById(R.id.btnChatbot);
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

    // Side navigation menu (in progress)
    private void setupNavigationMenu() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectFragment (new HomeFragment () ,R.id.nav_home);
            } else if (id == R.id.nav_learn) {
                 selectFragment (new LearnFragment () ,R.id.nav_learn);
            } else if (id == R.id.nav_discover) {
                navigationService.navigateToNews(MainActivity.this);
            } else if (id == R.id.nav_leaderboard) {
                navigationService.navigateToLeaderboard(MainActivity.this);
            } else if (id == R.id.nav_account) {
                loadFragment (new ProfileFragment ());
                selectFragment (new ProfileFragment () ,R.id.nav_profile);
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

    // Bottom navigation menu
    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        //Fragment selectedFragment;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_learn) {
         //   selectedFragment = new LearnFragment();
            navigationService.navigateToLearnFragment(MainActivity.this);
        } else if (itemId == R.id.nav_home) {
            navigationService.navigateToHomeFragment(MainActivity.this);
        } else if (itemId == R.id.nav_profile) {
            navigationService.navigateToProfileFragment(MainActivity.this);
        } else {
            return false;
        }
        return true;
    };
    private void selectFragment(Fragment fragment, int navItemId) {
        // Load the fragment
        loadFragment(fragment);

        // Update bottom navigation UI to show the selected item
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(navItemId);
        }
    }
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commit();
    }

    private void setupDictionaryButton() {
        chatbot.setOnClickListener(v -> {
        navigationService.navigateToChatbot(MainActivity.this);
        });
    }


}
