package com.example.chocominto.ui.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chocominto.R;
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
import java.util.List;

public class ReviewFragment extends Fragment {

    private FragmentReviewBinding binding;
    private VocabHelper vocabHelper;
    private ReviewAdapter adapter;
    private String currentQuery = "";
    private ArrayList<Vocab> allVocabList = new ArrayList<>();

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
        setupSearchView();
        loadVocabFromDatabase();
        binding.btnStartLearning.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_reviewFragment_to_learnFragment);
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.rvVocab;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickCallback(this::showSelectedVocab);
    }

    private void setupSearchView() {
        binding.searchView.setIconifiedByDefault(false);
        binding.searchView.setQueryHint("Search vocabulary...");

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                filterVocabs();
                binding.searchView.clearFocus(); // Sembunyikan keyboard
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                filterVocabs();
                return true;
            }
        });

        binding.searchView.setOnClickListener(v -> {
            binding.searchView.onActionViewExpanded();
            binding.searchView.requestFocus();
        });
    }

    // Tambahkan method untuk filter vocab
    private void filterVocabs() {
        if (allVocabList.isEmpty()) {
            return;
        }

        List<Vocab> filteredList = new ArrayList<>();

        if (TextUtils.isEmpty(currentQuery)) {
            filteredList.addAll(allVocabList);
        } else {
            String lowerQuery = currentQuery.toLowerCase();
            for (Vocab vocab : allVocabList) {
                if (vocab.getCharacter().toLowerCase().contains(lowerQuery) ||
                        vocab.getMeaning().toLowerCase().contains(lowerQuery) ||
                        vocab.getReading().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(vocab);
                }
            }
        }

        adapter.setVocabList(filteredList);

        boolean isEmpty = filteredList.isEmpty();
        if (isEmpty) {
            if (!TextUtils.isEmpty(currentQuery)) {
                binding.tvEmpty.setText("No results found for \"" + currentQuery + "\"");
            } else {
                binding.tvEmpty.setText("No vocabulary available for review");
            }
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvVocab.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvVocab.setVisibility(View.VISIBLE);
        }
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
        return new ArrayList<>(allVocabs.subList(0, Math.min(10, allVocabs.size())));
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

            allVocabList.clear();
            allVocabList.addAll(vocabList);

            requireActivity().runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);

                if (vocabList.size() > 0) {
                    // Jika ada query aktif, filter hasil
                    if (!TextUtils.isEmpty(currentQuery)) {
                        filterVocabs();
                    } else {
                        adapter.setVocabList(vocabList);
                    }
                    binding.rvVocab.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setVisibility(View.GONE);
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
        binding.searchView.setVisibility(View.GONE);
        binding.btnStartReview.setVisibility(View.GONE);
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