package com.example.elsa_speak_clone.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.adapters.LeaderboardAdapter;
import com.example.elsa_speak_clone.database.firebase.FirebaseDataManager;
import com.example.elsa_speak_clone.models.LeaderboardEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "LeaderboardActivity";
    private static final int LEADERBOARD_LIMIT = 20; // Top 20 users
    
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private FirebaseDataManager firebaseDataManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerLeaderboard);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        // Initialize Firebase manager
        firebaseDataManager = FirebaseDataManager.getInstance(this);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        
        // Load leaderboard data
        loadLeaderboard();
    }
    
    private void loadLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);
        
        firebaseDataManager.getTopUsers(LEADERBOARD_LIMIT)
                .thenAccept(usersMap -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        
                        if (usersMap.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            
                            // Convert map to list for adapter
                            List<LeaderboardEntry> entries = new ArrayList<>();
                            
                            for (Map.Entry<String, Map<String, Object>> entry : usersMap.entrySet()) {
                                String username = entry.getKey();
                                Map<String, Object> userData = entry.getValue();
                                
                                // Safely convert values from Firebase (handles both Integer and Long)
                                int userId = 0;
                                int streak = 0;
                                int xp = 0;
                                
                                if (userData.get("userId") != null) {
                                    userId = ((Number) userData.get("userId")).intValue();
                                }
                                
                                if (userData.get("userStreak") != null) {
                                    streak = ((Number) userData.get("userStreak")).intValue();
                                }
                                
                                if (userData.get("userXp") != null) {
                                    xp = ((Number) userData.get("userXp")).intValue();
                                }
                                
                                entries.add(new LeaderboardEntry(
                                        username, 
                                        userId, 
                                        streak, 
                                        xp
                                ));
                            }
                            
                            // Sort by XP (highest first)
                            Collections.sort(entries, (e1, e2) -> Integer.compare(e2.getXp(), e1.getXp()));
                            
                            // Update adapter
                            adapter.updateData(entries);
                        }
                    });
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error loading leaderboard: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText(R.string.error_loading_leaderboard);
                    });
                    return null;
                });
    }
} 