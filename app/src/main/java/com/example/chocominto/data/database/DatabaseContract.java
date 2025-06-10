package com.example.chocominto.data.database;

import android.provider.BaseColumns;

public class DatabaseContract {

    private DatabaseContract() {}

    public static final class VocabColumns implements BaseColumns {
        public static final String TABLE_NAME = "vocabulary";
        public static final String COLUMN_ID = "vocab_id";
        public static final String COLUMN_CHARACTER = "character";
        public static final String COLUMN_MEANING = "meaning";
        public static final String COLUMN_READING = "reading";
        public static final String COLUMN_PART_OF_SPEECH = "part_of_speech";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_MEANING_MNEMONIC = "meaning_mnemonic";
        public static final String COLUMN_READING_MNEMONIC = "reading_mnemonic";
        public static final String COLUMN_AUDIO_URL = "audio_url";
        public static final String COLUMN_LEARNED_AT = "learned_at";
    }

    // Context Sentences table (one-to-many relationship with vocab)
    public static final class ContextSentenceColumns implements BaseColumns {
        public static final String TABLE_NAME = "context_sentences";
        public static final String COLUMN_ID = "sentence_id";
        public static final String COLUMN_VOCAB_ID = "vocab_id"; // Foreign key to vocab table
        public static final String COLUMN_JAPANESE_TEXT = "japanese_text";
        public static final String COLUMN_ENGLISH_TEXT = "english_text";
    }
}