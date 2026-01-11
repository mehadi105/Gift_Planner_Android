package com.giftplanner.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "occasions",
    foreignKeys = {
        @ForeignKey(
            entity = User.class,
            parentColumns = "id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Person.class,
            parentColumns = "id",
            childColumns = "person_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("user_id"),
        @Index("person_id"),
        @Index("event_date")
    }
)
public class Occasion {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "person_id")
    private long personId;
    
    @ColumnInfo(name = "event_date")
    private String eventDate; // ISO date string
    
    @ColumnInfo(name = "event_type")
    private String eventType; // "birthday", "anniversary", "custom"
    
    @ColumnInfo(name = "gift_idea")
    private String giftIdea;
    
    private double budget;
    
    @ColumnInfo(name = "gift_status")
    private String giftStatus; // "pending", "decided", "bought"
    
    public Occasion(long userId, long personId, String eventDate, String eventType) {
        this.userId = userId;
        this.personId = personId;
        this.eventDate = eventDate;
        this.eventType = eventType;
        this.budget = 0.0;
        this.giftStatus = "pending";
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
    
    public long getPersonId() {
        return personId;
    }
    
    public void setPersonId(long personId) {
        this.personId = personId;
    }
    
    public String getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getGiftIdea() {
        return giftIdea;
    }
    
    public void setGiftIdea(String giftIdea) {
        this.giftIdea = giftIdea;
    }
    
    public double getBudget() {
        return budget;
    }
    
    public void setBudget(double budget) {
        this.budget = budget;
    }
    
    public String getGiftStatus() {
        return giftStatus;
    }
    
    public void setGiftStatus(String giftStatus) {
        this.giftStatus = giftStatus;
    }
}


