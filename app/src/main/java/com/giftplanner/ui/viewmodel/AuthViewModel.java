package com.giftplanner.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.giftplanner.data.entity.User;
import com.giftplanner.data.repository.AuthRepository;

import java.util.regex.Pattern;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.IDLE);
    private final MutableLiveData<String> otpEmail = new MutableLiveData<>("");
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+"
    );
    
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }
    
    public LiveData<AuthState> getAuthState() {
        return authState;
    }
    
    public LiveData<String> getOtpEmail() {
        return otpEmail;
    }
    
    public void register(String username, String email, String password, String confirmPassword) {
        authState.setValue(AuthState.LOADING);
        
        // Validation
        if (username.trim().isEmpty()) {
            authState.setValue(AuthState.error("Username is required"));
            return;
        }
        if (email.trim().isEmpty()) {
            authState.setValue(AuthState.error("Email is required"));
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            authState.setValue(AuthState.error("Invalid email format"));
            return;
        }
        if (password.length() < 4) {
            authState.setValue(AuthState.error("Password must be at least 4 characters"));
            return;
        }
        if (!password.equals(confirmPassword)) {
            authState.setValue(AuthState.error("Passwords do not match"));
            return;
        }
        
        authRepository.register(username, email, password, new AuthRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                authState.postValue(AuthState.REGISTER_SUCCESS);
            }
            
            @Override
            public void onError(Exception e) {
                authState.postValue(AuthState.error(e.getMessage()));
            }
        });
    }
    
    public void login(String username, String password) {
        authState.setValue(AuthState.LOADING);
        
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            authState.setValue(AuthState.error("Username and password are required"));
            return;
        }
        
        authRepository.login(username, password, new AuthRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                authState.postValue(AuthState.loginSuccess(result.getId()));
            }
            
            @Override
            public void onError(Exception e) {
                authState.postValue(AuthState.error(e.getMessage()));
            }
        });
    }
    
    public void sendOtp(String email) {
        authState.setValue(AuthState.LOADING);
        
        if (email.trim().isEmpty()) {
            authState.setValue(AuthState.error("Email is required"));
            return;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            authState.setValue(AuthState.error("Invalid email format"));
            return;
        }
        
        authRepository.generateOtp(email, new AuthRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                otpEmail.postValue(email);
                authState.postValue(AuthState.OTP_SENT);
            }
            
            @Override
            public void onError(Exception e) {
                authState.postValue(AuthState.error(e.getMessage()));
            }
        });
    }
    
    public void resetPassword(String otp, String newPassword, String confirmPassword) {
        authState.setValue(AuthState.LOADING);
        
        String email = otpEmail.getValue();
        if (email == null || email.isEmpty()) {
            authState.setValue(AuthState.error("Invalid session"));
            return;
        }
        
        // Validation
        if (otp.trim().isEmpty()) {
            authState.setValue(AuthState.error("OTP is required"));
            return;
        }
        if (newPassword.length() < 4) {
            authState.setValue(AuthState.error("Password must be at least 4 characters"));
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            authState.setValue(AuthState.error("Passwords do not match"));
            return;
        }
        
        authRepository.resetPassword(email, otp, newPassword, new AuthRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                authState.postValue(AuthState.PASSWORD_RESET_SUCCESS);
            }
            
            @Override
            public void onError(Exception e) {
                authState.postValue(AuthState.error(e.getMessage()));
            }
        });
    }
    
    public void resetState() {
        authState.setValue(AuthState.IDLE);
    }
    
    // AuthState sealed class equivalent in Java
    public static class AuthState {
        public static final AuthState IDLE = new AuthState("IDLE", null, 0);
        public static final AuthState LOADING = new AuthState("LOADING", null, 0);
        public static final AuthState REGISTER_SUCCESS = new AuthState("REGISTER_SUCCESS", null, 0);
        public static final AuthState OTP_SENT = new AuthState("OTP_SENT", null, 0);
        public static final AuthState PASSWORD_RESET_SUCCESS = new AuthState("PASSWORD_RESET_SUCCESS", null, 0);
        
        private final String type;
        private final String message;
        private final long userId;
        
        private AuthState(String type, String message, long userId) {
            this.type = type;
            this.message = message;
            this.userId = userId;
        }
        
        public static AuthState error(String message) {
            return new AuthState("ERROR", message, 0);
        }
        
        public static AuthState loginSuccess(long userId) {
            return new AuthState("LOGIN_SUCCESS", null, userId);
        }
        
        public String getType() {
            return type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public long getUserId() {
            return userId;
        }
    }
}


