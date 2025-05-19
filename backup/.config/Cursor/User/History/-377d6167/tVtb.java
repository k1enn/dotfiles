package com.example.elsa_speak_clone.api;

import com.example.elsa_speak_clone.models.NewsResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsApiClient {
    private static final String BASE_URL = "https://newsapi.org/v2/";
    private static final String API_KEY = "59f053d29ae04e0686c904d704155655"; // Replace with your actual API key
    
    private static NewsApiClient instance;
    private NewsApiService newsApiService;
    
    private NewsApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        newsApiService = retrofit.create(NewsApiService.class);
    }
    
    public static synchronized NewsApiClient getInstance() {
        if (instance == null) {
            instance = new NewsApiClient();
        }
        return instance;
    }
    
    public Call<NewsResponse> getTopHeadlines(String country) {
        return newsApiService.getTopHeadlines(country, API_KEY);
    }
    
    public Call<NewsResponse> getTopHeadlinesBySource(String source) {
        return newsApiService.getTopHeadlinesBySource(source, API_KEY);
    }
} 