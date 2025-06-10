package com.example.chocominto.data.repository;

import android.util.Log;

import com.example.chocominto.BuildConfig;
import com.example.chocominto.data.api.request.ApiConfig;
import com.example.chocominto.data.api.request.ApiService;
import com.example.chocominto.data.api.response.DataItem;
import com.example.chocominto.data.api.response.WaniKaniResponse;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.utils.VocabMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VocabRepository {
    private static final String TAG = "VocabRepository";
    private static VocabRepository instance;

    private ApiService apiService;
    private String apiKey;

    private List<Vocab> cachedVocabList = null;
    private String nextPageUrl = null;

    // Cache for individual vocab items
    private Map<Integer, Vocab> vocabDetailsCache = new HashMap<>();

    private VocabRepository() {
        apiService = ApiConfig.getApiService();
        apiKey = "Bearer " + BuildConfig.API_KEY;
    }

    public static synchronized VocabRepository getInstance() {
        if (instance == null) {
            instance = new VocabRepository();
        }
        return instance;
    }

    public interface VocabListCallback {
        void onSuccess(List<Vocab> vocabList, String nextUrl);
        void onError(String message);
    }

    public interface VocabDetailCallback {
        void onSuccess(Vocab vocab);
        void onError(String message);
    }

    public boolean hasVocabListCache() {
        return cachedVocabList != null && !cachedVocabList.isEmpty();
    }

    public void getVocabList(final VocabListCallback callback, boolean forceRefresh) {
        // Return cached data if available and not forcing refresh
        if (!forceRefresh && hasVocabListCache()) {
            Log.d(TAG, "Returning cached vocab list with " + cachedVocabList.size() + " items");
            callback.onSuccess(cachedVocabList, nextPageUrl);
            return;
        }

        // Otherwise fetch from API
        Log.d(TAG, "Fetching vocab list from API");
        apiService.getVocabularySubjects(apiKey, "vocabulary").enqueue(new Callback<WaniKaniResponse>() {
            @Override
            public void onResponse(Call<WaniKaniResponse> call, Response<WaniKaniResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save to cache
                    cachedVocabList = VocabMapper.mapFromResponse(response.body());
                    nextPageUrl = response.body().getPages() != null ? response.body().getPages().getNextUrl() : null;

                    Log.d(TAG, "Saved " + cachedVocabList.size() + " vocab items to cache");
                    callback.onSuccess(cachedVocabList, nextPageUrl);
                } else {
                    String errorMsg = "Error: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<WaniKaniResponse> call, Throwable t) {
                String errorMsg = "Connection Error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    public void getNextVocabPage(String url, final VocabListCallback callback) {
        if (url == null || url.isEmpty()) {
            callback.onError("No more pages available");
            return;
        }

        Log.d(TAG, "Fetching next page from: " + url);
        apiService.getNextPage(url, apiKey).enqueue(new Callback<WaniKaniResponse>() {
            @Override
            public void onResponse(Call<WaniKaniResponse> call, Response<WaniKaniResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Vocab> newItems = VocabMapper.mapFromResponse(response.body());
                    nextPageUrl = response.body().getPages() != null ? response.body().getPages().getNextUrl() : null;

                    // Add to cache
                    if (cachedVocabList == null) {
                        cachedVocabList = new ArrayList<>();
                    }
                    cachedVocabList.addAll(newItems);

                    Log.d(TAG, "Added " + newItems.size() + " items to cache, total: " + cachedVocabList.size());
                    callback.onSuccess(newItems, nextPageUrl);
                } else {
                    String errorMsg = "Error: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<WaniKaniResponse> call, Throwable t) {
                String errorMsg = "Connection Error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    public void getVocabDetail(int vocabId, final VocabDetailCallback callback) {
        // Check cache first
        if (vocabDetailsCache.containsKey(vocabId)) {
            Log.d(TAG, "Returning cached vocab detail for ID: " + vocabId);
            callback.onSuccess(vocabDetailsCache.get(vocabId));
            return;
        }

        // Not in cache, fetch from API
        Log.d(TAG, "Fetching vocab detail for ID: " + vocabId);
        apiService.getVocabById(apiKey, vocabId).enqueue(new Callback<DataItem>() {
            @Override
            public void onResponse(Call<DataItem> call, Response<DataItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Vocab vocab = VocabMapper.mapFromSingleSubject(response.body());

                    if (vocab != null) {
                        // Save to cache
                        vocabDetailsCache.put(vocabId, vocab);
                        Log.d(TAG, "Saved vocab detail to cache: " + vocab.getCharacter());
                        callback.onSuccess(vocab);
                    } else {
                        callback.onError("Failed to parse vocabulary data");
                    }
                } else {
                    String errorMsg = "Error: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<DataItem> call, Throwable t) {
                String errorMsg = "Connection Error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    // Add this method to your VocabRepository class
    public void getVocabByLevelRange(int startLevel, int endLevel, final VocabListCallback callback) {
        // First check if we already have the full list cached
        if (hasVocabListCache()) {
            // Filter the cached list by level range
            List<Vocab> filteredList = new ArrayList<>();
            for (Vocab vocab : cachedVocabList) {
                if (vocab.getLevel() >= startLevel && vocab.getLevel() <= endLevel) {
                    filteredList.add(vocab);
                }
            }

            Log.d(TAG, "Returning " + filteredList.size() + " vocab items for levels " +
                    startLevel + "-" + endLevel + " from cache");
            callback.onSuccess(filteredList, null);
            return;
        }

        // If no cache, fetch all items first and then filter
        getVocabList(new VocabListCallback() {
            @Override
            public void onSuccess(List<Vocab> vocabList, String nextUrl) {
                // Now filter by level range
                List<Vocab> filteredList = new ArrayList<>();
                for (Vocab vocab : vocabList) {
                    if (vocab.getLevel() >= startLevel && vocab.getLevel() <= endLevel) {
                        filteredList.add(vocab);
                    }
                }

                Log.d(TAG, "Fetched and filtered " + filteredList.size() +
                        " vocab items for levels " + startLevel + "-" + endLevel);
                callback.onSuccess(filteredList, null);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        }, false);
    }

    public void clearCache() {
        cachedVocabList = null;
        vocabDetailsCache.clear();
        nextPageUrl = null;
        Log.d(TAG, "Cache cleared");
    }
}