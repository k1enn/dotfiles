package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.elsa_speak_clone.database.entity.SharedResult;

import java.util.List;

@Dao
public interface SharedResultDao {
    @Insert
    long insert(SharedResult sharedResult);

    @Query("SELECT * FROM SharedResult WHERE UserId = :userId ORDER BY ShareDate DESC")
    List<SharedResult> getUserSharedResults(int userId);

    @Query("SELECT * FROM SharedResult ORDER BY ShareDate DESC LIMIT :limit")
    List<SharedResult> getRecentSharedResults(int limit);
} 