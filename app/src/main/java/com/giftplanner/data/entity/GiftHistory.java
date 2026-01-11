package com.giftplanner.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "gift_history",
    foreignKeys = {
        @ForeignKey(
            entity = User.class,
            parentColumns = "id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Occasion.class,
            parentColumns = "id",
            childColumns = "occasion_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("user_id"),
        @Index("occasion_id"),
        @Index("given_date")
    }
)
public class GiftHistory {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "occasion_id")
    private long occasionId;
    
    @ColumnInfo(name = "gift_given")
    private String giftGiven;
    
    private double cost;
    private String store;
    private String link;
    
    @ColumnInfo(name = "given_date")
    private String givenDate; // ISO date string
    
    public GiftHistory(long userId, long occasionId, String giftGiven, double cost, String givenDate) {
        this.userId = userId;
        this.occasionId = occasionId;
        this.giftGiven = giftGiven;
        this.cost = cost;
        this.givenDate = givenDate;
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
    
    public long getOccasionId() {
        return occasionId;
    }
    
    public void setOccasionId(long occasionId) {
        this.occasionId = occasionId;
    }
    
    public String getGiftGiven() {
        return giftGiven;
    }
    
    public void setGiftGiven(String giftGiven) {
        this.giftGiven = giftGiven;
    }
    
    public double getCost() {
        return cost;
    }
    
    public void setCost(double cost) {
        this.cost = cost;
    }
    
    public String getStore() {
        return store;
    }
    
    public void setStore(String store) {
        this.store = store;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public String getGivenDate() {
        return givenDate;
    }
    
    public void setGivenDate(String givenDate) {
        this.givenDate = givenDate;
    }
}


