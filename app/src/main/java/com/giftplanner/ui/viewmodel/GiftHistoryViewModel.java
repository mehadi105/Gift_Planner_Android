package com.giftplanner.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.giftplanner.data.entity.GiftHistory;
import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.repository.GiftHistoryRepository;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;

import java.util.List;

public class GiftHistoryViewModel extends ViewModel {
    private final GiftHistoryRepository giftHistoryRepository;
    private final OccasionsRepository occasionsRepository;
    private final PeopleRepository peopleRepository;
    private final MutableLiveData<List<GiftHistory>> giftHistory = new MutableLiveData<>();
    private final MutableLiveData<List<Occasion>> occasions = new MutableLiveData<>();
    private final MutableLiveData<GiftHistory> selectedGiftHistory = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private Long selectedPersonFilter = null;
    private String startDateFilter = null;
    private String endDateFilter = null;
    
    public GiftHistoryViewModel(GiftHistoryRepository giftHistoryRepository,
                               OccasionsRepository occasionsRepository,
                               PeopleRepository peopleRepository) {
        this.giftHistoryRepository = giftHistoryRepository;
        this.occasionsRepository = occasionsRepository;
        this.peopleRepository = peopleRepository;
    }
    
    public LiveData<List<GiftHistory>> getGiftHistory() {
        return giftHistory;
    }
    
    public LiveData<List<Occasion>> getOccasions() {
        return occasions;
    }
    
    public LiveData<GiftHistory> getSelectedGiftHistory() {
        return selectedGiftHistory;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }
    
    public void loadGiftHistory(long userId) {
        if (startDateFilter != null && endDateFilter != null) {
            giftHistoryRepository.getGiftHistoryByDateRange(
                userId, startDateFilter, endDateFilter,
                new GiftHistoryRepository.RepositoryCallback<List<GiftHistory>>() {
                    @Override
                    public void onSuccess(List<GiftHistory> result) {
                        giftHistory.postValue(result);
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        error.postValue("Failed to load gift history: " + e.getMessage());
                    }
                }
            );
        } else {
            giftHistoryRepository.getAllGiftHistory(userId).observeForever(historyList -> {
                giftHistory.postValue(historyList);
            });
        }
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
    
    public void setDateRangeFilter(String startDate, String endDate) {
        startDateFilter = startDate;
        endDateFilter = endDate;
    }
    
    public void clearDateRangeFilter() {
        startDateFilter = null;
        endDateFilter = null;
    }
    
    public void selectGiftHistory(GiftHistory giftHistory) {
        selectedGiftHistory.setValue(giftHistory);
    }
    
    public void clearSelection() {
        selectedGiftHistory.setValue(null);
    }
    
    public void addGiftHistory(GiftHistory giftHistory) {
        if (giftHistory.getOccasionId() == 0) {
            error.setValue("Occasion is required");
            return;
        }
        if (giftHistory.getGiftGiven() == null || giftHistory.getGiftGiven().trim().isEmpty()) {
            error.setValue("Gift given is required");
            return;
        }
        if (giftHistory.getCost() < 0) {
            error.setValue("Cost must be a valid number");
            return;
        }
        
        this.giftHistoryRepository.insertGiftHistory(giftHistory, new GiftHistoryRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                operationSuccess.postValue(true);
                error.postValue(null);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to add gift history: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
    
    public void updateGiftHistory(GiftHistory giftHistory) {
        if (giftHistory.getOccasionId() == 0) {
            error.setValue("Occasion is required");
            return;
        }
        if (giftHistory.getGiftGiven() == null || giftHistory.getGiftGiven().trim().isEmpty()) {
            error.setValue("Gift given is required");
            return;
        }
        
        this.giftHistoryRepository.updateGiftHistory(giftHistory, new GiftHistoryRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                operationSuccess.postValue(true);
                error.postValue(null);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to update gift history: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
    
    public void deleteGiftHistory(GiftHistory giftHistory) {
        this.giftHistoryRepository.deleteGiftHistory(giftHistory, new GiftHistoryRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                operationSuccess.postValue(true);
                error.postValue(null);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to delete gift history: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
}


