package com.giftplanner.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;
import com.giftplanner.data.dao.BudgetDao;
import com.giftplanner.data.dao.GiftHistoryDao;
import com.giftplanner.data.dao.OccasionDao;
import com.giftplanner.data.dao.PasswordResetOtpDao;
import com.giftplanner.data.dao.PersonDao;
import com.giftplanner.data.dao.UserDao;
import com.giftplanner.data.entity.Budget;
import com.giftplanner.data.entity.GiftHistory;
import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.entity.PasswordResetOtp;
import com.giftplanner.data.entity.Person;
import com.giftplanner.data.entity.User;

@Database(
    entities = {
        User.class,
        Person.class,
        Occasion.class,
        Budget.class,
        GiftHistory.class,
        PasswordResetOtp.class
    },
    version = 4,
    exportSchema = false
)
public abstract class GiftPlannerDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract PersonDao personDao();
    public abstract OccasionDao occasionDao();
    public abstract BudgetDao budgetDao();
    public abstract GiftHistoryDao giftHistoryDao();
    public abstract PasswordResetOtpDao passwordResetOtpDao();

    private static volatile GiftPlannerDatabase INSTANCE;

    public static GiftPlannerDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GiftPlannerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            GiftPlannerDatabase.class,
                            "gift_planner.db"
                        )
                        .addCallback(new RoomDatabase.Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                db.execSQL("PRAGMA foreign_keys = ON");
                            }
                            
                            @Override
                            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                super.onOpen(db);
                                db.execSQL("PRAGMA foreign_keys = ON");
                            }
                        })
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }
}


