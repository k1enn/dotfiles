package com.example.elsa_speak_clone.api;

import com.example.elsa_speak_clone.models.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {
    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country,
            @Query("apiKey") String apiKey
    );

    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlinesBySource(
            @Query("sources") String sources,
            @Query("apiKey") String apiKey
    );
} 