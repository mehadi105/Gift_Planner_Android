package com.giftplanner.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;
import com.giftplanner.data.dao.PasswordResetOtpDao;
import com.giftplanner.data.dao.PersonDao;
import com.giftplanner.data.dao.UserDao;
import com.giftplanner.data.entity.PasswordResetOtp;
import com.giftplanner.data.entity.Person;
import com.giftplanner.data.entity.User;

@Database(
    entities = {
        User.class,
        Person.class,
        PasswordResetOtp.class
    },
    version = 2,
    exportSchema = false
)
public abstract class GiftPlannerDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract PersonDao personDao();
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


