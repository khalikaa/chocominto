package com.example.chocominto.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.chocominto.R;
import com.example.chocominto.data.database.ContextSentenceHelper;
import com.example.chocominto.utils.MappingHelper;
import com.example.chocominto.data.database.VocabHelper;
import com.example.chocominto.data.manager.LearnManager;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.databinding.ActivityQuizBinding;
import com.example.chocominto.utils.AudioHelper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {
    private int totalWordsInSession = 0;
    private int completedWordsInSession = 0;

    private ActivityQuizBinding binding;
    private LearnManager learnManager;
    private VocabHelper vocabHelper;
    private ContextSentenceHelper contextSentenceHelper;
    private Deque<Integer> quizQueue = new ArrayDeque<>();
    private Vocab currentVocab;
    private boolean detailsVisible = false;
    private ArrayList<Vocab> vocabList = new ArrayList<>();
    private boolean isReviewMode = false;

    public static Intent createIntent(Context context, List<Vocab> vocabList) {
        Intent intent = new Intent(context, QuizActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isReviewMode = getIntent().getBooleanExtra("review_mode", false);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isReviewMode ? "Review" : "Quiz");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.choco));
        }

        learnManager = LearnManager.getInstance(this);
        vocabHelper = VocabHelper.getInstance(this);
        contextSentenceHelper = ContextSentenceHelper.getInstance(this);

        binding.btnToggleWordDetails.setOnClickListener(v -> toggleWordDetails());
        binding.btnToggleWordDetails.setVisibility(View.VISIBLE);
        binding.layoutDetails.setVisibility(View.GONE);
        binding.layoutPronunciation.setVisibility(View.GONE);
        binding.layoutQuizMode.setVisibility(View.VISIBLE);

        binding.btnPlayAudio.setOnClickListener(v -> playAudio());

        if (isReviewMode) {
            binding.btnStopLearn.setText("I Remember\nThis Word");
            binding.btnKeepLearn.setText("Keep Reviewing\nThis Word");
            binding.btnRemoveWord.setVisibility(View.VISIBLE);
        }

        binding.btnStopLearn.setOnClickListener(v -> {
            if (currentVocab != null) {
                quizQueue.remove(currentVocab.getId());
                completedWordsInSession++;
                nextQuizWord();
            }
        });

        binding.btnKeepLearn.setOnClickListener(v -> {
            if (currentVocab != null) {
                quizQueue.pollFirst();
                quizQueue.addLast(currentVocab.getId());
                nextQuizWord();
            }
        });


        binding.btnRemoveWord.setOnClickListener(v -> deleteCurrentVocabFromDatabase());

        loadVocabFromDatabase();
    }

    private void deleteCurrentVocabFromDatabase() {
        if (currentVocab == null) {
            Toast.makeText(this, "No vocabulary selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Vocabulary")
                .setMessage("Are you sure you want to delete '" + currentVocab.getCharacter() + "' from your learned vocabulary?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        try {
                            VocabHelper vocabHelper = VocabHelper.getInstance(this);
                            ContextSentenceHelper contextHelper = ContextSentenceHelper.getInstance(this);

                            vocabHelper.open();
                            contextHelper.open();

                            contextHelper.deleteContextSentencesByVocabId(String.valueOf(currentVocab.getId()));

                            boolean success = vocabHelper.deleteVocabById(String.valueOf(currentVocab.getId()));

                            contextHelper.close();
                            vocabHelper.close();

                            runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(this, "Vocabulary deleted successfully", Toast.LENGTH_SHORT).show();

                                    quizQueue.remove(currentVocab.getId());

                                    if (quizQueue.isEmpty()) {
                                        showQuizDone();
                                    } else {
                                        nextQuizWord();
                                    }
                                } else {
                                    Toast.makeText(this, "Failed to delete vocabulary", Toast.LENGTH_SHORT).show();
                                    binding.contentContainer.setVisibility(View.VISIBLE);
                                    binding.tvError.setVisibility(View.GONE);
                                }
                            });

                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                binding.contentContainer.setVisibility(View.VISIBLE);
                                binding.tvError.setVisibility(View.GONE);
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadVocabFromDatabase() {
//        showLoading(true);

        new Thread(() -> {
            try {
                Set<String> selectedWordIds = learnManager.getSelectedWordIds();

                if (selectedWordIds.isEmpty()) {
                    runOnUiThread(() -> {
//                        showLoading(false);
                        showError("No words selected for quiz!");
                    });
                    return;
                }

                vocabHelper.open();
                vocabList.clear();
                quizQueue.clear();

                for (String idStr : selectedWordIds) {
                    Cursor vocabCursor = vocabHelper.queryVocabById(idStr);

                    if (vocabCursor != null && vocabCursor.getCount() > 0) {
                        contextSentenceHelper.open();
                        Cursor contextCursor = contextSentenceHelper.queryContextSentencesByVocabId(idStr);

                        Vocab vocab = MappingHelper.mapCursorToVocabWithContextSentences(
                                vocabCursor, contextCursor);
                        if (vocab != null) {
                            vocabList.add(vocab);
                            quizQueue.add(vocab.getId());
                            totalWordsInSession++;
                        }

                        contextCursor.close();
                        vocabCursor.close();
                    }
                }

                contextSentenceHelper.close();
                vocabHelper.close();

                runOnUiThread(() -> {
//                    showLoading(false);

                    if (vocabList.isEmpty()) {
                        showError("No vocabulary data found in database!");
                    } else {
                        nextQuizWord();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
//                    showLoading(false);
                    showError("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void toggleWordDetails() {
        detailsVisible = !detailsVisible;

        binding.layoutDetails.setVisibility(detailsVisible ? View.VISIBLE : View.GONE);
        binding.btnToggleWordDetails.setText(detailsVisible ? "Hide Word Details" : "Show Word Details");
        binding.layoutPronunciation.setVisibility(detailsVisible ? View.VISIBLE : View.GONE);

        if (detailsVisible) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            binding.layoutDetails.startAnimation(fadeIn);
        }
    }

    private void updateSessionProgress() {
        if (isReviewMode) {
            binding.tvProgressCount.setText(String.format("You've Reviewed %d/%d words",
                    completedWordsInSession, totalWordsInSession));
        } else {
            binding.tvProgressCount.setText(String.format("You've Memorized %d/%d words",
                    completedWordsInSession, totalWordsInSession));
        }
    }


    private void nextQuizWord() {
        if (quizQueue.isEmpty()) {
            showQuizDone();
            return;
        }

        binding.layoutDetails.setVisibility(View.GONE);
        binding.layoutPronunciation.setVisibility(View.GONE);
        binding.btnToggleWordDetails.setVisibility(View.VISIBLE);
        binding.getRoot().scrollTo(0, 0);

        int vocabId = quizQueue.peekFirst();

        Vocab foundVocab = null;
        for (Vocab vocab : vocabList) {
            if (vocab.getId() == vocabId) {
                foundVocab = vocab;
                break;
            }
        }

        if (foundVocab != null) {
            currentVocab = foundVocab;
            displayVocabData();
            updateSessionProgress();
        } else {
            Toast.makeText(this, "Error: Vocab not found!", Toast.LENGTH_SHORT).show();
            quizQueue.pollFirst();
            nextQuizWord();
        }
    }

    private void displayVocabData() {
        if (currentVocab == null) return;

        binding.tvCharacter.setText(currentVocab.getCharacter());
        binding.tvReading.setText(currentVocab.getReading());
        binding.tvMeaning.setText(currentVocab.getMeaning());
        binding.tvLevel.setText(getString(R.string.level_format, currentVocab.getLevel()));
        binding.tvPartOfSpeech.setText(currentVocab.getPartOfSpeech());
        binding.tvMeaningMnemonic.setText(stripTags(currentVocab.getMeaningMnemonic()));
        binding.tvReadingMnemonic.setText(stripTags(currentVocab.getReadingMnemonic()));

        displayExampleSentences();
    }

    private void displayExampleSentences() {
        binding.layoutExamples.removeAllViews();
        if (currentVocab.getContextSentences() != null && !currentVocab.getContextSentences().isEmpty()) {
            for (Vocab.ContextSentence sentence : currentVocab.getContextSentences()) {
                View sentenceView = getLayoutInflater().inflate(
                        R.layout.item_context_sentence,
                        binding.layoutExamples,
                        false
                );
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

    private void playAudio() {
        if (currentVocab == null || currentVocab.getAudioUrl() == null) {
            Toast.makeText(this, "No audio available", Toast.LENGTH_SHORT).show();
            return;
        }

        String audioUrl = currentVocab.getAudioUrl();
        AudioHelper.getInstance().playAudio(audioUrl);
    }

//    private void showLoading(boolean isLoading) {
//        binding.contentContainer.setVisibility(isLoading ? View.GONE : View.VISIBLE);
//        binding.tvError.setVisibility(View.GONE);
//
//        if (isLoading) {
//            binding.tvError.setText("Loading...");
//            binding.tvError.setVisibility(View.VISIBLE);
//        }
//    }

    private String stripTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "");
    }

    private void showError(String message) {
        binding.contentContainer.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showQuizDone() {
        binding.contentContainer.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText("Congratulations!\nYou have memorized all your words.");
        Toast.makeText(this, "Quiz Complete!", Toast.LENGTH_LONG).show();

        learnManager.clearSelectedWords();

        binding.tvError.postDelayed(this::finish, 2000);
    }


    @Override
    public void onBackPressed() {
        if (!isReviewMode) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit Learning Mode")
                    .setMessage("Are you sure you want to exit? Words you've selected will remain in your review list.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        learnManager.clearSelectedWords();
                        super.onBackPressed();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}