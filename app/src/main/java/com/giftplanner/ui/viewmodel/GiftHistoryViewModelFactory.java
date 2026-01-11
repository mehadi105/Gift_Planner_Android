package com.giftplanner.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.giftplanner.data.repository.GiftHistoryRepository;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;

public class GiftHistoryViewModelFactory implements ViewModelProvider.Factory {
    private final GiftHistoryRepository giftHistoryRepository;
    private final OccasionsRepository occasionsRepository;
    private final PeopleRepository peopleRepository;
    
    public GiftHistoryViewModelFactory(GiftHistoryRepository giftHistoryRepository,
                                      OccasionsRepository occasionsRepository,
                                      PeopleRepository peopleRepository) {
        this.giftHistoryRepository = giftHistoryRepository;
        this.occasionsRepository = occasionsRepository;
        this.peopleRepository = peopleRepository;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GiftHistoryViewModel.class)) {
            return (T) new GiftHistoryViewModel(giftHistoryRepository, occasionsRepository, peopleRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


