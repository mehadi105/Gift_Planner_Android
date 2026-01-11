package com.giftplanner.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "budgets",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("user_id"),
        @Index("month"),
        @Index(value = {"user_id", "month"}, unique = true)
    }
)
public class Budget {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    private String month; // Format: "YYYY-MM"
    
    @ColumnInfo(name = "planned_budget")
    private double plannedBudget;
    
    @ColumnInfo(name = "actual_spent")
    private double actualSpent;
    
    public Budget(long userId, String month) {
        this.userId = userId;
        this.month = month;
        this.plannedBudget = 0.0;
        this.actualSpent = 0.0;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getMonth() {
        return month;
    }
    
    public void setMonth(String month) {
        this.month = month;
    }
    
    public double getPlannedBudget() {
        return plannedBudget;
    }
    
    public void setPlannedBudget(double plannedBudget) {
        this.plannedBudget = plannedBudget;
    }
    
    public double getActualSpent() {
        return actualSpent;
    }
    
    public void setActualSpent(double actualSpent) {
        this.actualSpent = actualSpent;
    }
}


