package com.example.chocominto.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chocominto.data.database.VocabHelper;
import com.example.chocominto.data.manager.LearnManager;
import com.example.chocominto.databinding.FragmentLearnBinding;
import com.example.chocominto.ui.activities.VocabDetailActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LearnFragment extends Fragment {
    private FragmentLearnBinding binding;
    private LearnManager learnManager;
    private VocabHelper vocabHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLearnBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        learnManager = LearnManager.getInstance(requireContext());

        setupProgressData();

        setupLevelClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProgressBar();
    }

    private void setupProgressData() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            handler.post(() -> {
                vocabHelper = VocabHelper.getInstance(requireContext());
                vocabHelper.open();
                int selectedCount = vocabHelper.getTodayVocabCount();
                binding.textWordsLearned.setText("You've learned " + selectedCount + " Words Today!");
                vocabHelper.close();
            });
        });
    }

    private void updateProgressBar() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            handler.post(() -> {
                vocabHelper = VocabHelper.getInstance(requireContext());
                vocabHelper.open();
                int selectedCount = vocabHelper.getTodayVocabCount();
                binding.textWordsLearned.setText("You've learned " + selectedCount + " Words Today!");
                vocabHelper.close();
            });
        });
    }

    private void setupLevelClickListeners() {
        binding.levelOne.setOnClickListener(v -> {
            openVocabDetailWithLevel(1, 10);
        });

        binding.levelTwo.setOnClickListener(v -> {
            openVocabDetailWithLevel(11, 20);
        });

        binding.levelThree.setOnClickListener(v -> {
            openVocabDetailWithLevel(21, 30);
        });

        binding.levelFour.setOnClickListener(v -> {
            openVocabDetailWithLevel(31, 40);
        });

        binding.levelFive.setOnClickListener(v -> {
            openVocabDetailWithLevel(41, 50);
        });

        binding.levelSix.setOnClickListener(v -> {
            openVocabDetailWithLevel(51, 60);
        });
    }

    private void openVocabDetailWithLevel(int startLevel, int endLevel) {
        Intent intent = VocabDetailActivity.createIntentForLearn(
                requireContext(),
                startLevel,
                endLevel
        );
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}