package com.social7.androidnoteapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.social7.androidnoteapp.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper for raw SQLite CRUD operations.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NOTES = "notes";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_FAVORITE = "favorite";
    public static final String COLUMN_PINNED = "pinned";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    private static final String CREATE_TABLE_NOTES =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT DEFAULT 'Personal', " +
                    COLUMN_FAVORITE + " INTEGER DEFAULT 0, " +
                    COLUMN_PINNED + " INTEGER DEFAULT 0, " +
                    COLUMN_CREATED_AT + " INTEGER, " +
                    COLUMN_UPDATED_AT + " INTEGER" +
                    ")";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Safe upgrade without dropping data unless explicitly designed
    }

    /**
     * Insert a new note into SQLite database.
     */
    public long insertNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_CATEGORY, note.getCategory());
        values.put(COLUMN_FAVORITE, note.isFavorite() ? 1 : 0);
        values.put(COLUMN_PINNED, note.isPinned() ? 1 : 0);
        values.put(COLUMN_CREATED_AT, note.getCreatedAt());
        values.put(COLUMN_UPDATED_AT, note.getUpdatedAt());

        long id = db.insert(TABLE_NOTES, null, values);
        note.setId(id);
        return id;
    }

    /**
     * Retrieve all notes, sorted by pinned status first, then by specified sort order.
     */
    public List<Note> getAllNotes(String sortOrder) {
        List<Note> noteList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String orderBy = COLUMN_PINNED + " DESC, ";
        if ("date_asc".equalsIgnoreCase(sortOrder)) {
            orderBy += COLUMN_UPDATED_AT + " ASC";
        } else if ("title_asc".equalsIgnoreCase(sortOrder)) {
            orderBy += COLUMN_TITLE + " ASC";
        } else { // default: date_desc
            orderBy += COLUMN_UPDATED_AT + " DESC";
        }

        Cursor cursor = db.query(TABLE_NOTES, null, null, null, null, null, orderBy);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Note note = cursorToNote(cursor);
                    noteList.add(note);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return noteList;
    }

    /**
     * Get a single note by ID.
     */
    public Note getNoteById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Note note = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                note = cursorToNote(cursor);
            }
            cursor.close();
        }
        return note;
    }

    /**
     * Update an existing note.
     */
    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_CATEGORY, note.getCategory());
        values.put(COLUMN_FAVORITE, note.isFavorite() ? 1 : 0);
        values.put(COLUMN_PINNED, note.isPinned() ? 1 : 0);
        values.put(COLUMN_UPDATED_AT, System.currentTimeMillis());

        return db.update(TABLE_NOTES, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(note.getId())});
    }

    /**
     * Delete a single note by ID.
     */
    public int deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NOTES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    /**
     * Delete all notes.
     */
    public void deleteAllNotes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, null, null);
    }

    /**
     * Search notes by title or content.
     */
    public List<Note> searchNotes(String query, String sortOrder) {
        List<Note> noteList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?";
        String wildQuery = "%" + query + "%";
        String[] selectionArgs = new String[]{wildQuery, wildQuery};

        String orderBy = COLUMN_PINNED + " DESC, ";
        if ("date_asc".equalsIgnoreCase(sortOrder)) {
            orderBy += COLUMN_UPDATED_AT + " ASC";
        } else if ("title_asc".equalsIgnoreCase(sortOrder)) {
            orderBy += COLUMN_TITLE + " ASC";
        } else {
            orderBy += COLUMN_UPDATED_AT + " DESC";
        }

        Cursor cursor = db.query(TABLE_NOTES, null, selection, selectionArgs, null, null, orderBy);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    noteList.add(cursorToNote(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return noteList;
    }

    private Note cursorToNote(Cursor cursor) {
        int idIdx = cursor.getColumnIndexOrThrow(COLUMN_ID);
        int titleIdx = cursor.getColumnIndexOrThrow(COLUMN_TITLE);
        int contentIdx = cursor.getColumnIndexOrThrow(COLUMN_CONTENT);
        int catIdx = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY);
        int favIdx = cursor.getColumnIndexOrThrow(COLUMN_FAVORITE);
        int pinIdx = cursor.getColumnIndexOrThrow(COLUMN_PINNED);
        int createdIdx = cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT);
        int updatedIdx = cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT);

        return new Note(
                cursor.getLong(idIdx),
                cursor.getString(titleIdx),
                cursor.getString(contentIdx),
                cursor.getString(catIdx),
                cursor.getInt(favIdx) == 1,
                cursor.getInt(pinIdx) == 1,
                cursor.getLong(createdIdx),
                cursor.getLong(updatedIdx)
        );
    }
}
