package com.example.chocominto.ui.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chocominto.adapters.ReviewAdapter;
import com.example.chocominto.data.manager.LearnManager;
import com.example.chocominto.ui.activities.QuizActivity;
import com.example.chocominto.ui.activities.VocabDetailActivity;
import com.example.chocominto.utils.MappingHelper;
import com.example.chocominto.data.database.VocabHelper;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.databinding.FragmentReviewBinding;

import java.util.ArrayList;
import java.util.Collections;

public class ReviewFragment extends Fragment {

    private FragmentReviewBinding binding;
    private VocabHelper vocabHelper;
    private ReviewAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vocabHelper = VocabHelper.getInstance(requireContext());
        adapter = new ReviewAdapter();

        setupRecyclerView();
        setupStartReviewButton();
        loadVocabFromDatabase();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.rvVocab;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickCallback(this::showSelectedVocab);
    }

    private void setupStartReviewButton() {
        binding.btnStartReview.setOnClickListener(v -> startReview());
    }

    private void startReview() {
        binding.progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                vocabHelper.open();

                Cursor cursor = vocabHelper.queryMemorizedVocab();

                if (cursor == null || cursor.getCount() < 10) {
                    requireActivity().runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "You need at least 5 learned words to start review", Toast.LENGTH_SHORT).show();
                    });

                    if (cursor != null) cursor.close();
                    vocabHelper.close();
                    return;
                }

                ArrayList<Vocab> allVocabs = MappingHelper.mapCursorToVocabList(cursor);
                cursor.close();
                vocabHelper.close();

                ArrayList<Vocab> selectedVocabs = selectRandomVocabs(allVocabs);

                saveSelectedVocabsForReview(selectedVocabs);

                requireActivity().runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    launchReviewActivity();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error starting review", e);
                requireActivity().runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void launchReviewActivity() {
        Intent intent = new Intent(requireContext(), QuizActivity.class);
        intent.putExtra("review_mode", true); // Tambahkan flag untuk mode review
        startActivity(intent);
    }

    private ArrayList<Vocab> selectRandomVocabs(ArrayList<Vocab> allVocabs) {
        Collections.shuffle(allVocabs);
        return new ArrayList<>(allVocabs.subList(0, 10));
    }

    private void saveSelectedVocabsForReview(ArrayList<Vocab> selectedVocabs) {
        LearnManager learnManager = LearnManager.getInstance(requireContext());
        learnManager.clearSelectedWords();

        for (Vocab vocab : selectedVocabs) {
            learnManager.addWordToLearn(vocab.getId());
        }
    }

    private void loadVocabFromDatabase() {
        binding.progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            vocabHelper.open();
            final Cursor cursor = vocabHelper.queryAllVocab(); // atau queryMemorizedVocab() jika hanya ingin yang sudah di-memorized
            final ArrayList<Vocab> vocabList = MappingHelper.mapCursorToVocabList(cursor);
            cursor.close();
            vocabHelper.close();

            requireActivity().runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);

                if (vocabList.size() > 0) {
                    adapter.setVocabList(vocabList);
                } else {
                    showEmptyState();
                }
            });
        }).start();
    }

    private void showEmptyState() {
        binding.rvVocab.setVisibility(View.GONE);
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.btnStartLearning.setVisibility(View.VISIBLE);
    }

    private void showSelectedVocab(Vocab vocab) {
        Intent intent = VocabDetailActivity.createIntent(
                requireContext(),
                vocab.getId(),
                VocabDetailActivity.MODE_REVIEW_DETAIL,
                "review_fragment"
        );
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVocabFromDatabase();
    }
}