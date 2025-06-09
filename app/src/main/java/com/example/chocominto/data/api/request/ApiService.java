package com.example.chocominto.data.api.request;

import com.example.chocominto.data.api.response.DataItem;
import com.example.chocominto.data.api.response.WaniKaniResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {

    @GET("subjects")
    Call<WaniKaniResponse> getVocabularySubjects(
            @Header("Authorization") String apiKey,
            @Query("types") String subjectType
    );

    @GET
    Call<WaniKaniResponse> getNextPage(
            @Url String url,
            @Header("Authorization") String apiKey
    );

    @GET("subjects/{id}")
    Call<DataItem> getVocabById(
            @Header("Authorization") String apiKey,
            @Path("id") int vocabId
    );
}
