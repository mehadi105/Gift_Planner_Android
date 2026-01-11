package com.giftplanner.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.giftplanner.data.repository.BudgetRepository;
import com.giftplanner.data.repository.GiftHistoryRepository;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;

public class BudgetViewModelFactory implements ViewModelProvider.Factory {
    private final BudgetRepository budgetRepository;
    private final GiftHistoryRepository giftHistoryRepository;
    private final OccasionsRepository occasionsRepository;
    private final PeopleRepository peopleRepository;
    
    public BudgetViewModelFactory(BudgetRepository budgetRepository,
                                 GiftHistoryRepository giftHistoryRepository,
                                 OccasionsRepository occasionsRepository,
                                 PeopleRepository peopleRepository) {
        this.budgetRepository = budgetRepository;
        this.giftHistoryRepository = giftHistoryRepository;
        this.occasionsRepository = occasionsRepository;
        this.peopleRepository = peopleRepository;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BudgetViewModel.class)) {
            return (T) new BudgetViewModel(budgetRepository, giftHistoryRepository, 
                occasionsRepository, peopleRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


