package com.example.chocominto.utils;

import android.database.Cursor;

import com.example.chocominto.data.database.DatabaseContract;
import com.example.chocominto.data.models.Vocab;

import java.util.ArrayList;

public class MappingHelper {
    public static ArrayList<Vocab> mapCursorToVocabList(Cursor vocabCursor) {
        ArrayList<Vocab> vocabList = new ArrayList<>();

        if (vocabCursor != null && vocabCursor.getCount() > 0) {
            while (vocabCursor.moveToNext()) {
                int id = vocabCursor.getInt(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_ID));
                String character = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_CHARACTER));
                String meaning = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_MEANING));
                String reading = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_READING));
                String partOfSpeech = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_PART_OF_SPEECH));
                int level = vocabCursor.getInt(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_LEVEL));
                String meaningMnemonic = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_MEANING_MNEMONIC));
                String readingMnemonic = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_READING_MNEMONIC));
                String audioUrl = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_AUDIO_URL));

                Vocab vocab = new Vocab(id, character, meaning, reading, partOfSpeech,
                        level, meaningMnemonic, readingMnemonic, audioUrl,
                        new ArrayList<>());

                vocabList.add(vocab);
            }
        }

        return vocabList;
    }

    public static Vocab mapCursorToVocabWithContextSentences(Cursor vocabCursor, Cursor contextSentenceCursor) {
        if (vocabCursor == null || vocabCursor.getCount() == 0) {
            return null;
        }

        vocabCursor.moveToFirst();
        int id = vocabCursor.getInt(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_ID));
        String character = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_CHARACTER));
        String meaning = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_MEANING));
        String reading = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_READING));
        String partOfSpeech = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_PART_OF_SPEECH));
        int level = vocabCursor.getInt(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_LEVEL));
        String meaningMnemonic = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_MEANING_MNEMONIC));
        String readingMnemonic = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_READING_MNEMONIC));
        String audioUrl = vocabCursor.getString(vocabCursor.getColumnIndexOrThrow(DatabaseContract.VocabColumns.COLUMN_AUDIO_URL));

        ArrayList<Vocab.ContextSentence> contextSentences = new ArrayList<>();

        if (contextSentenceCursor != null && contextSentenceCursor.getCount() > 0) {
            while (contextSentenceCursor.moveToNext()) {
                String japaneseText = contextSentenceCursor.getString(
                        contextSentenceCursor.getColumnIndexOrThrow(DatabaseContract.ContextSentenceColumns.COLUMN_JAPANESE_TEXT));
                String englishText = contextSentenceCursor.getString(
                        contextSentenceCursor.getColumnIndexOrThrow(DatabaseContract.ContextSentenceColumns.COLUMN_ENGLISH_TEXT));

                Vocab.ContextSentence sentence = new Vocab.ContextSentence(japaneseText, englishText);
                contextSentences.add(sentence);
            }
        }

        return new Vocab(id, character, meaning, reading, partOfSpeech,
                level, meaningMnemonic, readingMnemonic, audioUrl, contextSentences);
    }

    public static String getCurrentTimestamp() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return dateFormat.format(new java.util.Date());
    }
}