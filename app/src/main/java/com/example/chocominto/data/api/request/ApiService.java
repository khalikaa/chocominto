package com.example.chocominto.data.api.request;

import com.example.chocominto.data.api.response.WaniKaniResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiService {

    @GET("subjects")
    Call<WaniKaniResponse> getVocabularySubjects(
            @Header("Authorization") String apiKey,
            @Query("types") String subjectType
    );
}
