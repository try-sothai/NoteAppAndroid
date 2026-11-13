package com.social7.androidnoteapp.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.social7.androidnoteapp.model.Note;

/**
 * Room Database Entity for Note storage.
 */
@Entity(tableName = "notes_room")
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "favorite")
    private boolean favorite;

    @ColumnInfo(name = "pinned")
    private boolean pinned;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public NoteEntity() {
    }

    public NoteEntity(long id, String title, String content, String category, boolean favorite, boolean pinned, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.favorite = favorite;
        this.pinned = pinned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NoteEntity fromNote(Note note) {
        if (note == null) return null;
        return new NoteEntity(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCategory(),
                note.isFavorite(),
                note.isPinned(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    public Note toNote() {
        return new Note(id, title, content, category, favorite, pinned, createdAt, updatedAt);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
