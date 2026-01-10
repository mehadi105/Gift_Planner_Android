package com.giftplanner.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.giftplanner.R;
import com.giftplanner.data.entity.Person;

import java.util.ArrayList;
import java.util.List;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {
    private List<Person> people = new ArrayList<>();
    private OnPersonClickListener listener;
    
    public interface OnPersonClickListener {
        void onPersonClick(Person person);
    }
    
    public void setPeople(List<Person> people) {
        this.people = people != null ? people : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setOnPersonClickListener(OnPersonClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_person, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Person person = people.get(position);
        
        String displayText = person.getName();
        if (person.getRelation() != null && !person.getRelation().isEmpty()) {
            displayText += " (" + person.getRelation() + ")";
        }
        holder.tvPersonName.setText(displayText);
        
        if (person.getRelation() != null && !person.getRelation().isEmpty()) {
            holder.tvPersonRelation.setText(person.getRelation());
            holder.tvPersonRelation.setVisibility(View.VISIBLE);
        } else {
            holder.tvPersonRelation.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPersonClick(person);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return people.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPersonName;
        TextView tvPersonRelation;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvPersonName = itemView.findViewById(R.id.tv_person_name);
            tvPersonRelation = itemView.findViewById(R.id.tv_person_relation);
        }
    }
}


