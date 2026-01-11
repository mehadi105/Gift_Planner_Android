package com.giftplanner.ui.occasions;

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
import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.entity.Person;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;
import com.giftplanner.databinding.FragmentOccasionsBinding;
import com.giftplanner.ui.adapter.OccasionsAdapter;
import com.giftplanner.ui.viewmodel.OccasionsViewModel;
import com.giftplanner.ui.viewmodel.OccasionsViewModelFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OccasionsFragment extends Fragment {
    private FragmentOccasionsBinding binding;
    private OccasionsViewModel viewModel;
    private GiftPlannerApplication app;
    private OccasionsAdapter adapter;
    private Occasion selectedOccasion;
    private List<Person> peopleList = new ArrayList<>();
    private Map<Long, Person> personMap = new HashMap<>();
    private Calendar eventDateCalendar = Calendar.getInstance();
    private ArrayAdapter<String> personAdapter;
    private ArrayAdapter<String> filterPersonAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOccasionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        app = (GiftPlannerApplication) requireActivity().getApplication();
        long userId = app.getSessionManager().getUserId();
        
        PeopleRepository peopleRepository = new PeopleRepository(app.getDatabase().personDao());
        OccasionsRepository occasionsRepository = new OccasionsRepository(app.getDatabase().occasionDao());
        
        viewModel = new ViewModelProvider(this, new OccasionsViewModelFactory(
            occasionsRepository, peopleRepository
        )).get(OccasionsViewModel.class);
        
        setupRecyclerView();
        setupDropdowns();
        setupListeners();
        observeData();
        
        viewModel.loadPeople(userId);
        viewModel.loadOccasions(userId);
    }
    
    private void setupRecyclerView() {
        adapter = new OccasionsAdapter();
        adapter.setOnOccasionClickListener(occasion -> {
            selectedOccasion = occasion;
            viewModel.selectOccasion(occasion);
            loadOccasionIntoForm(occasion);
        });
        
        binding.rvOccasions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOccasions.setAdapter(adapter);
    }
    
    private void setupDropdowns() {
        // Event type dropdown
        String[] eventTypes = {"birthday", "anniversary", "custom"};
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            eventTypes
        );
        binding.actvEventType.setAdapter(eventTypeAdapter);
        binding.actvEventType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = eventTypes[position];
            if ("custom".equals(selectedType)) {
                binding.tilCustomEvent.setVisibility(View.VISIBLE);
            } else {
                binding.tilCustomEvent.setVisibility(View.GONE);
                binding.etCustomEvent.setText("");
            }
        });
        
        // Status dropdown
        String[] statuses = {"pending", "decided", "bought"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            statuses
        );
        binding.actvStatus.setAdapter(statusAdapter);
        
        // Person adapters (will be populated when people load)
        personAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
        binding.actvPerson.setAdapter(personAdapter);
        
        filterPersonAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
        binding.actvFilterPerson.setAdapter(filterPersonAdapter);
    }
    
    private void setupListeners() {
        binding.tvEventDate.setOnClickListener(v -> showDatePicker());
        
        binding.actvFilterPerson.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                // "All People" selected
                viewModel.clearPersonFilter();
            } else {
                Person person = peopleList.get(position - 1);
                viewModel.setPersonFilter(person.getId());
            }
            long userId = app.getSessionManager().getUserId();
            viewModel.loadOccasions(userId);
        });
        
        binding.btnAdd.setOnClickListener(v -> addOccasion());
        binding.btnUpdate.setOnClickListener(v -> updateOccasion());
        binding.btnDelete.setOnClickListener(v -> deleteOccasion());
        binding.btnClear.setOnClickListener(v -> clearForm());
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                eventDateCalendar.set(year, month, dayOfMonth);
                LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);
                String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                binding.tvEventDate.setText("Event Date: " + dateStr);
            },
            eventDateCalendar.get(Calendar.YEAR),
            eventDateCalendar.get(Calendar.MONTH),
            eventDateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void loadOccasionIntoForm(Occasion occasion) {
        // Set person
        Person person = personMap.get(occasion.getPersonId());
        if (person != null) {
            String personText = person.getName() + (person.getBirthday() != null ? " (" + person.getBirthday() + ")" : "");
            binding.actvPerson.setText(personText, false);
        }
        
        // Set event date
        if (occasion.getEventDate() != null && !occasion.getEventDate().isEmpty()) {
            binding.tvEventDate.setText("Event Date: " + occasion.getEventDate());
            LocalDate date = LocalDate.parse(occasion.getEventDate());
            eventDateCalendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        }
        
        // Set event type
        binding.actvEventType.setText(occasion.getEventType(), false);
        if ("custom".equals(occasion.getEventType())) {
            binding.tilCustomEvent.setVisibility(View.VISIBLE);
            binding.etCustomEvent.setText(occasion.getGiftIdea());
        } else {
            binding.tilCustomEvent.setVisibility(View.GONE);
        }
        
        // Set gift idea (if not custom)
        if (!"custom".equals(occasion.getEventType())) {
            binding.etGiftIdea.setText(occasion.getGiftIdea() != null ? occasion.getGiftIdea() : "");
        }
        
        // Set budget
        binding.etBudget.setText(String.valueOf(occasion.getBudget()));
        
        // Set status
        binding.actvStatus.setText(occasion.getGiftStatus(), false);
    }
    
    private void addOccasion() {
        long userId = app.getSessionManager().getUserId();
        Occasion occasion = createOccasionFromForm(userId);
        
        if (occasion != null) {
            viewModel.addOccasion(occasion);
        }
    }
    
    private void updateOccasion() {
        if (selectedOccasion == null) {
            Toast.makeText(requireContext(), "Please select an occasion to update", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Occasion occasion = createOccasionFromForm(selectedOccasion.getUserId());
        if (occasion != null) {
            occasion.setId(selectedOccasion.getId());
            viewModel.updateOccasion(occasion);
        }
    }
    
    private void deleteOccasion() {
        if (selectedOccasion == null) {
            Toast.makeText(requireContext(), "Please select an occasion to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Occasion")
            .setMessage("Are you sure you want to delete this occasion?")
            .setPositiveButton("Yes", (dialog, which) -> {
                viewModel.deleteOccasion(selectedOccasion);
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void clearForm() {
        binding.actvPerson.setText("", false);
        binding.tvEventDate.setText("Event Date");
        binding.actvEventType.setText("", false);
        binding.tilCustomEvent.setVisibility(View.GONE);
        binding.etCustomEvent.setText("");
        binding.etGiftIdea.setText("");
        binding.etBudget.setText("");
        binding.actvStatus.setText("pending", false);
        selectedOccasion = null;
        viewModel.clearSelection();
    }
    
    private Occasion createOccasionFromForm(long userId) {
        // Get selected person
        String personText = binding.actvPerson.getText().toString();
        if (personText.isEmpty()) {
            Toast.makeText(requireContext(), "Person is required", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        Long personId = null;
        for (Person person : peopleList) {
            String personDisplay = person.getName() + (person.getBirthday() != null ? " (" + person.getBirthday() + ")" : "");
            if (personDisplay.equals(personText)) {
                personId = person.getId();
                break;
            }
        }
        
        if (personId == null) {
            Toast.makeText(requireContext(), "Please select a valid person", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        // Get event date
        String eventDateText = binding.tvEventDate.getText().toString();
        String eventDate = null;
        if (eventDateText.startsWith("Event Date: ")) {
            eventDate = eventDateText.substring(13);
        } else {
            Toast.makeText(requireContext(), "Event date is required", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        // Get event type
        String eventType = binding.actvEventType.getText().toString();
        if (eventType.isEmpty()) {
            Toast.makeText(requireContext(), "Event type is required", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        // Get gift idea or custom event
        String giftIdea = null;
        if ("custom".equals(eventType)) {
            giftIdea = binding.etCustomEvent.getText().toString().trim();
            if (giftIdea.isEmpty()) {
                Toast.makeText(requireContext(), "Custom event name is required", Toast.LENGTH_SHORT).show();
                return null;
            }
        } else {
            giftIdea = binding.etGiftIdea.getText().toString().trim();
        }
        
        // Get budget
        double budget = 0.0;
        String budgetText = binding.etBudget.getText().toString().trim();
        if (!budgetText.isEmpty()) {
            try {
                budget = Double.parseDouble(budgetText);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        
        // Get status
        String status = binding.actvStatus.getText().toString();
        if (status.isEmpty()) {
            status = "pending";
        }
        
        Occasion occasion = new Occasion(userId, personId, eventDate, eventType);
        occasion.setGiftIdea(giftIdea);
        occasion.setBudget(budget);
        occasion.setGiftStatus(status);
        
        return occasion;
    }
    
    private void observeData() {
        viewModel.getPeople().observe(getViewLifecycleOwner(), people -> {
            if (people != null) {
                peopleList = people;
                personMap.clear();
                
                List<String> personNames = new ArrayList<>();
                personNames.add("All People");
                
                for (Person person : people) {
                    personMap.put(person.getId(), person);
                    String display = person.getName() + (person.getBirthday() != null ? " (" + person.getBirthday() + ")" : "");
                    personNames.add(display);
                }
                
                personAdapter.clear();
                personAdapter.addAll(personNames.subList(1, personNames.size()));
                
                filterPersonAdapter.clear();
                filterPersonAdapter.addAll(personNames);
            }
        });
        
        viewModel.getOccasions().observe(getViewLifecycleOwner(), occasions -> {
            adapter.setPersonMap(personMap);
            adapter.setOccasions(occasions);
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
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
