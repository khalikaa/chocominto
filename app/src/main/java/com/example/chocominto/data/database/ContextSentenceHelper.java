package com.example.chocominto.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContextSentenceHelper {
    private static final String CONTEXT_SENTENCE_TABLE = DatabaseContract.ContextSentenceColumns.TABLE_NAME;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;
    public static volatile ContextSentenceHelper INSTANCE;

    private ContextSentenceHelper(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public static ContextSentenceHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SQLiteOpenHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ContextSentenceHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    public void open() throws SQLException {
        database = databaseHelper.getWritableDatabase();
    }

    public void close() {
        databaseHelper.close();
        if (database.isOpen()) {
            database.close();
        }
    }


    public Cursor queryContextSentencesByVocabId(String vocabId) {
        return database.query(
                CONTEXT_SENTENCE_TABLE,
                null,
                DatabaseContract.ContextSentenceColumns.COLUMN_VOCAB_ID + " = ?",
                new String[]{vocabId},
                null,
                null,
                null
        );
    }

    public long insertContextSentence(ContentValues values) {
        return database.insert(CONTEXT_SENTENCE_TABLE, null, values);
    }

    public int deleteContextSentencesByVocabId(String vocabId) {
        return database.delete(CONTEXT_SENTENCE_TABLE,
                DatabaseContract.ContextSentenceColumns.COLUMN_VOCAB_ID + " = ?",
                new String[]{vocabId});
    }
}