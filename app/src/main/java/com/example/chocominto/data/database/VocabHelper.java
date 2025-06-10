package com.example.chocominto.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VocabHelper {
    private static final String VOCAB_TABLE = DatabaseContract.VocabColumns.TABLE_NAME;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;
    public static volatile VocabHelper INSTANCE;

    private VocabHelper(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public static VocabHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SQLiteOpenHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new VocabHelper(context);
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

    // Query all vocabulary
    public Cursor queryAllVocab() {
        return database.query(
                VOCAB_TABLE,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.VocabColumns.COLUMN_ID + " ASC"
        );
    }

    // Query vocab by ID
    public Cursor queryVocabById(String id) {
        return database.query(
                VOCAB_TABLE,
                null,
                DatabaseContract.VocabColumns.COLUMN_ID + " = ?",
                new String[]{id},
                null,
                null,
                null
        );
    }

    // Query vocab by character (partial match)
    public Cursor queryVocabByCharacter(String character) {
        return database.query(
                VOCAB_TABLE,
                null,
                DatabaseContract.VocabColumns.COLUMN_CHARACTER + " LIKE ?",
                new String[]{"%" + character + "%"},
                null,
                null,
                null
        );
    }

    // Query vocab by meaning (partial match)
    public Cursor queryVocabByMeaning(String meaning) {
        return database.query(
                VOCAB_TABLE,
                null,
                DatabaseContract.VocabColumns.COLUMN_MEANING + " LIKE ?",
                new String[]{"%" + meaning + "%"},
                null,
                null,
                null
        );
    }

    // Query vocab by level
    public Cursor queryVocabByLevel(int level) {
        return database.query(
                VOCAB_TABLE,
                null,
                DatabaseContract.VocabColumns.COLUMN_LEVEL + " = ?",
                new String[]{String.valueOf(level)},
                null,
                null,
                null
        );
    }

    // Query memorized vocabulary (those with LEARNED_AT not null)
    public Cursor queryMemorizedVocab() {
        return database.query(
                VOCAB_TABLE,
                null,
                DatabaseContract.VocabColumns.COLUMN_LEARNED_AT + " IS NOT NULL",
                null,
                null,
                null,
                DatabaseContract.VocabColumns.COLUMN_LEARNED_AT + " DESC" // Most recently learned first
        );
    }

    // Query non-memorized vocabulary (those with LEARNED_AT null)
    public Cursor queryNonMemorizedVocab() {
        return database.query(
                VOCAB_TABLE,
                null,
                DatabaseContract.VocabColumns.COLUMN_LEARNED_AT + " IS NULL",
                null,
                null,
                null,
                DatabaseContract.VocabColumns.COLUMN_ID + " ASC"
        );
    }

    // Insert new vocabulary
    public long insertVocab(ContentValues values) {
        return database.insert(VOCAB_TABLE, null, values);
    }

    // Update vocabulary
    public int updateVocab(String id, ContentValues values) {
        return database.update(VOCAB_TABLE, values,
                DatabaseContract.VocabColumns.COLUMN_ID + " = ?",
                new String[]{id});
    }

    // Delete vocabulary by ID
//    public int deleteVocabById(String id) {
//        return database.delete(VOCAB_TABLE,
//                DatabaseContract.VocabColumns.COLUMN_ID + " = ?",
//                new String[]{id});
//    }

    public boolean deleteVocabById(String id) {
        int rowsAffected = database.delete(VOCAB_TABLE,
                DatabaseContract.VocabColumns.COLUMN_ID + " = ?",
                new String[]{id});
        return rowsAffected > 0;
    }

    // Mark vocabulary as memorized with current timestamp
    public int markVocabAsMemorized(String id, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.VocabColumns.COLUMN_LEARNED_AT, timestamp);
        return updateVocab(id, values);
    }

    // Mark vocabulary as not memorized (clear timestamp)
    public int markVocabAsNotMemorized(String id) {
        ContentValues values = new ContentValues();
        values.putNull(DatabaseContract.VocabColumns.COLUMN_LEARNED_AT);
        return updateVocab(id, values);
    }

    // Check if vocabulary exists by ID
    public boolean isVocabExists(String id) {
        Cursor cursor = queryVocabById(id);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }
}