package com.example.elsa_speak_clone.models;

public class LeaderboardEntry {
    private String username;
    private int userId;
    private int streak;
    private int xp;
    
    public LeaderboardEntry(String username, int userId, int streak, int xp) {
        this.username = username;
        this.userId = userId;
        this.streak = streak;
        this.xp = xp;
    }
    
    public String getUsername() {
        return username;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public int getStreak() {
        return streak;
    }
    
    public int getXp() {
        return xp;
    }
} 