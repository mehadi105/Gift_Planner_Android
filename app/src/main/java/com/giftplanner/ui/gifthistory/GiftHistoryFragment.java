package com.giftplanner.ui.gifthistory;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.giftplanner.GiftPlannerApplication;
import com.giftplanner.R;
import com.giftplanner.data.entity.GiftHistory;
import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.entity.Person;
import com.giftplanner.data.repository.GiftHistoryRepository;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;
import com.giftplanner.databinding.FragmentGiftHistoryBinding;
import com.giftplanner.ui.adapter.GiftHistoryAdapter;
import com.giftplanner.ui.viewmodel.GiftHistoryViewModel;
import com.giftplanner.ui.viewmodel.GiftHistoryViewModelFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiftHistoryFragment extends Fragment {
    private FragmentGiftHistoryBinding binding;
    private GiftHistoryViewModel viewModel;
    private GiftPlannerApplication app;
    private GiftHistoryAdapter adapter;
    private GiftHistory selectedGiftHistory;
    private List<Occasion> occasionsList = new ArrayList<>();
    private List<Person> peopleList = new ArrayList<>();
    private Map<Long, Occasion> occasionMap = new HashMap<>();
    private Map<Long, Person> personMap = new HashMap<>();
    private Calendar givenDateCalendar = Calendar.getInstance();
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private ArrayAdapter<String> occasionAdapter;
    private ArrayAdapter<String> filterPersonAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGiftHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        app = (GiftPlannerApplication) requireActivity().getApplication();
        long userId = app.getSessionManager().getUserId();
        
        GiftHistoryRepository giftHistoryRepository = new GiftHistoryRepository(app.getDatabase().giftHistoryDao());
        OccasionsRepository occasionsRepository = new OccasionsRepository(app.getDatabase().occasionDao());
        PeopleRepository peopleRepository = new PeopleRepository(app.getDatabase().personDao());
        
        viewModel = new ViewModelProvider(this, new GiftHistoryViewModelFactory(
            giftHistoryRepository, occasionsRepository, peopleRepository
        )).get(GiftHistoryViewModel.class);
        
        setupRecyclerView();
        setupDropdowns();
        setupListeners();
        observeData();
        
        viewModel.loadGiftHistory(userId);
        viewModel.loadOccasions(userId);
        
        // Load people for filter
        peopleRepository.getAllPeople(userId).observe(getViewLifecycleOwner(), people -> {
            if (people != null) {
                peopleList = people;
                personMap.clear();
                List<String> personNames = new ArrayList<>();
                personNames.add("All People");
                for (Person person : people) {
                    personMap.put(person.getId(), person);
                    personNames.add(person.getName());
                }
                filterPersonAdapter.clear();
                filterPersonAdapter.addAll(personNames);
            }
        });
    }
    
    private void setupRecyclerView() {
        adapter = new GiftHistoryAdapter();
        adapter.setOnGiftHistoryClickListener(giftHistory -> {
            selectedGiftHistory = giftHistory;
            viewModel.selectGiftHistory(giftHistory);
            loadGiftHistoryIntoForm(giftHistory);
        });
        
        binding.rvGiftHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGiftHistory.setAdapter(adapter);
    }
    
    private void setupDropdowns() {
        occasionAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
        binding.actvOccasion.setAdapter(occasionAdapter);
        
        filterPersonAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
        binding.actvFilterPerson.setAdapter(filterPersonAdapter);
    }
    
    private void setupListeners() {
        binding.tvGivenDate.setOnClickListener(v -> showDatePicker(true));
        binding.tvStartDate.setOnClickListener(v -> showDatePicker(false));
        binding.tvEndDate.setOnClickListener(v -> showDatePicker(false));
        
        binding.actvFilterPerson.setOnItemClickListener((parent, view, position, id) -> {
            long userId = app.getSessionManager().getUserId();
            if (position == 0) {
                viewModel.clearPersonFilter();
            } else {
                Person person = peopleList.get(position - 1);
                viewModel.setPersonFilter(person.getId());
            }
            viewModel.loadOccasions(userId);
            viewModel.loadGiftHistory(userId);
        });
        
        binding.btnClearFilter.setOnClickListener(v -> {
            viewModel.clearPersonFilter();
            viewModel.clearDateRangeFilter();
            binding.actvFilterPerson.setText("", false);
            binding.tvStartDate.setText("Start Date");
            binding.tvEndDate.setText("End Date");
            long userId = app.getSessionManager().getUserId();
            viewModel.loadGiftHistory(userId);
        });
        
        binding.btnAdd.setOnClickListener(v -> addGiftHistory());
        binding.btnUpdate.setOnClickListener(v -> updateGiftHistory());
        binding.btnDelete.setOnClickListener(v -> deleteGiftHistory());
        binding.btnClear.setOnClickListener(v -> clearForm());
    }
    
    private void showDatePicker(boolean isGivenDate) {
        Calendar calendar = isGivenDate ? givenDateCalendar : 
            (binding.tvStartDate.hasFocus() ? startDateCalendar : endDateCalendar);
        TextView targetView = isGivenDate ? binding.tvGivenDate : 
            (binding.tvStartDate.hasFocus() ? binding.tvStartDate : binding.tvEndDate);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);
                String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                
                if (isGivenDate) {
                    targetView.setText("Given Date: " + dateStr);
                } else {
                    String label = targetView == binding.tvStartDate ? "Start Date: " : "End Date: ";
                    targetView.setText(label + dateStr);
                    
                    // Apply date range filter if both dates are set
                    String startText = binding.tvStartDate.getText().toString();
                    String endText = binding.tvEndDate.getText().toString();
                    if (startText.startsWith("Start Date: ") && endText.startsWith("End Date: ")) {
                        viewModel.setDateRangeFilter(
                            startText.substring(13),
                            endText.substring(11)
                        );
                        long userId = app.getSessionManager().getUserId();
                        viewModel.loadGiftHistory(userId);
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void loadGiftHistoryIntoForm(GiftHistory giftHistory) {
        Occasion occasion = occasionMap.get(giftHistory.getOccasionId());
        if (occasion != null) {
            Person person = personMap.get(occasion.getPersonId());
            String occasionText = (person != null ? person.getName() : "Unknown") + 
                " - " + occasion.getEventDate() + " (" + occasion.getEventType() + ")";
            binding.actvOccasion.setText(occasionText, false);
        }
        
        binding.etGiftGiven.setText(giftHistory.getGiftGiven());
        binding.etCost.setText(String.valueOf(giftHistory.getCost()));
        binding.etStore.setText(giftHistory.getStore() != null ? giftHistory.getStore() : "");
        binding.etLink.setText(giftHistory.getLink() != null ? giftHistory.getLink() : "");
        
        if (giftHistory.getGivenDate() != null && !giftHistory.getGivenDate().isEmpty()) {
            binding.tvGivenDate.setText("Given Date: " + giftHistory.getGivenDate());
            LocalDate date = LocalDate.parse(giftHistory.getGivenDate());
            givenDateCalendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        }
    }
    
    private void addGiftHistory() {
        long userId = app.getSessionManager().getUserId();
        GiftHistory giftHistory = createGiftHistoryFromForm(userId);
        
        if (giftHistory != null) {
            viewModel.addGiftHistory(giftHistory);
        }
    }
    
    private void updateGiftHistory() {
        if (selectedGiftHistory == null) {
            Toast.makeText(requireContext(), "Please select a gift history entry to update", Toast.LENGTH_SHORT).show();
            return;
        }
        
        GiftHistory giftHistory = createGiftHistoryFromForm(selectedGiftHistory.getUserId());
        if (giftHistory != null) {
            giftHistory.setId(selectedGiftHistory.getId());
            viewModel.updateGiftHistory(giftHistory);
        }
    }
    
    private void deleteGiftHistory() {
        if (selectedGiftHistory == null) {
            Toast.makeText(requireContext(), "Please select a gift history entry to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Gift History")
            .setMessage("Are you sure you want to delete this gift history entry?")
            .setPositiveButton("Yes", (dialog, which) -> {
                viewModel.deleteGiftHistory(selectedGiftHistory);
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void clearForm() {
        binding.actvOccasion.setText("", false);
        binding.etGiftGiven.setText("");
        binding.etCost.setText("");
        binding.etStore.setText("");
        binding.etLink.setText("");
        binding.tvGivenDate.setText("Given Date");
        selectedGiftHistory = null;
        viewModel.clearSelection();
    }
    
    private GiftHistory createGiftHistoryFromForm(long userId) {
        // Get selected occasion
        String occasionText = binding.actvOccasion.getText().toString();
        if (occasionText.isEmpty()) {
            Toast.makeText(requireContext(), "Occasion is required", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        Long occasionId = null;
        for (Occasion occasion : occasionsList) {
            Person person = personMap.get(occasion.getPersonId());
            String display = (person != null ? person.getName() : "Unknown") + 
                " - " + occasion.getEventDate() + " (" + occasion.getEventType() + ")";
            if (display.equals(occasionText)) {
                occasionId = occasion.getId();
                break;
            }
        }
        
        if (occasionId == null) {
            Toast.makeText(requireContext(), "Please select a valid occasion", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        // Get gift given
        String giftGiven = binding.etGiftGiven.getText().toString().trim();
        if (giftGiven.isEmpty()) {
            Toast.makeText(requireContext(), "Gift given is required", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        // Get cost
        double cost = 0.0;
        String costText = binding.etCost.getText().toString().trim();
        if (costText.isEmpty()) {
            Toast.makeText(requireContext(), "Cost is required", Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            cost = Double.parseDouble(costText);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid cost amount", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        // Get given date
        String givenDateText = binding.tvGivenDate.getText().toString();
        String givenDate;
        if (givenDateText.startsWith("Given Date: ")) {
            givenDate = givenDateText.substring(13);
        } else {
            givenDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        GiftHistory giftHistory = new GiftHistory(userId, occasionId, giftGiven, cost, givenDate);
        giftHistory.setStore(binding.etStore.getText().toString().trim());
        giftHistory.setLink(binding.etLink.getText().toString().trim());
        
        return giftHistory;
    }
    
    private void observeData() {
        viewModel.getOccasions().observe(getViewLifecycleOwner(), occasions -> {
            if (occasions != null) {
                occasionsList = occasions;
                occasionMap.clear();
                
                List<String> occasionDisplays = new ArrayList<>();
                for (Occasion occasion : occasions) {
                    occasionMap.put(occasion.getId(), occasion);
                    Person person = personMap.get(occasion.getPersonId());
                    String display = (person != null ? person.getName() : "Unknown") + 
                        " - " + occasion.getEventDate() + " (" + occasion.getEventType() + ")";
                    occasionDisplays.add(display);
                }
                
                occasionAdapter.clear();
                occasionAdapter.addAll(occasionDisplays);
            }
        });
        
        viewModel.getGiftHistory().observe(getViewLifecycleOwner(), giftHistory -> {
            adapter.setOccasionMap(occasionMap);
            adapter.setPersonMap(personMap);
            adapter.setGiftHistory(giftHistory);
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        
        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                clearForm();
                Toast.makeText(requireContext(), "Operation successful", Toast.LENGTH_SHORT).show();
                long userId = app.getSessionManager().getUserId();
                viewModel.loadGiftHistory(userId);
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
