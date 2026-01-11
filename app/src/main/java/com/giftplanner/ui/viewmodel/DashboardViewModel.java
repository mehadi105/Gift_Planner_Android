package com.giftplanner.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.entity.Person;
import com.giftplanner.data.repository.BudgetRepository;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;
import com.giftplanner.util.DateFormatter;

import java.util.List;

public class DashboardViewModel extends ViewModel {
    private final PeopleRepository peopleRepository;
    private final OccasionsRepository occasionsRepository;
    private final BudgetRepository budgetRepository;
    
    private final MutableLiveData<Integer> totalPeople = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> upcoming7Days = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> upcoming30Days = new MutableLiveData<>(0);
    private final MutableLiveData<Double> monthlyBudget = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> monthlySpent = new MutableLiveData<>(0.0);
    private final MutableLiveData<List<Occasion>> upcomingOccasions = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    public DashboardViewModel(PeopleRepository peopleRepository, 
                             OccasionsRepository occasionsRepository,
                             BudgetRepository budgetRepository) {
        this.peopleRepository = peopleRepository;
        this.occasionsRepository = occasionsRepository;
        this.budgetRepository = budgetRepository;
    }
    
    public LiveData<Integer> getTotalPeople() {
        return totalPeople;
    }
    
    public LiveData<Integer> getUpcoming7Days() {
        return upcoming7Days;
    }
    
    public LiveData<Integer> getUpcoming30Days() {
        return upcoming30Days;
    }
    
    public LiveData<Double> getMonthlyBudget() {
        return monthlyBudget;
    }
    
    public LiveData<Double> getMonthlySpent() {
        return monthlySpent;
    }
    
    public LiveData<List<Occasion>> getUpcomingOccasions() {
        return upcomingOccasions;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public void loadDashboardData(long userId) {
        // Load total people count
        peopleRepository.getAllPeopleSync(userId, new PeopleRepository.RepositoryCallback<List<Person>>() {
            @Override
            public void onSuccess(List<Person> result) {
                totalPeople.postValue(result != null ? result.size() : 0);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to load people: " + e.getMessage());
            }
        });
        
        // Load upcoming occasions count (7 days)
        occasionsRepository.getOccasionCount(userId, 7, new OccasionsRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                upcoming7Days.postValue(result);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to load occasions: " + e.getMessage());
            }
        });
        
        // Load upcoming occasions count (30 days)
        occasionsRepository.getOccasionCount(userId, 30, new OccasionsRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                upcoming30Days.postValue(result);
            }
            
            @Override
            public void onError(Exception e) {
                // Error already set
            }
        });
        
        // Load upcoming occasions (60 days) for table
        occasionsRepository.getUpcomingOccasions(userId, 60, new OccasionsRepository.RepositoryCallback<List<Occasion>>() {
            @Override
            public void onSuccess(List<Occasion> result) {
                upcomingOccasions.postValue(result);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to load upcoming occasions: " + e.getMessage());
            }
        });
        
        // Load current month budget
        String currentMonth = DateFormatter.getCurrentYearMonth();
        budgetRepository.getBudgetByMonth(userId, currentMonth, new BudgetRepository.RepositoryCallback<com.giftplanner.data.entity.Budget>() {
            @Override
            public void onSuccess(com.giftplanner.data.entity.Budget result) {
                if (result != null) {
                    monthlyBudget.postValue(result.getPlannedBudget());
                    monthlySpent.postValue(result.getActualSpent());
                } else {
                    monthlyBudget.postValue(0.0);
                    monthlySpent.postValue(0.0);
                }
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to load budget: " + e.getMessage());
            }
        });
    }
}


