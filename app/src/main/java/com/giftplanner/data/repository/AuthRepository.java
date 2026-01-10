package com.giftplanner.data.repository;

import com.giftplanner.data.dao.PasswordResetOtpDao;
import com.giftplanner.data.dao.UserDao;
import com.giftplanner.data.entity.PasswordResetOtp;
import com.giftplanner.data.entity.User;
import com.giftplanner.util.EmailService;
import com.giftplanner.util.PasswordHasher;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {
    private final UserDao userDao;
    private final PasswordResetOtpDao otpDao;
    private final ExecutorService executorService;
    
    public AuthRepository(UserDao userDao, PasswordResetOtpDao otpDao) {
        this.userDao = userDao;
        this.otpDao = otpDao;
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
    
    public void generateOtp(String email, RepositoryCallback<String> callback) {
        executorService.execute(() -> {
            try {
                // Check if email exists
                User user = userDao.getUserByEmail(email);
                if (user == null) {
                    callback.onError(new Exception("Email not found"));
                    return;
                }
                
                // Generate 6-digit OTP
                Random random = new Random();
                String otp = String.format("%06d", random.nextInt(1000000));
                
                // Delete existing OTPs for this email
                otpDao.deleteOtpsByEmail(email);
                
                // Create OTP record with 10 minute expiration
                long expiresAt = System.currentTimeMillis() + (10 * 60 * 1000); // 10 minutes
                PasswordResetOtp otpRecord = new PasswordResetOtp(email, otp, expiresAt);
                otpDao.insert(otpRecord);
                
                // Send email
                EmailService.sendOtpEmail(email, otp, new EmailService.EmailCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(otp);
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        // Still return success even if email fails (OTP logged to console)
                        callback.onSuccess(otp);
                    }
                });
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void resetPassword(String email, String otp, String newPassword, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                // Verify OTP
                PasswordResetOtp otpRecord = otpDao.getValidOtp(email, otp, System.currentTimeMillis());
                if (otpRecord == null) {
                    callback.onError(new Exception("Invalid or expired OTP"));
                    return;
                }
                
                // Get user
                User user = userDao.getUserByEmail(email);
                if (user == null) {
                    callback.onError(new Exception("User not found"));
                    return;
                }
                
                // Update password
                String hashedPassword = PasswordHasher.hashPassword(newPassword);
                user.setPassword(hashedPassword);
                userDao.update(user);
                
                // Delete OTP
                otpDao.delete(otpRecord);
                
                callback.onSuccess(true);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void cleanupExpiredOtps() {
        executorService.execute(() -> {
            otpDao.deleteExpiredOtps(System.currentTimeMillis());
        });
    }
    
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}


