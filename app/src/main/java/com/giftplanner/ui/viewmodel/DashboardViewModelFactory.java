package com.giftplanner.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.giftplanner.data.repository.BudgetRepository;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {
    private final PeopleRepository peopleRepository;
    private final OccasionsRepository occasionsRepository;
    private final BudgetRepository budgetRepository;
    
    public DashboardViewModelFactory(PeopleRepository peopleRepository,
                                    OccasionsRepository occasionsRepository,
                                    BudgetRepository budgetRepository) {
        this.peopleRepository = peopleRepository;
        this.occasionsRepository = occasionsRepository;
        this.budgetRepository = budgetRepository;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(peopleRepository, occasionsRepository, budgetRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


