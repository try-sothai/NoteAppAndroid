package com.social7.androidnoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.social7.androidnoteapp.preferences.preferences;

import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchDarkMode;
    private Spinner spinnerDbMode, spinnerFontSize, spinnerDefaultCategory, spinnerSortOrder;
    private ImageButton btnBack;
    private MaterialButton btnExportImportBackup;

    private preferences prefs;

    private final String[] dbModes = {"SQLite Database", "Room Database"};
    private final String[] dbModeKeys = {preferences.DB_MODE_SQLITE, preferences.DB_MODE_ROOM};

    private final String[] fontSizes = {"Small", "Medium", "Large"};

    private final String[] categories = {"Study", "Personal", "Work", "Shopping", "Other"};

    private final String[] sortOrders = {"Date (Newest First)", "Date (Oldest First)", "Title (A-Z)"};
    private final String[] sortOrderKeys = {"date_desc", "date_asc", "title_asc"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        prefs = new preferences(this);

        initViews();
        setupSpinners();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnExportImportBackup = findViewById(R.id.btnExportImportBackup);
        spinnerDbMode = findViewById(R.id.spinnerDbMode);
        spinnerFontSize = findViewById(R.id.spinnerFontSize);
        spinnerDefaultCategory = findViewById(R.id.spinnerDefaultCategory);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);
    }

    private void setupSpinners() {
        ArrayAdapter<String> dbAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dbModes);
        dbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDbMode.setAdapter(dbAdapter);

        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fontSizes);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontSize.setAdapter(fontAdapter);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDefaultCategory.setAdapter(catAdapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOrders);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortAdapter);
    }

    private void loadSettings() {
        switchDarkMode.setChecked(prefs.isDarkMode());

        String currentDbMode = prefs.getDbMode();
        int dbPos = preferences.DB_MODE_ROOM.equals(currentDbMode) ? 1 : 0;
        spinnerDbMode.setSelection(dbPos);

        String currentFontSize = prefs.getFontSize();
        int fontPos = Arrays.asList(fontSizes).indexOf(currentFontSize);
        spinnerFontSize.setSelection(fontPos >= 0 ? fontPos : 1);

        String currentCategory = prefs.getDefaultCategory();
        int catPos = Arrays.asList(categories).indexOf(currentCategory);
        spinnerDefaultCategory.setSelection(catPos >= 0 ? catPos : 1);

        String currentSort = prefs.getSortOrder();
        int sortPos = Arrays.asList(sortOrderKeys).indexOf(currentSort);
        spinnerSortOrder.setSelection(sortPos >= 0 ? sortPos : 0);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setDarkMode(isChecked);
        });

        if (btnExportImportBackup != null) {
            btnExportImportBackup.setOnClickListener(v -> {
                startActivity(new Intent(SettingsActivity.this, ExportImportActivity.class));
            });
        }

        spinnerDbMode.setOnItemSelectedListener(new SimpleSpinnerListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setDbMode(dbModeKeys[position]);
            }
        });

        spinnerFontSize.setOnItemSelectedListener(new SimpleSpinnerListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setFontSize(fontSizes[position]);
            }
        });

        spinnerDefaultCategory.setOnItemSelectedListener(new SimpleSpinnerListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setDefaultCategory(categories[position]);
            }
        });

        spinnerSortOrder.setOnItemSelectedListener(new SimpleSpinnerListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setSortOrder(sortOrderKeys[position]);
            }
        });
    }

    private abstract static class SimpleSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }
}
