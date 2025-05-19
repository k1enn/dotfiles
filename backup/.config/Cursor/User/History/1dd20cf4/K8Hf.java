package com.example.elsa_speak_clone.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserScore;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM Users WHERE UserId = :userId")
    User getUserById(int userId);

    @Query("SELECT * FROM Users WHERE Gmail = :email")
    User getUserByEmail(String email);
    
    @Query("SELECT * FROM Users WHERE Name = :username")
    User getUserByUsername(String username);
    
    @Query("SELECT COUNT(*) FROM Users WHERE Gmail = :email")
    int checkEmailExists(String email);
    
    @Query("SELECT COUNT(*) FROM Users WHERE Name = :username")
    int checkUsernameExists(String username);
    
    @Query("SELECT * FROM Users WHERE Gmail = :usernameOrEmail OR Name = :usernameOrEmail")
    User getUserByEmailOrUsername(String usernameOrEmail);
    
    @Query("SELECT * FROM Users WHERE Name = :username AND google = 0")
    User getLocalUserByUsername(String username);
    
    @Query("SELECT * FROM Users WHERE Gmail = :email AND google = 1")
    User getGoogleUserByEmail(String email);
    
    @Query("DELETE FROM Users WHERE UserId = :userId")
    int deleteUserById(int userId);

    @Query("SELECT * FROM Users")
    List<User> getAllUsers();

    @Query("SELECT * FROM UserScores us JOIN Quizzes q ON us.QuizId = q.QuizId " +
           "WHERE us.UserId = :userId AND q.LessonId = :lessonId LIMIT 1")
    UserScore getUserScoreByLessonAndUser(int userId, int lessonId);
} 