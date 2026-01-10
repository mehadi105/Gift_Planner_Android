package com.giftplanner.data.dao;

import androidx.room.*;
import com.giftplanner.data.entity.PasswordResetOtp;

@Dao
public interface PasswordResetOtpDao {
    @Insert
    long insert(PasswordResetOtp otp);
    
    @Delete
    void delete(PasswordResetOtp otp);
    
    @Query("SELECT * FROM password_reset_otps WHERE email = :email AND otp = :otp AND expires_at > :currentTime LIMIT 1")
    PasswordResetOtp getValidOtp(String email, String otp, long currentTime);
    
    @Query("DELETE FROM password_reset_otps WHERE expires_at < :currentTime")
    void deleteExpiredOtps(long currentTime);
    
    @Query("DELETE FROM password_reset_otps WHERE email = :email")
    void deleteOtpsByEmail(String email);
}


