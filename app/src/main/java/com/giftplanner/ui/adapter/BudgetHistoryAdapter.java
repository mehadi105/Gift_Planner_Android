package com.giftplanner.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.giftplanner.R;
import com.giftplanner.data.entity.Budget;
import com.giftplanner.util.DateFormatter;

import java.util.ArrayList;
import java.util.List;

public class BudgetHistoryAdapter extends RecyclerView.Adapter<BudgetHistoryAdapter.ViewHolder> {
    private List<Budget> budgets = new ArrayList<>();
    
    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        
        holder.tvMonth.setText(DateFormatter.formatMonthYear(budget.getMonth()));
        holder.tvPlanned.setText("Planned: " + DateFormatter.formatCurrency(budget.getPlannedBudget()));
        holder.tvActual.setText("Actual: " + DateFormatter.formatCurrency(budget.getActualSpent()));
        
        double difference = budget.getPlannedBudget() - budget.getActualSpent();
        holder.tvDifference.setText("Difference: " + DateFormatter.formatCurrency(difference));
        
        if (difference < 0) {
            holder.tvDifference.setTextColor(holder.itemView.getContext().getColor(R.color.error));
        } else {
            holder.tvDifference.setTextColor(holder.itemView.getContext().getColor(R.color.success));
        }
    }
    
    @Override
    public int getItemCount() {
        return budgets.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonth;
        TextView tvPlanned;
        TextView tvActual;
        TextView tvDifference;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tv_month);
            tvPlanned = itemView.findViewById(R.id.tv_planned);
            tvActual = itemView.findViewById(R.id.tv_actual);
            tvDifference = itemView.findViewById(R.id.tv_difference);
        }
    }
}


