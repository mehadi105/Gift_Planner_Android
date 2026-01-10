package com.giftplanner.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "people",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("user_id")}
)
public class Person {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    private String name;
    private String relation;
    private String birthday; // ISO date string
    private String anniversary; // ISO date string
    
    @ColumnInfo(name = "custom_event")
    private String customEvent;
    
    private String notes;
    
    public Person(long userId, String name) {
        this.userId = userId;
        this.name = name;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRelation() {
        return relation;
    }
    
    public void setRelation(String relation) {
        this.relation = relation;
    }
    
    public String getBirthday() {
        return birthday;
    }
    
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    
    public String getAnniversary() {
        return anniversary;
    }
    
    public void setAnniversary(String anniversary) {
        this.anniversary = anniversary;
    }
    
    public String getCustomEvent() {
        return customEvent;
    }
    
    public void setCustomEvent(String customEvent) {
        this.customEvent = customEvent;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}


