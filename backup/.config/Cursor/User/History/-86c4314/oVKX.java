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

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    
    // UI components
    private BottomNavigationView bottomNavigationView;
    private GoogleSignInHelper googleSignInHelper;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeServices();
        setupToolbarAndDrawer(); // Call base class method
        initializeUI();
        initializeSharedPreferences();
        checkUserLogin();
        
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        loadFragment(new HomeFragment());
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        
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
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navListener);
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

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commit();
    }
}
