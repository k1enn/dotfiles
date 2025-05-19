package com.example.elsa_speak_clone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        
        // Set rank with # symbol
        holder.tvRank.setText(String.valueOf(position + 1));
        
        // Set user data
        holder.tvUsername.setText(entry.getUsername());
        holder.tvXp.setText(String.valueOf(entry.getXp()));
        holder.tvStreak.setText(String.valueOf(entry.getStreak()));
        
        // Highlight the top 3 positions with medal background colors
        if (position == 0) {
            holder.tvRank.setBackgroundResource(R.drawable.rank_gold);
            holder.ivMedal.setVisibility(View.VISIBLE);
            holder.ivMedal.setImageResource(R.drawable.medal_gold);
        } else if (position == 1) {
            holder.tvRank.setBackgroundResource(R.drawable.rank_silver);
            holder.ivMedal.setVisibility(View.VISIBLE);
            holder.ivMedal.setImageResource(R.drawable.medal_silver);
        } else if (position == 2) {
            holder.tvRank.setBackgroundResource(R.drawable.rank_bronze);
            holder.ivMedal.setVisibility(View.VISIBLE);
            holder.ivMedal.setImageResource(R.drawable.medal_bronze);
        } else {
            holder.tvRank.setBackgroundResource(R.drawable.rounded_button);
            holder.ivMedal.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return entries.size();
    }
    
    public void updateData(List<LeaderboardEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        TextView tvUsername;
        TextView tvXpLabel;
        TextView tvXp;
        TextView tvStreakLabel;
        TextView tvStreak;
        ImageView ivMedal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvXpLabel = itemView.findViewById(R.id.tvXpLabel);
            tvXp = itemView.findViewById(R.id.tvXp);
            tvStreakLabel = itemView.findViewById(R.id.tvStreakLabel);
            tvStreak = itemView.findViewById(R.id.tvStreak);
            ivMedal = itemView.findViewById(R.id.ivMedal);
        }
    }
} 