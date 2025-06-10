package com.example.chocominto.data.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class LearnManager {
    private static final String TAG = "LearnManager";
    private static LearnManager instance;

    private static final String PREFS_NAME = "learn_prefs";
    private static final String KEY_SELECTED_WORDS = "selected_words";
    private static final String KEY_MEMORIZED_WORDS = "memorized_words";

    private SharedPreferences prefs;

    private LearnManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized LearnManager getInstance(Context context) {
        if (instance == null) {
            instance = new LearnManager(context);
        }
        return instance;
    }

    public void addWordToLearn(int vocabId) {
        Set<String> selectedWords = getSelectedWordIds();
        selectedWords.add(String.valueOf(vocabId));
        prefs.edit().putStringSet(KEY_SELECTED_WORDS, selectedWords).apply();
        Log.d(TAG, "Added word " + vocabId + " to learn list. Total: " + selectedWords.size());
    }

    public boolean isWordSelected(int vocabId) {
        return getSelectedWordIds().contains(String.valueOf(vocabId));
    }

    public Set<String> getSelectedWordIds() {
        return new HashSet<>(prefs.getStringSet(KEY_SELECTED_WORDS, new HashSet<>()));
    }

    public int getSelectedWordsCount() {
        return getSelectedWordIds().size();
    }

    public void clearSelectedWords() {
        prefs.edit().remove(KEY_SELECTED_WORDS).apply();
    }

    public Set<String> getMemorizedWordIds() {
        return new HashSet<>(prefs.getStringSet(KEY_MEMORIZED_WORDS, new HashSet<>()));
    }
}