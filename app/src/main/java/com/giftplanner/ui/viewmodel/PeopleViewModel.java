package com.giftplanner.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.giftplanner.data.entity.Person;
import com.giftplanner.data.repository.PeopleRepository;

import java.util.List;

public class PeopleViewModel extends ViewModel {
    private final PeopleRepository peopleRepository;
    private final MutableLiveData<List<Person>> people = new MutableLiveData<>();
    private final MutableLiveData<Person> selectedPerson = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    
    public PeopleViewModel(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }
    
    public LiveData<List<Person>> getPeople() {
        return people;
    }
    
    public LiveData<Person> getSelectedPerson() {
        return selectedPerson;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }
    
    public void loadPeople(long userId) {
        peopleRepository.getAllPeople(userId).observeForever(peopleList -> {
            people.postValue(peopleList);
        });
    }
    
    public void selectPerson(Person person) {
        selectedPerson.setValue(person);
    }
    
    public void clearSelection() {
        selectedPerson.setValue(null);
    }
    
    public void addPerson(Person person) {
        if (person.getName() == null || person.getName().trim().isEmpty()) {
            error.setValue("Name is required");
            return;
        }
        
        peopleRepository.insertPerson(person, new PeopleRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                operationSuccess.postValue(true);
                error.postValue(null);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to add person: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
    
    public void updatePerson(Person person) {
        if (person.getName() == null || person.getName().trim().isEmpty()) {
            error.setValue("Name is required");
            return;
        }
        
        peopleRepository.updatePerson(person, new PeopleRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                operationSuccess.postValue(true);
                error.postValue(null);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to update person: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
    
    public void deletePerson(Person person) {
        peopleRepository.deletePerson(person, new PeopleRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                operationSuccess.postValue(true);
                error.postValue(null);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to delete person: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
}


