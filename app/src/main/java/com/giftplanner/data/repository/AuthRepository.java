package com.giftplanner.data.repository;

import com.giftplanner.data.dao.UserDao;
import com.giftplanner.data.entity.User;
import com.giftplanner.util.PasswordHasher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {
    private final UserDao userDao;
    private final ExecutorService executorService;
    
    public AuthRepository(UserDao userDao) {
        this.userDao = userDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public void register(String username, String email, String password, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                // Check if username exists
                if (userDao.getUserByUsername(username) != null) {
                    callback.onError(new Exception("Username already exists"));
                    return;
                }
                
                // Check if email exists
                if (userDao.getUserByEmail(email) != null) {
                    callback.onError(new Exception("Email already exists"));
                    return;
                }
                
                // Hash password and create user
                String hashedPassword = PasswordHasher.hashPassword(password);
                User user = new User(username, hashedPassword, email);
                long userId = userDao.insert(user);
                callback.onSuccess(userId);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void login(String username, String password, RepositoryCallback<User> callback) {
        executorService.execute(() -> {
            try {
                String hashedPassword = PasswordHasher.hashPassword(password);
                User user = userDao.authenticateUser(username, hashedPassword);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError(new Exception("Invalid username or password"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}


