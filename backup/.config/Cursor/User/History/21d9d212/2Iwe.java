package com.example.elsa_speak_clone.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.adapters.NewsAdapter;
import com.example.elsa_speak_clone.api.NewsApiClient;
import com.example.elsa_speak_clone.models.Article;
import com.example.elsa_speak_clone.models.NewsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {
    private static final String TAG = "NewsActivity";
    private static final String DEFAULT_COUNTRY = "us";
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvError;
    private NewsAdapter newsAdapter;
    private List<Article> articles = new ArrayList<>();
    private String currentSource = null; // null means we're using country filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Latest News");
        }
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewNews);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, articles);
        recyclerView.setAdapter(newsAdapter);
        
        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadNews);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.purple_500,
                R.color.purple_700,
                R.color.teal_200
        );
        
        // Load news
        loadNews();
    }
    
    private void loadNews() {
        showLoading();
        
        Call<NewsResponse> call;
        if (currentSource != null) {
            // Load news by source
            call = NewsApiClient.getInstance().getTopHeadlinesBySource(currentSource);
        } else {
            // Load news by country
            call = NewsApiClient.getInstance().getTopHeadlines(DEFAULT_COUNTRY);
        }
        
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    NewsResponse newsResponse = response.body();
                    
                    if ("ok".equals(newsResponse.getStatus()) && newsResponse.getArticles() != null) {
                        articles.clear();
                        articles.addAll(newsResponse.getArticles());
                        newsAdapter.updateArticles(articles);
                        showContent();
                    } else {
                        showError("Error loading news: " + newsResponse.getStatus());
                    }
                } else {
                    showError("Error: " + response.code());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                hideLoading();
                Log.e(TAG, "Error loading news", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showLoading() {
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
    }
    
    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }
    
    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }
    
    private void showError(String message) {
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_refresh) {
            loadNews();
            return true;
        } else if (id == R.id.action_us_news) {
            currentSource = null; // Use country filter
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("US News");
            }
            loadNews();
            return true;
        } else if (id == R.id.action_bbc_news) {
            currentSource = "bbc-news";
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("BBC News");
            }
            loadNews();
            return true;
        } else if (id == R.id.action_cnn_news) {
            currentSource = "cnn";
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("CNN News");
            }
            loadNews();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 