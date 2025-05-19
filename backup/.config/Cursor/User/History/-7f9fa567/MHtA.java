package com.example.elsa_speak_clone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.models.LeaderboardEntry;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<LeaderboardEntry> entries;
    
    public LeaderboardAdapter(List<LeaderboardEntry> entries) {
        this.entries = entries;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);
        
        // Set position number (1-based)
        holder.tvRank.setText(String.valueOf(position + 1));
        
        // Set user data
        holder.tvUsername.setText(entry.getUsername());
        holder.tvXp.setText(String.valueOf(entry.getXp()));
        holder.tvStreak.setText(String.valueOf(entry.getStreak()));
    }
    
    @Override
    public int getItemCount() {
        return entries.size();
    }
    
    public void updateData(List<LeaderboardEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        TextView tvUsername;
        TextView tvXp;
        TextView tvStreak;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvXp = itemView.findViewById(R.id.tvXp);
            tvStreak = itemView.findViewById(R.id.tvStreak);
        }
    }
} 