package com.social7.androidnoteapp;

import com.social7.androidnoteapp.model.Note;
import com.social7.androidnoteapp.preferences.preferences;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Local Unit Tests for Note model logic, default values, and preference constants.
 */
public class NoteUnitTest {

    @Test
    public void noteCreation_defaultValues_areCorrect() {
        Note note = new Note();
        assertEquals("Personal", note.getCategory());
        assertFalse(note.isFavorite());
        assertFalse(note.isPinned());
        assertTrue(note.getCreatedAt() > 0);
        assertTrue(note.getUpdatedAt() > 0);
    }

    @Test
    public void noteCreation_customValues_areCorrect() {
        long now = System.currentTimeMillis();
        Note note = new Note(1, "Study Note", "Algorithms & Data Structures", "Study", true, true, now, now);

        assertEquals(1, note.getId());
        assertEquals("Study Note", note.getTitle());
        assertEquals("Algorithms & Data Structures", note.getContent());
        assertEquals("Study", note.getCategory());
        assertTrue(note.isFavorite());
        assertTrue(note.isPinned());
        assertEquals(now, note.getCreatedAt());
        assertEquals(now, note.getUpdatedAt());
    }

    @Test
    public void noteSetters_updateFieldsCorrectly() {
        Note note = new Note();
        note.setId(42);
        note.setTitle("Updated Title");
        note.setContent("Updated Content");
        note.setCategory("Work");
        note.setFavorite(true);
        note.setPinned(true);

        long updatedTime = System.currentTimeMillis() + 1000;
        note.setUpdatedAt(updatedTime);

        assertEquals(42, note.getId());
        assertEquals("Updated Title", note.getTitle());
        assertEquals("Updated Content", note.getContent());
        assertEquals("Work", note.getCategory());
        assertTrue(note.isFavorite());
        assertTrue(note.isPinned());
        assertEquals(updatedTime, note.getUpdatedAt());
    }

    @Test
    public void preferences_constantKeys_areCorrect() {
        assertEquals("sqlite", preferences.DB_MODE_SQLITE);
        assertEquals("room", preferences.DB_MODE_ROOM);
        assertEquals("pref_dark_mode", preferences.KEY_DARK_MODE);
        assertEquals("pref_db_mode", preferences.KEY_DB_MODE);
    }
}
