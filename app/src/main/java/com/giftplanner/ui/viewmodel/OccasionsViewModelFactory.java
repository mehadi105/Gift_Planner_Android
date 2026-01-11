package com.giftplanner.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;

public class OccasionsViewModelFactory implements ViewModelProvider.Factory {
    private final OccasionsRepository occasionsRepository;
    private final PeopleRepository peopleRepository;
    
    public OccasionsViewModelFactory(OccasionsRepository occasionsRepository, PeopleRepository peopleRepository) {
        this.occasionsRepository = occasionsRepository;
        this.peopleRepository = peopleRepository;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(OccasionsViewModel.class)) {
            return (T) new OccasionsViewModel(occasionsRepository, peopleRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


