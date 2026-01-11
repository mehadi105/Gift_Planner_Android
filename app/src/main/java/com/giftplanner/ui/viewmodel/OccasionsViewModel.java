package com.giftplanner.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.entity.Person;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;

import java.util.List;

public class OccasionsViewModel extends ViewModel {
    private final OccasionsRepository occasionsRepository;
    private final PeopleRepository peopleRepository;
    private final MutableLiveData<List<Occasion>> occasions = new MutableLiveData<>();
    private final MutableLiveData<List<Person>> people = new MutableLiveData<>();
    private final MutableLiveData<Occasion> selectedOccasion = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private Long selectedPersonFilter = null;
    
    public OccasionsViewModel(OccasionsRepository occasionsRepository, PeopleRepository peopleRepository) {
        this.occasionsRepository = occasionsRepository;
        this.peopleRepository = peopleRepository;
    }
    
    public LiveData<List<Occasion>> getOccasions() {
        return occasions;
    }
    
    public LiveData<List<Person>> getPeople() {
        return people;
    }
    
    public LiveData<Occasion> getSelectedOccasion() {
        return selectedOccasion;
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
    
    public void loadOccasions(long userId) {
        if (selectedPersonFilter != null) {
            occasionsRepository.getOccasionsByPerson(userId, selectedPersonFilter)
                .observeForever(occasionsList -> {
                    occasions.postValue(occasionsList);
                });
        } else {
            occasionsRepository.getAllOccasions(userId).observeForever(occasionsList -> {
                occasions.postValue(occasionsList);
            });
        }
    }
    
    public void setPersonFilter(Long personId) {
        selectedPersonFilter = personId;
    }
    
    public void clearPersonFilter() {
        selectedPersonFilter = null;
    }
    
    public void selectOccasion(Occasion occasion) {
        selectedOccasion.setValue(occasion);
    }
    
    public void clearSelection() {
        selectedOccasion.setValue(null);
    }
    
    public void addOccasion(Occasion occasion) {
        if (occasion.getPersonId() == 0) {
            error.setValue("Person is required");
            return;
        }
        if (occasion.getEventDate() == null || occasion.getEventDate().isEmpty()) {
            error.setValue("Event date is required");
            return;
        }
        if (occasion.getEventType() == null || occasion.getEventType().isEmpty()) {
            error.setValue("Event type is required");
            return;
        }
        if ("custom".equals(occasion.getEventType()) && 
            (occasion.getGiftIdea() == null || occasion.getGiftIdea().isEmpty())) {
            error.setValue("Custom event name is required when event type is custom");
            return;
        }
        
        occasionsRepository.insertOccasion(occasion, new OccasionsRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                operationSuccess.postValue(true);
                error.postValue(null);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to add occasion: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
    
    public void updateOccasion(Occasion occasion) {
        if (occasion.getPersonId() == 0) {
            error.setValue("Person is required");
            return;
        }
        if (occasion.getEventDate() == null || occasion.getEventDate().isEmpty()) {
            error.setValue("Event date is required");
            return;
        }
        if (occasion.getEventType() == null || occasion.getEventType().isEmpty()) {
            error.setValue("Event type is required");
            return;
        }
        
        occasionsRepository.updateOccasion(occasion, new OccasionsRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                operationSuccess.postValue(true);
                error.postValue(null);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to update occasion: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
    
    public void deleteOccasion(Occasion occasion) {
        occasionsRepository.deleteOccasion(occasion, new OccasionsRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                operationSuccess.postValue(true);
                error.postValue(null);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to delete occasion: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
}


