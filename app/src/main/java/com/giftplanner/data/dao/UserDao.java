package com.giftplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.giftplanner.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);
    
    @Update
    void update(User user);
    
    @Delete
    void delete(User user);
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(long id);
    
    @Query("SELECT * FROM users WHERE username = :username AND password = :passwordHash LIMIT 1")
    User authenticateUser(String username, String passwordHash);
}


