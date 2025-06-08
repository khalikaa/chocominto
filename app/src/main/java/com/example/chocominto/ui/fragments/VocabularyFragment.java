package com.example.chocominto.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chocominto.BuildConfig;
import com.example.chocominto.R;
import com.example.chocominto.data.api.request.ApiConfig;
import com.example.chocominto.data.api.request.ApiService;
import com.example.chocominto.data.api.response.WaniKaniResponse;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.adapters.VocabularyAdapter;
import com.example.chocominto.utils.VocabMapper;
import com.example.chocominto.utils.AudioHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VocabularyFragment extends Fragment {

    private static final String TAG = "VocabularyFragment";
    private RecyclerView recyclerView;
    private VocabularyAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ApiService apiService;
    private String nextUrl;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vocabulary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AudioHelper.getInstance().setContext(requireContext());
        Log.d(TAG, "AudioHelper initialized");

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new VocabularyAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        setupPagination(layoutManager);

        apiService = ApiConfig.getApiService();
        loadVocabularyData(null); // Initial load
    }

    private void setupPagination(LinearLayoutManager layoutManager) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && nextUrl != null) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreVocabulary();
                    }
                }
            }
        });
    }

    private void navigateToDetailScreen(Vocab item) {
        Toast.makeText(requireContext(), "Selected: " + item.getCharacter(), Toast.LENGTH_SHORT).show();

        // Debug audio URLs
        Log.d(TAG, "Selected vocabulary: " + item.getCharacter());
        if (item.getPronunciationAudios() != null) {
            for (Vocab.PronunciationAudio audio : item.getPronunciationAudios()) {
                Log.d(TAG, String.format("Audio: gender=%s, type=%s, url=%s",
                        audio.getGender(), audio.getContentType(), audio.getUrl()));
            }
        } else {
            Log.d(TAG, "No audio available for this item");
        }
    }

    private void loadVocabularyData(String url) {
        showLoading(true);
        isLoading = true;

        String apiKey = "Bearer " + BuildConfig.API_KEY;
        Log.d(TAG, "Loading vocabulary data" + (url == null ? " (initial)" : " (pagination)"));

        Call<WaniKaniResponse> call;
        if (url == null) {
            call = apiService.getVocabularySubjects(apiKey, "vocabulary");
        } else {
            call = apiService.getNextPage(url, apiKey);
        }

        call.enqueue(new Callback<WaniKaniResponse>() {
            @Override
            public void onResponse(@NonNull Call<WaniKaniResponse> call, @NonNull Response<WaniKaniResponse> response) {
                showLoading(false);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    // Log response info
                    Log.d(TAG, "API call successful, received data");

                    // Simpan next URL untuk pagination
                    if (response.body().getPages() != null) {
                        nextUrl = response.body().getPages().getNextUrl();
                        Log.d(TAG, "Next URL: " + nextUrl);
                    }

                    // Parse response dan convert ke list Vocab
                    List<Vocab> Vocabs = VocabMapper.mapFromResponse(response.body());
                    Log.d(TAG, "Mapped " + Vocabs.size() + " vocabulary items");

                    if (url == null) {
                        // Initial load
                        adapter.setVocabularyList(Vocabs);
                    } else {
                        // Load more - add to existing list
                        adapter.addVocabs(Vocabs);
                    }

                    showEmptyView(adapter.getItemCount() == 0);
                } else {
                    String errorMsg = "Error: " + response.code();
                    showError(errorMsg);
                    showEmptyView(adapter.getItemCount() == 0);
                    Log.e(TAG, errorMsg + " " + response.message());

                    try {
                        Log.e(TAG, "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't read error body", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WaniKaniResponse> call, @NonNull Throwable t) {
                showLoading(false);
                isLoading = false;
                String errorMsg = "Connection Error: " + t.getMessage();
                showError(errorMsg);
                showEmptyView(adapter.getItemCount() == 0);
                Log.e(TAG, errorMsg, t);
            }
        });
    }

    private void loadMoreVocabulary() {
        if (nextUrl != null && !isLoading) {
            loadVocabularyData(nextUrl);
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyView(boolean isEmpty) {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AudioHelper.getInstance().release();
        Log.d(TAG, "AudioHelper resources released");
    }
}