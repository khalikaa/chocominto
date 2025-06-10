package com.example.chocominto.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chocominto.data.manager.LearnManager;
import com.example.chocominto.databinding.FragmentLearnBinding;
import com.example.chocominto.ui.activities.VocabDetailActivity;

public class LearnFragment extends Fragment {
    private FragmentLearnBinding binding;
    private LearnManager learnManager;

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
        int goal = 10; // Default goal
        int selectedCount = learnManager.getSelectedWordsCount();

        binding.progressBar.setMax(goal);
        binding.progressBar.setProgress(selectedCount);

        binding.textProgressCounter.setText(selectedCount + "/" + goal);

        binding.textWordsLearned.setText(selectedCount + " words selected for learning");

        // Streak
        int streak = 3; // Mock streak
        binding.textCurrentStreak.setText(streak + "-day streak! 🔥");
    }

    private void updateProgressBar() {
        int goal = 10; // Default goal
        int selectedCount = learnManager.getSelectedWordsCount();

        binding.progressBar.setProgress(selectedCount);
        binding.textProgressCounter.setText(selectedCount + "/" + goal);
        binding.textWordsLearned.setText(selectedCount + " words selected for learning");
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