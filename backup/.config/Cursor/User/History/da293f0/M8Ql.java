package com.example.elsa_speak_clone.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "SharedResult",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "UserId",
                childColumns = "UserId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("UserId")})
public class SharedResult {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "ShareId")
    private int shareId;

    @ColumnInfo(name = "UserId")
    private int userId;

    @ColumnInfo(name = "Message")
    private String message;

    @ColumnInfo(name = "ShareDate")
    private String shareDate;

    // Constructor
    public SharedResult(int shareId, int userId, String message, String shareDate) {
        this.shareId = shareId;
        this.userId = userId;
        this.message = message;
        this.shareDate = shareDate;
    }

    // Getters and setters
    public int getShareId() {
        return shareId;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShareDate() {
        return shareDate;
    }

    public void setShareDate(String shareDate) {
        this.shareDate = shareDate;
    }
} 