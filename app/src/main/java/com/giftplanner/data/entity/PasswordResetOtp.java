package com.giftplanner.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "password_reset_otps",
    indices = {
        @Index("email"),
        @Index("expires_at")
    }
)
public class PasswordResetOtp {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String email;
    private String otp;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "expires_at")
    private long expiresAt;
    
    public PasswordResetOtp(String email, String otp, long expiresAt) {
        this.email = email;
        this.otp = otp;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = expiresAt;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getOtp() {
        return otp;
    }
    
    public void setOtp(String otp) {
        this.otp = otp;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}


