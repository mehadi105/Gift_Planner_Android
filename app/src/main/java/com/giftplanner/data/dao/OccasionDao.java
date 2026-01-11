package com.giftplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.giftplanner.data.entity.Occasion;

import java.util.List;

@Dao
public interface OccasionDao {
    @Insert
    long insert(Occasion occasion);
    
    @Update
    void update(Occasion occasion);
    
    @Delete
    void delete(Occasion occasion);
    
    @Query("SELECT * FROM occasions WHERE user_id = :userId ORDER BY event_date ASC")
    LiveData<List<Occasion>> getAllOccasionsByUser(long userId);
    
    @Query("SELECT * FROM occasions WHERE id = :id LIMIT 1")
    Occasion getOccasionById(long id);
    
    @Query("SELECT * FROM occasions WHERE user_id = :userId AND person_id = :personId ORDER BY event_date ASC")
    LiveData<List<Occasion>> getOccasionsByPerson(long userId, long personId);
    
    @Query("SELECT * FROM occasions WHERE user_id = :userId AND event_date BETWEEN :startDate AND :endDate ORDER BY event_date ASC")
    List<Occasion> getOccasionsByDateRange(long userId, String startDate, String endDate);
    
    @Query("SELECT COUNT(*) FROM occasions WHERE user_id = :userId AND event_date BETWEEN :startDate AND :endDate")
    int getOccasionCountByDateRange(long userId, String startDate, String endDate);
}


