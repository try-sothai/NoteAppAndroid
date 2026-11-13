package com.social7.androidnoteapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room Database Singleton configuration.
 */
@Database(entities = {NoteEntity.class}, version = 1, exportSchema = false)
public abstract class NoteRoomDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "notes_room.db";
    private static volatile NoteRoomDatabase INSTANCE;

    public abstract NoteDao noteDao();

    public static NoteRoomDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (NoteRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NoteRoomDatabase.class,
                            DATABASE_NAME
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
