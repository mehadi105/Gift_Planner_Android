package com.giftplanner.ui.people;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.giftplanner.data.entity.Person;
import com.giftplanner.data.repository.PeopleRepository;
import com.giftplanner.databinding.FragmentPeopleBinding;
import com.giftplanner.ui.adapter.PeopleAdapter;
import com.giftplanner.ui.viewmodel.PeopleViewModel;
import com.giftplanner.ui.viewmodel.PeopleViewModelFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class PeopleFragment extends Fragment {
    private FragmentPeopleBinding binding;
    private PeopleViewModel viewModel;
    private GiftPlannerApplication app;
    private PeopleAdapter adapter;
    private Person selectedPerson;
    private Calendar birthdayCalendar = Calendar.getInstance();
    private Calendar anniversaryCalendar = Calendar.getInstance();
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPeopleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        app = (GiftPlannerApplication) requireActivity().getApplication();
        long userId = app.getSessionManager().getUserId();
        
        PeopleRepository peopleRepository = new PeopleRepository(app.getDatabase().personDao());
        viewModel = new ViewModelProvider(this, new PeopleViewModelFactory(peopleRepository))
            .get(PeopleViewModel.class);
        
        setupRecyclerView();
        setupListeners();
        observeData();
        viewModel.loadPeople(userId);
    }
    
    private void setupRecyclerView() {
        adapter = new PeopleAdapter();
        adapter.setOnPersonClickListener(person -> {
            selectedPerson = person;
            viewModel.selectPerson(person);
            loadPersonIntoForm(person);
        });
        
        binding.rvPeople.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPeople.setAdapter(adapter);
    }
    
    private void setupListeners() {
        binding.tvBirthday.setOnClickListener(v -> showDatePicker(true));
        binding.tvAnniversary.setOnClickListener(v -> showDatePicker(false));
        
        binding.btnAdd.setOnClickListener(v -> addPerson());
        binding.btnUpdate.setOnClickListener(v -> updatePerson());
        binding.btnDelete.setOnClickListener(v -> deletePerson());
        binding.btnClear.setOnClickListener(v -> clearForm());
    }
    
    private void showDatePicker(boolean isBirthday) {
        Calendar calendar = isBirthday ? birthdayCalendar : anniversaryCalendar;
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);
                String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                
                if (isBirthday) {
                    binding.tvBirthday.setText("Birthday: " + dateStr);
                } else {
                    binding.tvAnniversary.setText("Anniversary: " + dateStr);
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void loadPersonIntoForm(Person person) {
        binding.etName.setText(person.getName());
        binding.etRelation.setText(person.getRelation() != null ? person.getRelation() : "");
        binding.etCustomEvent.setText(person.getCustomEvent() != null ? person.getCustomEvent() : "");
        binding.etNotes.setText(person.getNotes() != null ? person.getNotes() : "");
        
        if (person.getBirthday() != null && !person.getBirthday().isEmpty()) {
            binding.tvBirthday.setText("Birthday: " + person.getBirthday());
            LocalDate date = LocalDate.parse(person.getBirthday());
            birthdayCalendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        } else {
            binding.tvBirthday.setText("Birthday");
        }
        
        if (person.getAnniversary() != null && !person.getAnniversary().isEmpty()) {
            binding.tvAnniversary.setText("Anniversary: " + person.getAnniversary());
            LocalDate date = LocalDate.parse(person.getAnniversary());
            anniversaryCalendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        } else {
            binding.tvAnniversary.setText("Anniversary");
        }
    }
    
    private void addPerson() {
        long userId = app.getSessionManager().getUserId();
        Person person = createPersonFromForm(userId);
        
        if (person != null) {
            viewModel.addPerson(person);
        }
    }
    
    private void updatePerson() {
        if (selectedPerson == null) {
            Toast.makeText(requireContext(), "Please select a person to update", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Person person = createPersonFromForm(selectedPerson.getUserId());
        if (person != null) {
            person.setId(selectedPerson.getId());
            viewModel.updatePerson(person);
        }
    }
    
    private void deletePerson() {
        if (selectedPerson == null) {
            Toast.makeText(requireContext(), "Please select a person to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Person")
            .setMessage("Are you sure you want to delete " + selectedPerson.getName() + "? This will also delete all related occasions.")
            .setPositiveButton("Yes", (dialog, which) -> {
                viewModel.deletePerson(selectedPerson);
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void clearForm() {
        binding.etName.setText("");
        binding.etRelation.setText("");
        binding.etCustomEvent.setText("");
        binding.etNotes.setText("");
        binding.tvBirthday.setText("Birthday");
        binding.tvAnniversary.setText("Anniversary");
        selectedPerson = null;
        viewModel.clearSelection();
    }
    
    private Person createPersonFromForm(long userId) {
        String name = binding.etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        Person person = new Person(userId, name);
        person.setRelation(binding.etRelation.getText().toString().trim());
        person.setCustomEvent(binding.etCustomEvent.getText().toString().trim());
        person.setNotes(binding.etNotes.getText().toString().trim());
        
        String birthdayText = binding.tvBirthday.getText().toString();
        if (birthdayText.startsWith("Birthday: ")) {
            person.setBirthday(birthdayText.substring(10));
        }
        
        String anniversaryText = binding.tvAnniversary.getText().toString();
        if (anniversaryText.startsWith("Anniversary: ")) {
            person.setAnniversary(anniversaryText.substring(14));
        }
        
        return person;
    }
    
    private void observeData() {
        viewModel.getPeople().observe(getViewLifecycleOwner(), people -> {
            adapter.setPeople(people);
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
