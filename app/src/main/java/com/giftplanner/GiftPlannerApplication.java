package com.giftplanner;

import android.app.Application;
import com.giftplanner.data.GiftPlannerDatabase;
import com.giftplanner.util.SessionManager;

public class GiftPlannerApplication extends Application {
    private GiftPlannerDatabase database;
    private SessionManager sessionManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        database = GiftPlannerDatabase.getDatabase(this);
        sessionManager = SessionManager.getInstance(this);
    }
    
    public GiftPlannerDatabase getDatabase() {
        return database;
    }
    
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}


