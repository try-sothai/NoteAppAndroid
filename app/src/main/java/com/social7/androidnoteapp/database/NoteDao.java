package com.social7.androidnoteapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) for Room Note entity operations.
 */
@Dao
public interface NoteDao {

    @Insert
    long insert(NoteEntity note);

    @Update
    int update(NoteEntity note);

    @Delete
    int delete(NoteEntity note);

    @Query("DELETE FROM notes_room WHERE id = :id")
    int deleteById(long id);

    @Query("DELETE FROM notes_room")
    void deleteAll();

    @Query("SELECT * FROM notes_room WHERE id = :id LIMIT 1")
    NoteEntity getNoteById(long id);

    @Query("SELECT * FROM notes_room ORDER BY pinned DESC, updated_at DESC")
    List<NoteEntity> getAllNotesDesc();

    @Query("SELECT * FROM notes_room ORDER BY pinned DESC, updated_at ASC")
    List<NoteEntity> getAllNotesAsc();

    @Query("SELECT * FROM notes_room ORDER BY pinned DESC, title ASC")
    List<NoteEntity> getAllNotesTitleAsc();

    @Query("SELECT * FROM notes_room WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY pinned DESC, updated_at DESC")
    List<NoteEntity> searchNotesDesc(String query);

    @Query("SELECT * FROM notes_room WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY pinned DESC, updated_at ASC")
    List<NoteEntity> searchNotesAsc(String query);

    @Query("SELECT * FROM notes_room WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY pinned DESC, title ASC")
    List<NoteEntity> searchNotesTitleAsc(String query);
}
