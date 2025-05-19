package com.example.elsa_speak_clone.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.services.NavigationService;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle toggle;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected ImageButton btnChatbot;
    protected NavigationService navigationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Child classes should call setContentView before calling setupToolbarAndDrawer
    }
    
    /**
     * Set up the toolbar and navigation drawer.
     * Call this method after setContentView in child activities.
     */
    protected void setupToolbarAndDrawer() {
        try {
            // Initialize navigation service
            navigationService = new NavigationService(this);
            
            // Set up toolbar
            toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(" ");
            }
            
            // Set up drawer and toggle
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            
            if (drawerLayout != null && navigationView != null) {
                toggle = new ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                );
                
                drawerLayout.addDrawerListener(toggle);
                toggle.syncState();
                
                // Set up navigation menu
                setupNavigationMenu();
            }
            
            // Set up chatbot button if it exists
            btnChatbot = findViewById(R.id.btnChatbot);
            if (btnChatbot != null) {
                btnChatbot.setOnClickListener(v -> {
                    navigationService.navigateToChatbot(this);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar and drawer: " + e.getMessage());
        }
    }
    
    /**
     * Set up the navigation menu click listener.
     * Override this method to customize behavior.
     */
    protected void setupNavigationMenu() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navigationService.navigateToHomeFragment(this);
            } else if (id == R.id.nav_learn) {
                navigationService.navigateToLearnFragment(this);
            } else if (id == R.id.nav_discover) {
                navigationService.navigateToNews(this);
            } else if (id == R.id.nav_leaderboard) {
                navigationService.navigateToLeaderboard(this);
            } else if (id == R.id.nav_account) {
                // Handle profile action
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
} 