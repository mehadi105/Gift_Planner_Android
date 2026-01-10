package com.giftplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.giftplanner.data.entity.Person;

import java.util.List;

@Dao
public interface PersonDao {
    @Insert
    long insert(Person person);
    
    @Update
    void update(Person person);
    
    @Delete
    void delete(Person person);
    
    @Query("SELECT * FROM people WHERE user_id = :userId ORDER BY name ASC")
    LiveData<List<Person>> getAllPeopleByUser(long userId);
    
    @Query("SELECT * FROM people WHERE id = :id LIMIT 1")
    Person getPersonById(long id);
    
    @Query("SELECT * FROM people WHERE user_id = :userId ORDER BY name ASC")
    List<Person> getAllPeopleByUserSync(long userId);
}


