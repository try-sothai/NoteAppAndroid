package com.social7.androidnoteapp.model;

import java.io.Serializable;

/**
 * Data Model class representing a Note in the application.
 */
public class Note implements Serializable {
    private long id;
    private String title;
    private String content;
    private String category;
    private boolean favorite;
    private boolean pinned;
    private long createdAt;
    private long updatedAt;

    public Note() {
        this.category = "Personal";
        this.favorite = false;
        this.pinned = false;
        long currentTime = System.currentTimeMillis();
        this.createdAt = currentTime;
        this.updatedAt = currentTime;
    }

    public Note(long id, String title, String content, String category, boolean favorite, boolean pinned, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = (category != null && !category.isEmpty()) ? category : "Personal";
        this.favorite = favorite;
        this.pinned = pinned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Note(String title, String content, String category, boolean favorite, boolean pinned) {
        this();
        this.title = title;
        this.content = content;
        if (category != null && !category.isEmpty()) {
            this.category = category;
        }
        this.favorite = favorite;
        this.pinned = pinned;
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
