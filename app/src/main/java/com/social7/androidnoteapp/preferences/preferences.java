package com.social7.androidnoteapp.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * SharedPreferences manager for application settings.
 */
public class preferences {

    private static final String PREF_NAME = "android_note_app_prefs";

    public static final String KEY_DARK_MODE = "pref_dark_mode";
    public static final String KEY_FONT_SIZE = "pref_font_size";
    public static final String KEY_DEFAULT_CATEGORY = "pref_default_category";
    public static final String KEY_SORT_ORDER = "pref_sort_order";
    public static final String KEY_DB_MODE = "pref_db_mode"; // "sqlite" or "room"

    public static final String DB_MODE_SQLITE = "sqlite";
    public static final String DB_MODE_ROOM = "room";

    private final SharedPreferences sharedPreferences;

    public preferences(Context context) {
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        applyDarkMode(enabled);
    }

    public static void applyDarkMode(boolean enabled) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public String getFontSize() {
        return sharedPreferences.getString(KEY_FONT_SIZE, "Medium");
    }

    public void setFontSize(String fontSize) {
        sharedPreferences.edit().putString(KEY_FONT_SIZE, fontSize).apply();
    }

    public String getDefaultCategory() {
        return sharedPreferences.getString(KEY_DEFAULT_CATEGORY, "Personal");
    }

    public void setDefaultCategory(String category) {
        sharedPreferences.edit().putString(KEY_DEFAULT_CATEGORY, category).apply();
    }

    public String getSortOrder() {
        return sharedPreferences.getString(KEY_SORT_ORDER, "date_desc");
    }

    public void setSortOrder(String sortOrder) {
        sharedPreferences.edit().putString(KEY_SORT_ORDER, sortOrder).apply();
    }

    public String getDbMode() {
        return sharedPreferences.getString(KEY_DB_MODE, DB_MODE_SQLITE);
    }

    public void setDbMode(String dbMode) {
        sharedPreferences.edit().putString(KEY_DB_MODE, dbMode).apply();
    }
}
