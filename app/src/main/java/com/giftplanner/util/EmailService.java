package com.giftplanner.util;

import android.util.Log;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    private static final String TAG = "EmailService";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    
    private static String emailUsername = "";
    private static String emailPassword = "";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public static void configure(String username, String password) {
        emailUsername = username;
        emailPassword = password;
    }
    
    public static void sendOtpEmail(String toEmail, String otp, EmailCallback callback) {
        executor.execute(() -> {
            try {
                // If email not configured, print to log (development mode)
                if (emailUsername.isEmpty() || emailPassword.isEmpty()) {
                    Log.d(TAG, "========================================");
                    Log.d(TAG, "EMAIL NOT CONFIGURED - DEVELOPMENT MODE");
                    Log.d(TAG, "OTP for " + toEmail + ": " + otp);
                    Log.d(TAG, "This OTP will expire in 10 minutes");
                    Log.d(TAG, "========================================");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    return;
                }
                
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailUsername, emailPassword);
                    }
                });
                
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(emailUsername));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("Password Reset OTP - Gift & Occasion Planner");
                message.setText("Hello,\n\n" +
                    "Your password reset OTP is: " + otp + "\n\n" +
                    "This OTP will expire in 10 minutes.\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "Gift & Occasion Planner Team");
                
                Transport.send(message);
                Log.d(TAG, "Email sent successfully to " + toEmail);
                
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to send email", e);
                // In case of email error, log the OTP for development
                Log.d(TAG, "========================================");
                Log.d(TAG, "EMAIL SEND FAILED - FALLBACK MODE");
                Log.d(TAG, "OTP for " + toEmail + ": " + otp);
                Log.d(TAG, "========================================");
                
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }
    
    public interface EmailCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}


