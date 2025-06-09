package com.example.chocominto.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chocominto.BuildConfig;
import com.example.chocominto.R;
import com.example.chocominto.data.api.request.ApiConfig;
import com.example.chocominto.data.api.request.ApiService;
import com.example.chocominto.data.api.response.DataItem;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.databinding.ActivityVocabDetailBinding;
import com.example.chocominto.utils.AudioHelper;
import com.example.chocominto.utils.VocabMapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VocabDetailActivity extends AppCompatActivity {
    private static final String TAG = "VocabDetailActivity";

    public static final int MODE_NORMAL = 0;
    public static final int MODE_REVIEW = 1;
    public static final int MODE_REVIEW_DETAIL = 2;
    public static final int MODE_LEARN = 3;
    public static final int MODE_QUIZ = 4;

    private static final String EXTRA_VOCAB_ID = "extra_vocab_id";
    private static final String EXTRA_MODE = "extra_mode";
    private static final String EXTRA_SOURCE = "extra_source";

    private ActivityVocabDetailBinding binding;
    private ApiService apiService;
    private int vocabId;
    private int mode;
    private String source;
    private Vocab vocab;

    public static Intent createIntent(Context context, int vocabId, int mode, String source) {
        Intent intent = new Intent(context, VocabDetailActivity.class);
        intent.putExtra(EXTRA_VOCAB_ID, vocabId);
        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_SOURCE, source);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vocabulary Detail");
        }

        vocabId = getIntent().getIntExtra(EXTRA_VOCAB_ID, -1);
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_NORMAL);
        source = getIntent().getStringExtra(EXTRA_SOURCE);

        if (vocabId == -1) {
            showError("Invalid vocabulary ID");
            return;
        }

        apiService = ApiConfig.getApiService();

        binding.btnPlayAudio.setOnClickListener(v -> playAudio());

        fetchVocabData();
    }

    private void fetchVocabData() {
        showLoading(true);

        String apiKey = "Bearer " + BuildConfig.API_KEY;

        apiService.getVocabById(apiKey, vocabId).enqueue(new Callback<DataItem>() {
            @Override
            public void onResponse(@NonNull Call<DataItem> call, @NonNull Response<DataItem> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Parse the response
                    vocab = VocabMapper.mapFromSingleSubject(response.body());
                    if (vocab != null) {
                        displayVocabData();
                        setupActionButtons();
                    } else {
                        showError("Failed to parse vocabulary data");
                    }
                } else {
                    String errorMsg = "Error: " + response.code();
                    showError(errorMsg);
                    Log.e(TAG, errorMsg + " " + response.message());

                    try {
                        Log.e(TAG, "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't read error body", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DataItem> call, @NonNull Throwable t) {
                showLoading(false);
                String errorMsg = "Connection Error: " + t.getMessage();
                showError(errorMsg);
                Log.e(TAG, errorMsg, t);
            }
        });
    }

    private void displayVocabData() {
        if (vocab == null) return;

        binding.tvLevel.setText(getString(R.string.level_format, vocab.getLevel()));
        binding.tvCharacter.setText(vocab.getCharacter());
        binding.tvReading.setText(vocab.getReading());
        binding.tvMeaning.setText(vocab.getMeaning());
        binding.tvPartOfSpeech.setText(vocab.getPartOfSpeech());
        binding.tvMeaningMnemonic.setText(vocab.getMeaningMnemonic());
        binding.tvReadingMnemonic.setText(vocab.getReadingMnemonic());

        binding.layoutExamples.removeAllViews();
        if (vocab.getContextSentences() != null && !vocab.getContextSentences().isEmpty()) {
            for (Vocab.ContextSentence sentence : vocab.getContextSentences()) {
                View sentenceView = LayoutInflater.from(this).inflate(
                        R.layout.item_context_sentence,
                        binding.layoutExamples,
                        false
                );

                sentenceView.findViewById(R.id.tvJapaneseSentence).setOnClickListener(v -> {
                });

                sentenceView.findViewById(R.id.tvJapaneseSentence)
                        .setTag(sentence); // For reference if needed

                ((android.widget.TextView) sentenceView.findViewById(R.id.tvJapaneseSentence))
                        .setText(sentence.getJa());

                ((android.widget.TextView) sentenceView.findViewById(R.id.tvEnglishSentence))
                        .setText(sentence.getEn());

                binding.layoutExamples.addView(sentenceView);
            }
        } else {
            android.widget.TextView noExamplesText = new android.widget.TextView(this);
            noExamplesText.setText("No example sentences available");
            noExamplesText.setPadding(0, 16, 0, 16);
            binding.layoutExamples.addView(noExamplesText);
        }
    }

    private void setupActionButtons() {
        binding.layoutActions.setVisibility(View.GONE);
    }

    private void playAudio() {
        if (vocab == null || vocab.getPronunciationAudios() == null ||
                vocab.getPronunciationAudios().isEmpty()) {
            Toast.makeText(this, "No audio available", Toast.LENGTH_SHORT).show();
            return;
        }

        String audioUrl = vocab.getPronunciationAudios().get(0).getUrl();

        AudioHelper.getInstance().playAudio(audioUrl);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.tvError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}