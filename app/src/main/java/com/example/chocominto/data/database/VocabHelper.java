package com.example.chocominto.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public int getTodayVocabCount() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + VOCAB_TABLE + " WHERE substr(" + DatabaseContract.VocabColumns.COLUMN_LEARNED_AT + ", 1, 10) = ?",
                new String[]{today}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public Cursor searchVocab(String query) {
        String searchQuery = "%" + query + "%";
        return database.query(
                VOCAB_TABLE,
                null,
                DatabaseContract.VocabColumns.COLUMN_CHARACTER + " LIKE ? OR " +
                        DatabaseContract.VocabColumns.COLUMN_READING + " LIKE ? OR " +
                        DatabaseContract.VocabColumns.COLUMN_MEANING + " LIKE ?",
                new String[]{searchQuery, searchQuery, searchQuery},
                null,
                null,
                DatabaseContract.VocabColumns.COLUMN_CHARACTER + " ASC"
        );
    }

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



    public long insertVocab(ContentValues values) {
        return database.insert(VOCAB_TABLE, null, values);
    }

    public boolean deleteVocabById(String id) {
        int rowsAffected = database.delete(VOCAB_TABLE,
                DatabaseContract.VocabColumns.COLUMN_ID + " = ?",
                new String[]{id});
        return rowsAffected > 0;
    }

    public boolean isVocabExists(String id) {
        Cursor cursor = queryVocabById(id);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }
}