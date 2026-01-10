package com.giftplanner.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "GiftPlannerSession";
    private static final String KEY_USER_ID = "user_id";
    private static final long DEFAULT_USER_ID = -1;
    
    private static SessionManager instance;
    private final SharedPreferences preferences;
    
    private SessionManager(Context context) {
        preferences = context.getApplicationContext()
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }
    
    public void saveUserId(long userId) {
        preferences.edit().putLong(KEY_USER_ID, userId).apply();
    }
    
    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, DEFAULT_USER_ID);
    }
    
    public boolean isLoggedIn() {
        return getUserId() != DEFAULT_USER_ID;
    }
    
    public void clearSession() {
        preferences.edit().clear().apply();
    }
}


