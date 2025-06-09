package com.example.chocominto.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chocominto.R;
import com.example.chocominto.databinding.FragmentLearnBinding;
import com.example.chocominto.ui.activities.VocabDetailActivity;

public class LearnFragment extends Fragment {
    private FragmentLearnBinding binding;

    // Mock data
    private int wordsLearned = 15;
    private int streak = 3;
    private int dailyGoal = 10;
    private int progress = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLearnBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up progress section
        setupProgressSection();

        // Set up level click listeners
        setupLevelClickListeners();
    }

    private void setupProgressSection() {
        // Update progress bar
        binding.progressBar.setMax(dailyGoal);
        binding.progressBar.setProgress(progress);

        // Update statistics text
        binding.textWordsLearned.setText(getString(R.string.words_learned, wordsLearned));
        binding.textCurrentStreak.setText(getString(R.string.current_streak, streak));
        binding.textProgressCounter.setText(progress + "/" + dailyGoal);
    }

    private void setupLevelClickListeners() {
        binding.levelOne.setOnClickListener(v -> navigateToWordDetail(1, 10));
        binding.levelTwo.setOnClickListener(v -> navigateToWordDetail(11, 20));
        binding.levelThree.setOnClickListener(v -> navigateToWordDetail(21, 30));
        binding.levelFour.setOnClickListener(v -> navigateToWordDetail(31, 40));
        binding.levelFive.setOnClickListener(v -> navigateToWordDetail(41, 50));
        binding.levelSix.setOnClickListener(v -> navigateToWordDetail(51, 60));
    }

    private void navigateToWordDetail(int minLevel, int maxLevel) {
        // Create intent to launch WordDetailActivity
        Intent intent = new Intent(getActivity(), VocabDetailActivity.class);

        // Pass the level range as extras
        intent.putExtra("MIN_LEVEL", minLevel);
        intent.putExtra("MAX_LEVEL", maxLevel);

        // Start the activity
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}