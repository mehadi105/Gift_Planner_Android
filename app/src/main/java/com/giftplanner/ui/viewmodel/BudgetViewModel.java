package com.giftplanner.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.giftplanner.data.entity.Budget;
import com.giftplanner.data.entity.GiftHistory;
import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.entity.Person;
import com.giftplanner.data.repository.BudgetRepository;
import com.giftplanner.data.repository.GiftHistoryRepository;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;
import com.giftplanner.util.DateFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetViewModel extends ViewModel {
    private final BudgetRepository budgetRepository;
    private final GiftHistoryRepository giftHistoryRepository;
    private final OccasionsRepository occasionsRepository;
    private final PeopleRepository peopleRepository;
    
    private final MutableLiveData<List<Budget>> budgetHistory = new MutableLiveData<>();
    private final MutableLiveData<Budget> currentBudget = new MutableLiveData<>();
    private final MutableLiveData<Double> actualSpent = new MutableLiveData<>(0.0);
    private final MutableLiveData<Map<String, Double>> spendingByPerson = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Double>> spendingByRelation = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    
    private String selectedMonth = DateFormatter.getCurrentYearMonth();
    
    public BudgetViewModel(BudgetRepository budgetRepository,
                          GiftHistoryRepository giftHistoryRepository,
                          OccasionsRepository occasionsRepository,
                          PeopleRepository peopleRepository) {
        this.budgetRepository = budgetRepository;
        this.giftHistoryRepository = giftHistoryRepository;
        this.occasionsRepository = occasionsRepository;
        this.peopleRepository = peopleRepository;
    }
    
    public LiveData<List<Budget>> getBudgetHistory() {
        return budgetHistory;
    }
    
    public LiveData<Budget> getCurrentBudget() {
        return currentBudget;
    }
    
    public LiveData<Double> getActualSpent() {
        return actualSpent;
    }
    
    public LiveData<Map<String, Double>> getSpendingByPerson() {
        return spendingByPerson;
    }
    
    public LiveData<Map<String, Double>> getSpendingByRelation() {
        return spendingByRelation;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }
    
    public String getSelectedMonth() {
        return selectedMonth;
    }
    
    public void setSelectedMonth(String month) {
        selectedMonth = month;
    }
    
    public void loadBudgetData(long userId) {
        loadBudget(userId);
        loadBudgetHistory(userId);
        loadActualSpent(userId);
        loadChartData(userId);
    }
    
    private void loadBudget(long userId) {
        budgetRepository.getBudgetByMonth(userId, selectedMonth, new BudgetRepository.RepositoryCallback<Budget>() {
            @Override
            public void onSuccess(Budget result) {
                currentBudget.postValue(result);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to load budget: " + e.getMessage());
            }
        });
    }
    
    private void loadBudgetHistory(long userId) {
        budgetRepository.getAllBudgets(userId).observeForever(budgets -> {
            budgetHistory.postValue(budgets);
        });
    }
    
    private void loadActualSpent(long userId) {
        budgetRepository.calculateActualSpent(userId, selectedMonth, new BudgetRepository.RepositoryCallback<Double>() {
            @Override
            public void onSuccess(Double result) {
                actualSpent.postValue(result);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to calculate actual spent: " + e.getMessage());
            }
        });
    }
    
    private void loadChartData(long userId) {
        String startDate = DateFormatter.getMonthStartDate(selectedMonth);
        String endDate = DateFormatter.getMonthEndDate(selectedMonth);
        
        giftHistoryRepository.getGiftHistoryByDateRange(userId, startDate, endDate,
            new GiftHistoryRepository.RepositoryCallback<List<GiftHistory>>() {
                @Override
                public void onSuccess(List<GiftHistory> giftHistoryList) {
                    // Load occasions and people for chart data
                    occasionsRepository.getAllOccasions(userId).observeForever(occasionsList -> {
                        peopleRepository.getAllPeople(userId).observeForever(peopleList -> {
                            Map<Long, Occasion> occasionMap = new HashMap<>();
                            Map<Long, Person> personMap = new HashMap<>();
                            
                            for (Occasion occasion : occasionsList) {
                                occasionMap.put(occasion.getId(), occasion);
                            }
                            
                            for (Person person : peopleList) {
                                personMap.put(person.getId(), person);
                            }
                            
                            // Calculate spending by person
                            Map<String, Double> byPerson = new HashMap<>();
                            Map<String, Double> byRelation = new HashMap<>();
                            
                            for (GiftHistory giftHistory : giftHistoryList) {
                                Occasion occasion = occasionMap.get(giftHistory.getOccasionId());
                                if (occasion != null) {
                                    Person person = personMap.get(occasion.getPersonId());
                                    if (person != null) {
                                        // By person
                                        byPerson.put(person.getName(), 
                                            byPerson.getOrDefault(person.getName(), 0.0) + giftHistory.getCost());
                                        
                                        // By relation
                                        String relation = person.getRelation() != null ? person.getRelation() : "Unknown";
                                        byRelation.put(relation,
                                            byRelation.getOrDefault(relation, 0.0) + giftHistory.getCost());
                                    }
                                }
                            }
                            
                            spendingByPerson.postValue(byPerson);
                            spendingByRelation.postValue(byRelation);
                        });
                    });
                }
                
                @Override
                public void onError(Exception e) {
                    error.postValue("Failed to load chart data: " + e.getMessage());
                }
            }
        );
    }
    
    public void setBudget(long userId, double plannedBudget) {
        if (plannedBudget < 0) {
            error.setValue("Budget cannot be negative");
            return;
        }
        
        Budget budget = new Budget(userId, selectedMonth);
        budget.setPlannedBudget(plannedBudget);
        
        budgetRepository.insertOrUpdateBudget(budget, new BudgetRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                operationSuccess.postValue(true);
                error.postValue(null);
                loadBudgetData(userId);
            }
            
            @Override
            public void onError(Exception e) {
                error.postValue("Failed to set budget: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }
}


