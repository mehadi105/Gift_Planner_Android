package com.giftplanner.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.giftplanner.R;
import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.entity.Person;
import com.giftplanner.util.DateFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OccasionsAdapter extends RecyclerView.Adapter<OccasionsAdapter.ViewHolder> {
    private List<Occasion> occasions = new ArrayList<>();
    private Map<Long, Person> personMap = new HashMap<>();
    private OnOccasionClickListener listener;
    
    public interface OnOccasionClickListener {
        void onOccasionClick(Occasion occasion);
    }
    
    public void setOccasions(List<Occasion> occasions) {
        this.occasions = occasions != null ? occasions : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setPersonMap(Map<Long, Person> personMap) {
        this.personMap = personMap != null ? personMap : new HashMap<>();
        notifyDataSetChanged();
    }
    
    public void setOnOccasionClickListener(OnOccasionClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_occasion, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Occasion occasion = occasions.get(position);
        Person person = personMap.get(occasion.getPersonId());
        
        holder.tvDate.setText(DateFormatter.formatDateForDisplay(occasion.getEventDate()));
        holder.tvPersonName.setText(person != null ? person.getName() : "Unknown");
        
        String eventType = occasion.getEventType();
        if ("custom".equals(eventType)) {
            holder.tvEventType.setText("Custom Event");
        } else {
            holder.tvEventType.setText(eventType.substring(0, 1).toUpperCase() + eventType.substring(1));
        }
        
        if (occasion.getGiftIdea() != null && !occasion.getGiftIdea().isEmpty()) {
            holder.tvGiftIdea.setText("Gift Idea: " + occasion.getGiftIdea());
            holder.tvGiftIdea.setVisibility(View.VISIBLE);
        } else {
            holder.tvGiftIdea.setVisibility(View.GONE);
        }
        
        holder.tvBudget.setText("Budget: " + DateFormatter.formatCurrency(occasion.getBudget()));
        
        String status = occasion.getGiftStatus();
        holder.tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOccasionClick(occasion);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return occasions.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvStatus;
        TextView tvPersonName;
        TextView tvEventType;
        TextView tvGiftIdea;
        TextView tvBudget;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvPersonName = itemView.findViewById(R.id.tv_person_name);
            tvEventType = itemView.findViewById(R.id.tv_event_type);
            tvGiftIdea = itemView.findViewById(R.id.tv_gift_idea);
            tvBudget = itemView.findViewById(R.id.tv_budget);
        }
    }
}


