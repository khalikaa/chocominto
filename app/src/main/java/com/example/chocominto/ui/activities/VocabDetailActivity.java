package com.example.chocominto.ui.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chocominto.R;
import com.example.chocominto.data.database.ContextSentenceHelper;
import com.example.chocominto.data.database.DatabaseContract;
import com.example.chocominto.data.database.VocabHelper;
import com.example.chocominto.data.manager.LearnManager;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.data.repository.VocabRepository;
import com.example.chocominto.databinding.ActivityVocabDetailBinding;
import com.example.chocominto.utils.AudioHelper;
import com.example.chocominto.utils.MappingHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class VocabDetailActivity extends AppCompatActivity {
    private static final String TAG = "VocabDetailActivity";

    public static final int MODE_NORMAL = 0;
    public static final int MODE_REVIEW_DETAIL = 2;
    public static final int MODE_LEARN = 3;

    private static final String EXTRA_VOCAB_ID = "extra_vocab_id";
    private static final String EXTRA_MODE = "extra_mode";
    private static final String EXTRA_SOURCE = "extra_source";
    private static final String EXTRA_START_LEVEL = "extra_start_level";
    private static final String EXTRA_END_LEVEL = "extra_end_level";

    private ActivityVocabDetailBinding binding;
    private VocabRepository repository;
    private LearnManager learnManager;
    private int vocabId = -1;
    private int mode;
    String source;
    private Vocab vocab;

    private int startLevel = -1;
    private int endLevel = -1;

    private boolean isWordSelected = false;

    public static Intent createIntent(Context context, int vocabId, int mode, String source) {
        Intent intent = new Intent(context, VocabDetailActivity.class);
        intent.putExtra(EXTRA_VOCAB_ID, vocabId);
        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_SOURCE, source);
        return intent;
    }

    public static Intent createIntentForLearn(Context context, int startLevel, int endLevel) {
        Intent intent = new Intent(context, VocabDetailActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_LEARN);
        intent.putExtra(EXTRA_START_LEVEL, startLevel);
        intent.putExtra(EXTRA_END_LEVEL, endLevel);
        intent.putExtra(EXTRA_SOURCE, "learn_fragment");
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
            getSupportActionBar().setTitle(getTitleForMode());
        }

        vocabId = getIntent().getIntExtra(EXTRA_VOCAB_ID, -1);
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_NORMAL);
        source = getIntent().getStringExtra(EXTRA_SOURCE);
        startLevel = getIntent().getIntExtra(EXTRA_START_LEVEL, -1);
        endLevel = getIntent().getIntExtra(EXTRA_END_LEVEL, -1);

        repository = VocabRepository.getInstance();
        learnManager = LearnManager.getInstance(this);

        binding.btnPlayAudio.setOnClickListener(v -> playAudio());

        if (vocabId != -1) {
            isWordSelected = learnManager.isWordSelected(vocabId);
            if (mode == MODE_REVIEW_DETAIL) {
                loadVocabFromDatabase(vocabId);
            } else {
                loadSpecificVocab(vocabId);
            }
        } else if (startLevel > 0 && endLevel > 0) {
            loadRandomVocabFromRange(startLevel, endLevel);
        } else {
            showError("Invalid parameters");
        }
    }

    private void loadSpecificVocab(int id) {
        showLoading(true);

        repository.getVocabDetail(id, new VocabRepository.VocabDetailCallback() {
            @Override
            public void onSuccess(Vocab vocabItem) {
                showLoading(false);
                vocab = vocabItem;

                if (vocab != null) {
                    vocabId = vocab.getId();
                    displayVocabData();
                    setupActionButtons();
                } else {
                    showError("Failed to parse vocabulary data");
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showError(message);
            }
        });
    }
    private void deleteVocabFromDatabase() {
        if (vocab == null) {
            Toast.makeText(this, "No vocabulary data", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Vocabulary")
                .setMessage("Are you sure you want to delete '" + vocab.getCharacter() + "' from your learned vocabulary?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    showLoading(true);
                    binding.tvError.setText("Deleting vocabulary...");
                    binding.tvError.setVisibility(View.VISIBLE);

                    new Thread(() -> {
                        try {
                            VocabHelper vocabHelper = VocabHelper.getInstance(this);
                            ContextSentenceHelper contextHelper = ContextSentenceHelper.getInstance(this);

                            vocabHelper.open();
                            contextHelper.open();

                            contextHelper.deleteContextSentencesByVocabId(String.valueOf(vocab.getId()));

                            boolean success = vocabHelper.deleteVocabById(String.valueOf(vocab.getId()));

                            contextHelper.close();
                            vocabHelper.close();

                            runOnUiThread(() -> {
                                showLoading(false);
                                if (success) {
                                    Toast.makeText(this, "Vocabulary deleted successfully", Toast.LENGTH_SHORT).show();

                                    finish();
                                } else {
                                    Toast.makeText(this, "Failed to delete vocabulary", Toast.LENGTH_SHORT).show();
                                    binding.contentContainer.setVisibility(View.VISIBLE);
                                    binding.tvError.setVisibility(View.GONE);
                                }
                            });

                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting vocab from database", e);
                            runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void loadRandomVocabFromRange(int startLevel, int endLevel) {
        showLoading(true);

        binding.tvError.setText("Finding a word for you to learn...");
        binding.tvError.setVisibility(View.VISIBLE);

        repository.getVocabByLevelRange(startLevel, endLevel, new VocabRepository.VocabListCallback() {
            @Override
            public void onSuccess(List<Vocab> vocabList, String nextUrl) {
                if (vocabList.isEmpty()) {
                    showLoading(false);
                    showError("No vocabulary found for levels " + startLevel + "-" + endLevel);
                    return;
                }

                Vocab randomVocab = findUnselectedWord(vocabList);

                if (randomVocab == null) {
                    showLoading(false);
                    showError("You've already selected all words in this level range!");
                    return;
                }

                vocabId = randomVocab.getId();
                isWordSelected = learnManager.isWordSelected(vocabId);
                loadSpecificVocab(vocabId);
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showError("Failed to load vocabulary: " + message);
            }
        });
    }

    private Vocab findUnselectedWord(List<Vocab> vocabList) {
        List<Vocab> shuffled = new ArrayList<>(vocabList);
        java.util.Collections.shuffle(shuffled);

        for (Vocab vocab : shuffled) {
            if (!learnManager.isWordSelected(vocab.getId())) {
                return vocab;
            }
        }

        return null;
    }

    private String getTitleForMode() {
        switch (mode) {
            case MODE_REVIEW_DETAIL:
                return "Review Detail Vocabulary";
            case MODE_LEARN:
                return "Learn Vocabulary";
            case MODE_NORMAL:
            default:
                return "Vocabulary Detail";
        }
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

        displayExampleSentences();
    }

//    private void toggleWordDetails() {
//        detailsVisible = !detailsVisible;
//
//        binding.layoutDetails.setVisibility(detailsVisible ? View.VISIBLE : View.GONE);
//        binding.btnToggleWordDetails.setText(detailsVisible ? "Hide Word Details" : "Show Word Details");
//
//        if (detailsVisible) {
//            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
//            binding.layoutDetails.startAnimation(fadeIn);
//        }
//    }

    private void displayExampleSentences() {
        binding.layoutExamples.removeAllViews();
        if (vocab.getContextSentences() != null && !vocab.getContextSentences().isEmpty()) {
            for (Vocab.ContextSentence sentence : vocab.getContextSentences()) {
                View sentenceView = LayoutInflater.from(this).inflate(
                        R.layout.item_context_sentence,
                        binding.layoutExamples,
                        false
                );

                sentenceView.findViewById(R.id.tvJapaneseSentence)
                        .setTag(sentence);

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
        binding.layoutNormalMode.setVisibility(View.GONE);
        binding.layoutLearnMode.setVisibility(View.GONE);
        binding.layoutReviewMode.setVisibility(View.GONE);

        switch (mode) {
            case MODE_LEARN:
                setupLearnModeButtons();
                binding.layoutLearnMode.setVisibility(View.VISIBLE);
                break;
            case MODE_REVIEW_DETAIL:
                binding.btnRemoveWord.setOnClickListener(v -> deleteVocabFromDatabase());
                binding.layoutReviewMode.setVisibility(View.VISIBLE);
                break;
            case MODE_NORMAL:
            default:
                binding.layoutNormalMode.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupLearnModeButtons() {
        binding.btnLearn.setOnClickListener(v -> learnThisWord());

        binding.btnSkip.setOnClickListener(v -> skipLearning());

        binding.btnStartLearning.setOnClickListener(v -> startQuiz());

        int selectedCount = learnManager.getSelectedWordsCount();

        if (isWordSelected) {
            binding.btnLearn.setText("Word already selected");
            binding.btnLearn.setEnabled(false);
        }

        binding.btnStartLearning.setEnabled(selectedCount >= 5);
        binding.btnStartLearning.setText(String.format("Start Learning (%d words selected)", selectedCount));
    }

    private void learnThisWord() {
        if (vocab == null) return;

        learnManager.addWordToLearn(vocab.getId());
        isWordSelected = true;

        int selectedCount = learnManager.getSelectedWordsCount();
        binding.btnStartLearning.setText(String.format("Start Learning (%d words selected)", selectedCount));
        binding.btnStartLearning.setEnabled(selectedCount >= 5);

        Toast.makeText(this, "Added " + vocab.getCharacter() + " to your learning list!", Toast.LENGTH_SHORT).show();

        if (startLevel > 0 && endLevel > 0) {
            binding.getRoot().postDelayed(() -> {
                loadRandomVocabFromRange(startLevel, endLevel);
            }, 1500);
        }
    }

    private void skipLearning() {
        if (vocab == null) return;

        Toast.makeText(this, "Skipped " + vocab.getCharacter(), Toast.LENGTH_SHORT).show();

        if (startLevel > 0 && endLevel > 0) {
            loadRandomVocabFromRange(startLevel, endLevel);
        } else {
            finish();
        }
    }

    private void loadVocabFromDatabase(int id) {
        showLoading(true);

        new Thread(() -> {
            try {
                VocabHelper vocabHelper = VocabHelper.getInstance(this);
                ContextSentenceHelper contextHelper = ContextSentenceHelper.getInstance(this);

                vocabHelper.open();
                contextHelper.open();

                Cursor vocabCursor = vocabHelper.queryVocabById(String.valueOf(id));
                Cursor contextCursor = contextHelper.queryContextSentencesByVocabId(String.valueOf(id));

                final Vocab loadedVocab = MappingHelper.mapCursorToVocabWithContextSentences(
                        vocabCursor, contextCursor);

                vocabCursor.close();
                contextCursor.close();
                vocabHelper.close();
                contextHelper.close();

                runOnUiThread(() -> {
                    showLoading(false);

                    if (loadedVocab != null) {
                        vocab = loadedVocab;
                        vocabId = vocab.getId();
                        displayVocabData();
                        setupActionButtons();
                    } else {
                        showError("Vocab not found in database");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading vocab from database", e);
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void startQuiz() {
        Set<String> selectedWordIds = learnManager.getSelectedWordIds();
        if (selectedWordIds.isEmpty()) {
            Toast.makeText(this, "No words selected for quiz!", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        binding.tvError.setText("Preparing your quiz...");
        binding.tvError.setVisibility(View.VISIBLE);

        List<Vocab> selectedVocabs = new ArrayList<>();
        List<String> ids = new ArrayList<>(selectedWordIds);
        final int total = ids.size();
        final int[] loadedCount = {0};

        for (String idStr : ids) {
            int vocabId = Integer.parseInt(idStr);
            repository.getVocabDetail(vocabId, new VocabRepository.VocabDetailCallback() {
                @Override
                public void onSuccess(Vocab vocabItem) {
                    if (vocabItem != null) selectedVocabs.add(vocabItem);
                    loadedCount[0]++;
                    if (loadedCount[0] == total) {
                        saveAllVocabsToDatabase(selectedVocabs);
                    }
                }
                @Override
                public void onError(String message) {
                    loadedCount[0]++;
                    if (loadedCount[0] == total) {
                        saveAllVocabsToDatabase(selectedVocabs);
                    }
                }
            });
        }
    }

    private void saveAllVocabsToDatabase(List<Vocab> vocabs) {
        if (vocabs.isEmpty()) {
            runOnUiThread(() -> {
                showLoading(false);
                Toast.makeText(this, "No vocab data loaded!", Toast.LENGTH_SHORT).show();
            });
            return;
        }

       new Thread(() -> {
            try {
                VocabHelper vocabHelper = VocabHelper.getInstance(this);
                ContextSentenceHelper contextHelper = ContextSentenceHelper.getInstance(this);

                vocabHelper.open();
                contextHelper.open();

                for (Vocab vocab : vocabs) {
                    if (!vocabHelper.isVocabExists(String.valueOf(vocab.getId()))) {
                        ContentValues vocabValues = new ContentValues();
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_ID, vocab.getId());
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_CHARACTER, vocab.getCharacter());
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_MEANING, vocab.getMeaning());
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_READING, vocab.getReading());
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_PART_OF_SPEECH, vocab.getPartOfSpeech());
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_LEVEL, vocab.getLevel());
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_MEANING_MNEMONIC, vocab.getMeaningMnemonic());
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_READING_MNEMONIC, vocab.getReadingMnemonic());
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_AUDIO_URL, vocab.getAudioUrl());

                        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        vocabValues.put(DatabaseContract.VocabColumns.COLUMN_LEARNED_AT, currentTime);

                        long result = vocabHelper.insertVocab(vocabValues);

                        if (result > 0 && vocab.getContextSentences() != null) {
                            for (Vocab.ContextSentence sentence : vocab.getContextSentences()) {
                                ContentValues sentenceValues = new ContentValues();
                                sentenceValues.put(DatabaseContract.ContextSentenceColumns.COLUMN_VOCAB_ID, vocab.getId());
                                sentenceValues.put(DatabaseContract.ContextSentenceColumns.COLUMN_JAPANESE_TEXT, sentence.getJa());
                                sentenceValues.put(DatabaseContract.ContextSentenceColumns.COLUMN_ENGLISH_TEXT, sentence.getEn());

                                contextHelper.insertContextSentence(sentenceValues);
                            }
                        }
                    }
                }

                vocabHelper.close();
                contextHelper.close();

                runOnUiThread(() -> {
                    showLoading(false);
                    launchQuiz();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error saving vocabs to database", e);
            }
        }).start();
    }

    private void launchQuiz() {
        Intent intent = QuizActivity.createIntent(this, null);
        startActivity(intent);
        finish();
    }

    private void playAudio() {
        if (vocab == null || vocab.getAudioUrl() == null) {
            Toast.makeText(this, "No audio available", Toast.LENGTH_SHORT).show();
            return;
        }

        String audioUrl = vocab.getAudioUrl();
        AudioHelper.getInstance().playAudio(audioUrl);
    }

    private void showLoading(boolean isLoading) {
        binding.loadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.contentContainer.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.tvError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.contentContainer.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}