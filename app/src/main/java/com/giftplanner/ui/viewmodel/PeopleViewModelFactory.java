package com.giftplanner.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.giftplanner.data.repository.PeopleRepository;

public class PeopleViewModelFactory implements ViewModelProvider.Factory {
    private final PeopleRepository peopleRepository;
    
    public PeopleViewModelFactory(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(PeopleViewModel.class)) {
            return (T) new PeopleViewModel(peopleRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


