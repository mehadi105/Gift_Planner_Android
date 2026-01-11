package com.giftplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.giftplanner.data.entity.GiftHistory;

import java.util.List;

@Dao
public interface GiftHistoryDao {
    @Insert
    long insert(GiftHistory giftHistory);
    
    @Update
    void update(GiftHistory giftHistory);
    
    @Delete
    void delete(GiftHistory giftHistory);
    
    @Query("SELECT * FROM gift_history WHERE user_id = :userId ORDER BY given_date DESC")
    LiveData<List<GiftHistory>> getAllGiftHistoryByUser(long userId);
    
    @Query("SELECT * FROM gift_history WHERE id = :id LIMIT 1")
    GiftHistory getGiftHistoryById(long id);
    
    @Query("SELECT * FROM gift_history WHERE user_id = :userId AND given_date BETWEEN :startDate AND :endDate")
    List<GiftHistory> getGiftHistoryByDateRange(long userId, String startDate, String endDate);
    
    @Query("SELECT SUM(cost) FROM gift_history WHERE user_id = :userId AND given_date BETWEEN :startDate AND :endDate")
    Double getTotalSpentByDateRange(long userId, String startDate, String endDate);
}


