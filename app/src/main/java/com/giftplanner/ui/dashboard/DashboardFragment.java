package com.giftplanner.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.giftplanner.GiftPlannerApplication;
import com.giftplanner.R;
import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.entity.Person;
import com.giftplanner.data.repository.BudgetRepository;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;
import com.giftplanner.databinding.FragmentDashboardBinding;
import com.giftplanner.ui.adapter.UpcomingOccasionsAdapter;
import com.giftplanner.ui.viewmodel.DashboardViewModel;
import com.giftplanner.ui.viewmodel.DashboardViewModelFactory;
import com.giftplanner.util.DateFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private GiftPlannerApplication app;
    private UpcomingOccasionsAdapter adapter;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        app = (GiftPlannerApplication) requireActivity().getApplication();
        long userId = app.getSessionManager().getUserId();
        
        PeopleRepository peopleRepository = new PeopleRepository(app.getDatabase().personDao());
        OccasionsRepository occasionsRepository = new OccasionsRepository(app.getDatabase().occasionDao());
        BudgetRepository budgetRepository = new BudgetRepository(
            app.getDatabase().budgetDao(),
            app.getDatabase().giftHistoryDao()
        );
        
        viewModel = new ViewModelProvider(this, new DashboardViewModelFactory(
            peopleRepository, occasionsRepository, budgetRepository
        )).get(DashboardViewModel.class);
        
        setupRecyclerView();
        observeData();
        viewModel.loadDashboardData(userId);
    }
    
    private void setupRecyclerView() {
        adapter = new UpcomingOccasionsAdapter();
        binding.rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcomingEvents.setAdapter(adapter);
    }
    
    private void observeData() {
        viewModel.getTotalPeople().observe(getViewLifecycleOwner(), count -> 
            binding.tvTotalPeople.setText(String.valueOf(count))
        );
        
        viewModel.getUpcoming7Days().observe(getViewLifecycleOwner(), count -> 
            binding.tvUpcoming7.setText(String.valueOf(count))
        );
        
        viewModel.getUpcoming30Days().observe(getViewLifecycleOwner(), count -> 
            binding.tvUpcoming30.setText(String.valueOf(count))
        );
        
        viewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> 
            binding.tvMonthlyBudget.setText(DateFormatter.formatCurrency(budget))
        );
        
        viewModel.getMonthlySpent().observe(getViewLifecycleOwner(), spent -> 
            binding.tvMonthlySpent.setText(DateFormatter.formatCurrency(spent))
        );
        
        viewModel.getUpcomingOccasions().observe(getViewLifecycleOwner(), occasions -> {
            if (occasions != null && !occasions.isEmpty()) {
                // Load person data for occasions
                long userId = app.getSessionManager().getUserId();
                PeopleRepository peopleRepository = new PeopleRepository(app.getDatabase().personDao());
                peopleRepository.getAllPeopleSync(userId, new PeopleRepository.RepositoryCallback<List<Person>>() {
                    @Override
                    public void onSuccess(List<Person> people) {
                        Map<Long, Person> personMap = new HashMap<>();
                        for (Person person : people) {
                            personMap.put(person.getId(), person);
                        }
                        adapter.setPersonMap(personMap);
                        adapter.setOccasions(occasions);
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        adapter.setOccasions(occasions);
                    }
                });
            } else {
                adapter.setOccasions(occasions);
            }
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        menu.add(0, R.id.action_dashboardFragment_to_peopleFragment, 0, R.string.people_management);
        menu.add(0, R.id.action_dashboardFragment_to_occasionsFragment, 0, R.string.occasions_management);
        menu.add(0, R.id.action_dashboardFragment_to_giftHistoryFragment, 0, R.string.gift_history);
        menu.add(0, R.id.action_dashboardFragment_to_budgetFragment, 0, R.string.budget_management);
        menu.add(0, R.id.action_logout, 0, R.string.logout);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        } else if (id == R.id.action_dashboardFragment_to_peopleFragment ||
                   id == R.id.action_dashboardFragment_to_occasionsFragment ||
                   id == R.id.action_dashboardFragment_to_giftHistoryFragment ||
                   id == R.id.action_dashboardFragment_to_budgetFragment) {
            Navigation.findNavController(requireView()).navigate(id);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.confirm_logout)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                app.getSessionManager().clearSession();
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_dashboardFragment_to_loginFragment);
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
