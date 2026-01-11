package com.giftplanner.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.giftplanner.R;
import com.giftplanner.data.entity.GiftHistory;
import com.giftplanner.data.entity.Occasion;
import com.giftplanner.data.entity.Person;
import com.giftplanner.util.DateFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiftHistoryAdapter extends RecyclerView.Adapter<GiftHistoryAdapter.ViewHolder> {
    private List<GiftHistory> giftHistoryList = new ArrayList<>();
    private Map<Long, Occasion> occasionMap = new HashMap<>();
    private Map<Long, Person> personMap = new HashMap<>();
    private OnGiftHistoryClickListener listener;
    
    public interface OnGiftHistoryClickListener {
        void onGiftHistoryClick(GiftHistory giftHistory);
    }
    
    public void setGiftHistory(List<GiftHistory> giftHistory) {
        this.giftHistoryList = giftHistory != null ? giftHistory : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setOccasionMap(Map<Long, Occasion> occasionMap) {
        this.occasionMap = occasionMap != null ? occasionMap : new HashMap<>();
        notifyDataSetChanged();
    }
    
    public void setPersonMap(Map<Long, Person> personMap) {
        this.personMap = personMap != null ? personMap : new HashMap<>();
        notifyDataSetChanged();
    }
    
    public void setOnGiftHistoryClickListener(OnGiftHistoryClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_gift_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiftHistory giftHistory = giftHistoryList.get(position);
        Occasion occasion = occasionMap.get(giftHistory.getOccasionId());
        Person person = null;
        if (occasion != null) {
            person = personMap.get(occasion.getPersonId());
        }
        
        holder.tvDate.setText(DateFormatter.formatDateForDisplay(giftHistory.getGivenDate()));
        holder.tvPersonName.setText(person != null ? person.getName() : "Unknown");
        holder.tvGiftGiven.setText(giftHistory.getGiftGiven());
        holder.tvCost.setText(DateFormatter.formatCurrency(giftHistory.getCost()));
        
        if (giftHistory.getStore() != null && !giftHistory.getStore().isEmpty()) {
            holder.tvStore.setText("Store: " + giftHistory.getStore());
            holder.tvStore.setVisibility(View.VISIBLE);
        } else {
            holder.tvStore.setVisibility(View.GONE);
        }
        
        if (giftHistory.getLink() != null && !giftHistory.getLink().isEmpty()) {
            holder.tvLink.setText("Link: " + giftHistory.getLink());
            holder.tvLink.setVisibility(View.VISIBLE);
        } else {
            holder.tvLink.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGiftHistoryClick(giftHistory);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return giftHistoryList.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvPersonName;
        TextView tvGiftGiven;
        TextView tvCost;
        TextView tvStore;
        TextView tvLink;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvPersonName = itemView.findViewById(R.id.tv_person_name);
            tvGiftGiven = itemView.findViewById(R.id.tv_gift_given);
            tvCost = itemView.findViewById(R.id.tv_cost);
            tvStore = itemView.findViewById(R.id.tv_store);
            tvLink = itemView.findViewById(R.id.tv_link);
        }
    }
}


