package com.giftplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.giftplanner.data.entity.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    long insert(Budget budget);
    
    @Update
    void update(Budget budget);
    
    @Delete
    void delete(Budget budget);
    
    @Query("SELECT * FROM budgets WHERE user_id = :userId ORDER BY month DESC")
    LiveData<List<Budget>> getAllBudgetsByUser(long userId);
    
    @Query("SELECT * FROM budgets WHERE user_id = :userId AND month = :month LIMIT 1")
    Budget getBudgetByMonth(long userId, String month);
    
    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    Budget getBudgetById(long id);
}


