package com.example.chocominto.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "chocominto.db";
    private static final int DATABASE_VERSION = 1;

    private final String SQL_CREATE_VOCAB_TABLE =
            String.format("CREATE TABLE %s " +
                            "(%s INTEGER PRIMARY KEY," +
                            " %s TEXT NOT NULL," +
                            " %s TEXT NOT NULL," +
                            " %s TEXT NOT NULL," +
                            " %s TEXT," +
                            " %s INTEGER," +
                            " %s TEXT," +
                            " %s TEXT," +
                            " %s TEXT," +
                            " %s TEXT)",
                    DatabaseContract.VocabColumns.TABLE_NAME,
                    DatabaseContract.VocabColumns.COLUMN_ID,
                    DatabaseContract.VocabColumns.COLUMN_CHARACTER,
                    DatabaseContract.VocabColumns.COLUMN_MEANING,
                    DatabaseContract.VocabColumns.COLUMN_READING,
                    DatabaseContract.VocabColumns.COLUMN_PART_OF_SPEECH,
                    DatabaseContract.VocabColumns.COLUMN_LEVEL,
                    DatabaseContract.VocabColumns.COLUMN_MEANING_MNEMONIC,
                    DatabaseContract.VocabColumns.COLUMN_READING_MNEMONIC,
                    DatabaseContract.VocabColumns.COLUMN_AUDIO_URL,
                    DatabaseContract.VocabColumns.COLUMN_LEARNED_AT);

    private final String SQL_CREATE_CONTEXT_SENTENCES_TABLE =
            String.format("CREATE TABLE %s " +
                            "(%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                            " %s INTEGER NOT NULL," +
                            " %s TEXT NOT NULL," +
                            " %s TEXT NOT NULL," +
                            " FOREIGN KEY (%s) REFERENCES %s(%s))",
                    DatabaseContract.ContextSentenceColumns.TABLE_NAME,
                    DatabaseContract.ContextSentenceColumns.COLUMN_ID,
                    DatabaseContract.ContextSentenceColumns.COLUMN_VOCAB_ID,
                    DatabaseContract.ContextSentenceColumns.COLUMN_JAPANESE_TEXT,
                    DatabaseContract.ContextSentenceColumns.COLUMN_ENGLISH_TEXT,
                    DatabaseContract.ContextSentenceColumns.COLUMN_VOCAB_ID,
                    DatabaseContract.VocabColumns.TABLE_NAME,
                    DatabaseContract.VocabColumns.COLUMN_ID);

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_VOCAB_TABLE);
        db.execSQL(SQL_CREATE_CONTEXT_SENTENCES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ContextSentenceColumns.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.VocabColumns.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}