package com.example.elsa_speak_clone.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.models.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private Context context;
    private List<Article> articles;
    
    // Format for converting ISO 8601 date to a more readable format
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
    
    public NewsAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article article = articles.get(position);
        
        // Set article title
        holder.tvTitle.setText(article.getTitle());
        
        // Set article description
        if (article.getDescription() != null && !article.getDescription().isEmpty()) {
            holder.tvDescription.setText(article.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }
        
        // Set article source
        if (article.getSource() != null && article.getSource().getName() != null) {
            holder.tvSource.setText(article.getSource().getName());
        } else {
            holder.tvSource.setText(R.string.unknown_source);
        }
        
        // Set article published date
        if (article.getPublishedAt() != null) {
            try {
                Date date = inputFormat.parse(article.getPublishedAt());
                holder.tvPublishedAt.setText(outputFormat.format(date));
            } catch (ParseException e) {
                holder.tvPublishedAt.setText(article.getPublishedAt());
            }
        }
        
        // Load article image
        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Glide.with(context)
                    .load(article.getUrlToImage())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.ivArticleImage);
            holder.ivArticleImage.setVisibility(View.VISIBLE);
        } else {
            holder.ivArticleImage.setVisibility(View.GONE);
        }
        
        // Set click listener to open the article URL
        holder.cardView.setOnClickListener(v -> {
            if (article.getUrl() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(article.getUrl()));
                context.startActivity(intent);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return articles != null ? articles.size() : 0;
    }
    
    public void updateArticles(List<Article> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }
    
    static class NewsViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvSource;
        TextView tvPublishedAt;
        ImageView ivArticleImage;
        
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvPublishedAt = itemView.findViewById(R.id.tvPublishedAt);
            ivArticleImage = itemView.findViewById(R.id.ivArticleImage);
        }
    }
} 