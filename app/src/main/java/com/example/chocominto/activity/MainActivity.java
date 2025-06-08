package com.example.chocominto.activity;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chocominto.BuildConfig;
import com.example.chocominto.R;
import com.example.chocominto.data.api.request.ApiConfig;
import com.example.chocominto.data.api.request.ApiService;
import com.example.chocominto.data.api.response.WaniKaniResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ApiService apiService;
    private String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = ApiConfig.getApiService();
        apiKey = "Bearer " + BuildConfig.API_KEY;
        Log.d(TAG, "API Key: " + apiKey);

        getVocabularyData();
    }

    private void getVocabularyData() {
        Log.d(TAG, "Memulai request vocabulary data");

        apiService.getVocabularySubjects(apiKey, "vocabulary")
                .enqueue(new Callback<WaniKaniResponse>() {
                    @Override
                    public void onResponse(Call<WaniKaniResponse> call, Response<WaniKaniResponse> response) {
                        if (response.isSuccessful()) {
                            WaniKaniResponse waniKaniResponse = response.body();
                            Log.d(TAG, "API Call Berhasil");

                            if (waniKaniResponse != null) {
                                Log.d(TAG, "Data diterima: " + waniKaniResponse.toString());
                            } else {
                                Log.e(TAG, "Response body kosong");
                            }
                        } else {
                            Log.e(TAG, "API Call Gagal, code: " + response.code());
                            try {
                                Log.e(TAG, "Error: " + response.errorBody().string());
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error response", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<WaniKaniResponse> call, Throwable t) {
                        Log.e(TAG, "API Call Error: " + t.getMessage(), t);
                    }
                });
    }
}