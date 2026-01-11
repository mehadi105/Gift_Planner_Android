package com.giftplanner.ui.budget;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.giftplanner.GiftPlannerApplication;
import com.giftplanner.R;
import com.giftplanner.data.entity.Budget;
import com.giftplanner.data.repository.BudgetRepository;
import com.giftplanner.data.repository.GiftHistoryRepository;
import com.giftplanner.data.repository.OccasionsRepository;
import com.giftplanner.data.repository.PeopleRepository;
import com.giftplanner.databinding.FragmentBudgetBinding;
import com.giftplanner.ui.adapter.BudgetHistoryAdapter;
import com.giftplanner.ui.viewmodel.BudgetViewModel;
import com.giftplanner.ui.viewmodel.BudgetViewModelFactory;
import com.giftplanner.util.DateFormatter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetFragment extends Fragment {
    private FragmentBudgetBinding binding;
    private BudgetViewModel viewModel;
    private GiftPlannerApplication app;
    private BudgetHistoryAdapter adapter;
    private ArrayAdapter<String> monthAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        app = (GiftPlannerApplication) requireActivity().getApplication();
        long userId = app.getSessionManager().getUserId();
        
        BudgetRepository budgetRepository = new BudgetRepository(
            app.getDatabase().budgetDao(),
            app.getDatabase().giftHistoryDao()
        );
        GiftHistoryRepository giftHistoryRepository = new GiftHistoryRepository(app.getDatabase().giftHistoryDao());
        OccasionsRepository occasionsRepository = new OccasionsRepository(app.getDatabase().occasionDao());
        PeopleRepository peopleRepository = new PeopleRepository(app.getDatabase().personDao());
        
        viewModel = new ViewModelProvider(this, new BudgetViewModelFactory(
            budgetRepository, giftHistoryRepository, occasionsRepository, peopleRepository
        )).get(BudgetViewModel.class);
        
        setupRecyclerView();
        setupMonthSelector();
        setupCharts();
        setupListeners();
        observeData();
        
        viewModel.loadBudgetData(userId);
    }
    
    private void setupRecyclerView() {
        adapter = new BudgetHistoryAdapter();
        binding.rvBudgetHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBudgetHistory.setAdapter(adapter);
    }
    
    private void setupMonthSelector() {
        List<String> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = -2; i <= 2; i++) {
            LocalDate month = now.plusMonths(i);
            String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            months.add(DateFormatter.formatMonthYear(monthStr) + " (" + monthStr + ")");
        }
        
        monthAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, months);
        binding.actvMonth.setAdapter(monthAdapter);
        binding.actvMonth.setText(months.get(2), false); // Select current month
        
        binding.actvMonth.setOnItemClickListener((parent, view, position, id) -> {
            String selected = months.get(position);
            String monthStr = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")"));
            viewModel.setSelectedMonth(monthStr);
            long userId = app.getSessionManager().getUserId();
            viewModel.loadBudgetData(userId);
        });
    }
    
    private void setupCharts() {
        // Pie Chart setup
        PieChart pieChart = binding.pieChartPerson;
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.getLegend().setEnabled(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);
        
        // Bar Chart setup
        BarChart barChart = binding.barChartRelation;
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(true);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "$" + String.format("%.2f", value);
            }
        });
        barChart.getAxisRight().setEnabled(false);
    }
    
    private void setupListeners() {
        binding.btnSetBudget.setOnClickListener(v -> {
            String budgetText = binding.etBudget.getText().toString().trim();
            if (budgetText.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a budget amount", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                double budget = Double.parseDouble(budgetText);
                long userId = app.getSessionManager().getUserId();
                viewModel.setBudget(userId, budget);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void observeData() {
        viewModel.getCurrentBudget().observe(getViewLifecycleOwner(), budget -> {
            if (budget != null) {
                binding.tvPlannedBudget.setText("Planned Budget: " + DateFormatter.formatCurrency(budget.getPlannedBudget()));
            } else {
                binding.tvPlannedBudget.setText("Planned Budget: $0.00");
            }
        });
        
        viewModel.getActualSpent().observe(getViewLifecycleOwner(), spent -> {
            binding.tvActualSpent.setText("Actual Spent: " + DateFormatter.formatCurrency(spent));
            
            Budget budget = viewModel.getCurrentBudget().getValue();
            double planned = budget != null ? budget.getPlannedBudget() : 0.0;
            double remaining = planned - spent;
            binding.tvRemaining.setText("Remaining: " + DateFormatter.formatCurrency(remaining));
            
            // Update progress bar
            if (planned > 0) {
                int progress = (int) ((spent / planned) * 100);
                binding.progressBarBudget.setProgress(Math.min(progress, 100));
            } else {
                binding.progressBarBudget.setProgress(0);
            }
        });
        
        viewModel.getSpendingByPerson().observe(getViewLifecycleOwner(), spendingMap -> {
            if (spendingMap != null && !spendingMap.isEmpty()) {
                updatePieChart(spendingMap);
            } else {
                binding.pieChartPerson.clear();
            }
        });
        
        viewModel.getSpendingByRelation().observe(getViewLifecycleOwner(), spendingMap -> {
            if (spendingMap != null && !spendingMap.isEmpty()) {
                updateBarChart(spendingMap);
            } else {
                binding.barChartRelation.clear();
            }
        });
        
        viewModel.getBudgetHistory().observe(getViewLifecycleOwner(), budgets -> {
            adapter.setBudgets(budgets);
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        
        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                binding.etBudget.setText("");
                Toast.makeText(requireContext(), "Budget set successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updatePieChart(Map<String, Double> spendingMap) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : spendingMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "Spending by Person");
        dataSet.setColors(getChartColors(entries.size()));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        
        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "$" + String.format("%.2f", value);
            }
        });
        
        binding.pieChartPerson.setData(pieData);
        binding.pieChartPerson.invalidate();
    }
    
    private void updateBarChart(Map<String, Double> spendingMap) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        int index = 0;
        for (Map.Entry<String, Double> entry : spendingMap.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Spending by Relation");
        dataSet.setColors(getChartColors(entries.size()));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "$" + String.format("%.2f", value);
            }
        });
        
        binding.barChartRelation.setData(barData);
        binding.barChartRelation.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });
        binding.barChartRelation.invalidate();
    }
    
    private int[] getChartColors(int count) {
        int[] colors = {
            Color.parseColor("#FF5722"), Color.parseColor("#2196F3"), Color.parseColor("#4CAF50"),
            Color.parseColor("#FFC107"), Color.parseColor("#9C27B0"), Color.parseColor("#00BCD4"),
            Color.parseColor("#FF9800"), Color.parseColor("#795548"), Color.parseColor("#607D8B")
        };
        
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = colors[i % colors.length];
        }
        return result;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
