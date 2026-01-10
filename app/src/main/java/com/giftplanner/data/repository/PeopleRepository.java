package com.giftplanner.data.repository;

import androidx.lifecycle.LiveData;
import com.giftplanner.data.dao.PersonDao;
import com.giftplanner.data.entity.Person;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeopleRepository {
    private final PersonDao personDao;
    private final ExecutorService executorService;
    
    public PeopleRepository(PersonDao personDao) {
        this.personDao = personDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<Person>> getAllPeople(long userId) {
        return personDao.getAllPeopleByUser(userId);
    }
    
    public void insertPerson(Person person, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = personDao.insert(person);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void updatePerson(Person person, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                personDao.update(person);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deletePerson(Person person, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                personDao.delete(person);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getPersonById(long id, RepositoryCallback<Person> callback) {
        executorService.execute(() -> {
            try {
                Person person = personDao.getPersonById(id);
                callback.onSuccess(person);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getAllPeopleSync(long userId, RepositoryCallback<List<Person>> callback) {
        executorService.execute(() -> {
            try {
                List<Person> people = personDao.getAllPeopleByUserSync(userId);
                callback.onSuccess(people);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}


