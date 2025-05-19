package com.example.elsa_speak_clone.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Users")
public class User {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "UserId")
    private int userId;

    @ColumnInfo(name = "Gmail")
    private String gmail;

    @ColumnInfo(name = "Name")
    private String name;

    @ColumnInfo(name = "Password")
    private String password;

    @ColumnInfo(name = "google")
    private int isGoogleUser;

    @ColumnInfo(name = "JoinDate")
    private String joinDate;

    @ColumnInfo(name = "last_login")
    private String lastLogin;

    // Constructor
    public User(int userId, String gmail, String name, String password, int isGoogleUser, String joinDate) {
        this.userId = userId;
        this.gmail = gmail;
        this.name = name;
        this.password = password;
        this.isGoogleUser = isGoogleUser;
        this.joinDate = joinDate;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getIsGoogleUser() {
        return isGoogleUser;
    }

    public void setIsGoogleUser(int isGoogleUser) {
        this.isGoogleUser = isGoogleUser;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
} 