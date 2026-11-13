package com.social7.androidnoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.androidnoteapp.adapter.NoteAdapter;
import com.social7.androidnoteapp.database.NoteDao;
import com.social7.androidnoteapp.database.NoteEntity;
import com.social7.androidnoteapp.database.NoteRoomDatabase;
import com.social7.androidnoteapp.database.SQLiteHelper;
import com.social7.androidnoteapp.model.Note;
import com.social7.androidnoteapp.preferences.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private EditText etSearch;
    private ImageButton btnClearSearch, btnSettings, btnExportImport, btnMore;
    private RecyclerView recyclerNotes;
    private TextView tvEmpty, btnDbBadge, tvNoteCount;
    private LinearLayout layoutEmpty;
    private MaterialButton btnAddNote;

    private TextView btnFilterAll, btnFilterStudy, btnFilterPersonal, btnFilterWork, btnFilterShopping, btnFilterOther;
    private String selectedCategory = "All";

    private NoteAdapter noteAdapter;
    private preferences prefs;
    private SQLiteHelper sqliteHelper;
    private NoteDao noteDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<Intent> activityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                loadNotes();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        prefs = new preferences(this);
        preferences.applyDarkMode(prefs.isDarkMode());

        sqliteHelper = new SQLiteHelper(this);
        noteDao = NoteRoomDatabase.getInstance(this).noteDao();

        initViews();
        setupRecyclerView();
        setupListeners();
        setupCategoryFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDbBadge();
        loadNotes();
    }

    private void initViews() {
        tvNoteCount = findViewById(R.id.tvNoteCount);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnExportImport = findViewById(R.id.btnExportImport);
        btnSettings = findViewById(R.id.btnSettings);
        btnMore = findViewById(R.id.btnMore);
        btnDbBadge = findViewById(R.id.btnDbBadge);
        recyclerNotes = findViewById(R.id.recyclerNotes);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAddNote = findViewById(R.id.btnAddNote);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterStudy = findViewById(R.id.btnFilterStudy);
        btnFilterPersonal = findViewById(R.id.btnFilterPersonal);
        btnFilterWork = findViewById(R.id.btnFilterWork);
        btnFilterShopping = findViewById(R.id.btnFilterShopping);
        btnFilterOther = findViewById(R.id.btnFilterOther);
    }

    private void setupRecyclerView() {
        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(this, this);
        recyclerNotes.setAdapter(noteAdapter);
    }

    private void setupListeners() {
        btnAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoteEditorActivity.class);
            activityLauncher.launch(intent);
        });

        btnExportImport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExportImportActivity.class);
            activityLauncher.launch(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            activityLauncher.launch(intent);
        });

        btnMore.setOnClickListener(this::showMorePopupMenu);

        btnDbBadge.setOnClickListener(v -> toggleDbMode());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    btnClearSearch.setVisibility(View.GONE);
                }
                loadNotes();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            loadNotes();
        });
    }

    private void showMorePopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("Export / Import Notes");
        popupMenu.getMenu().add("Sort Notes");
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("About");

        popupMenu.setOnMenuItemClickListener(item -> {
            CharSequence title = item.getTitle();
            if ("Export / Import Notes".equals(title)) {
                activityLauncher.launch(new Intent(this, ExportImportActivity.class));
            } else if ("Sort Notes".equals(title)) {
                showSortDialog();
            } else if ("Settings".equals(title)) {
                activityLauncher.launch(new Intent(this, SettingsActivity.class));
            } else if ("About".equals(title)) {
                startActivity(new Intent(this, AboutActivity.class));
            }
            return true;
        });

        popupMenu.show();
    }

    private void setupCategoryFilters() {
        TextView[] filterButtons = {btnFilterAll, btnFilterStudy, btnFilterPersonal, btnFilterWork, btnFilterShopping, btnFilterOther};
        String[] categoryNames = {"All", "Study", "Personal", "Work", "Shopping", "Other"};

        for (int i = 0; i < filterButtons.length; i++) {
            final String category = categoryNames[i];
            final TextView btn = filterButtons[i];
            btn.setOnClickListener(v -> {
                selectedCategory = category;
                updateCategoryFilterStyles(filterButtons, btn);
                loadNotes();
            });
        }
    }

    private void updateCategoryFilterStyles(TextView[] buttons, TextView selectedBtn) {
        for (TextView b : buttons) {
            if (b == selectedBtn) {
                b.setBackgroundResource(R.drawable.bg_chip_selected);
                b.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                b.setBackgroundResource(R.drawable.bg_chip_unselected);
                b.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
        }
    }

    private void updateDbBadge() {
        boolean isRoom = preferences.DB_MODE_ROOM.equals(prefs.getDbMode());
        btnDbBadge.setText(isRoom ? "Room DB" : "SQLite DB");
    }

    private void toggleDbMode() {
        boolean isRoom = preferences.DB_MODE_ROOM.equals(prefs.getDbMode());
        String newMode = isRoom ? preferences.DB_MODE_SQLITE : preferences.DB_MODE_ROOM;
        prefs.setDbMode(newMode);
        updateDbBadge();
        Toast.makeText(this, "Switched engine to: " + (isRoom ? "SQLite" : "Room"), Toast.LENGTH_SHORT).show();
        loadNotes();
    }

    private void loadNotes() {
        String searchQuery = etSearch.getText().toString().trim();
        String sortOrder = prefs.getSortOrder();
        boolean isRoom = preferences.DB_MODE_ROOM.equals(prefs.getDbMode());

        executorService.execute(() -> {
            List<Note> allNotes = new ArrayList<>();
            if (isRoom) {
                List<NoteEntity> entities;
                if (searchQuery.isEmpty()) {
                    if ("date_asc".equals(sortOrder)) {
                        entities = noteDao.getAllNotesAsc();
                    } else if ("title_asc".equals(sortOrder)) {
                        entities = noteDao.getAllNotesTitleAsc();
                    } else {
                        entities = noteDao.getAllNotesDesc();
                    }
                } else {
                    if ("date_asc".equals(sortOrder)) {
                        entities = noteDao.searchNotesAsc(searchQuery);
                    } else if ("title_asc".equals(sortOrder)) {
                        entities = noteDao.searchNotesTitleAsc(searchQuery);
                    } else {
                        entities = noteDao.searchNotesDesc(searchQuery);
                    }
                }
                for (NoteEntity entity : entities) {
                    allNotes.add(entity.toNote());
                }
            } else {
                if (searchQuery.isEmpty()) {
                    allNotes = sqliteHelper.getAllNotes(sortOrder);
                } else {
                    allNotes = sqliteHelper.searchNotes(searchQuery, sortOrder);
                }
            }

            List<Note> filteredNotes = new ArrayList<>();
            if ("All".equalsIgnoreCase(selectedCategory)) {
                filteredNotes.addAll(allNotes);
            } else {
                for (Note n : allNotes) {
                    if (selectedCategory.equalsIgnoreCase(n.getCategory())) {
                        filteredNotes.add(n);
                    }
                }
            }

            final List<Note> displayNotes = filteredNotes;
            final int count = displayNotes.size();

            runOnUiThread(() -> {
                noteAdapter.setNotes(displayNotes);
                tvNoteCount.setText(count + (count == 1 ? " note" : " notes"));

                if (displayNotes.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    recyclerNotes.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    recyclerNotes.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
        intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, note.getId());
        activityLauncher.launch(intent);
    }

    @Override
    public void onFavoriteToggle(Note note) {
        note.setFavorite(!note.isFavorite());
        note.setUpdatedAt(System.currentTimeMillis());

        executorService.execute(() -> {
            if (preferences.DB_MODE_ROOM.equals(prefs.getDbMode())) {
                noteDao.update(NoteEntity.fromNote(note));
            } else {
                sqliteHelper.updateNote(note);
            }
            runOnUiThread(this::loadNotes);
        });
    }

    @Override
    public void onNoteLongClick(Note note, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("Edit Note");
        popupMenu.getMenu().add(note.isPinned() ? "Unpin Note" : "Pin Note");
        popupMenu.getMenu().add("Delete Note");

        popupMenu.setOnMenuItemClickListener(item -> {
            CharSequence title = item.getTitle();
            if ("Edit Note".equals(title)) {
                onNoteClick(note);
            } else if ("Pin Note".equals(title) || "Unpin Note".equals(title)) {
                togglePin(note);
            } else if ("Delete Note".equals(title)) {
                confirmDeleteNote(note);
            }
            return true;
        });

        popupMenu.show();
    }

    private void togglePin(Note note) {
        note.setPinned(!note.isPinned());
        note.setUpdatedAt(System.currentTimeMillis());

        executorService.execute(() -> {
            if (preferences.DB_MODE_ROOM.equals(prefs.getDbMode())) {
                noteDao.update(NoteEntity.fromNote(note));
            } else {
                sqliteHelper.updateNote(note);
            }
            runOnUiThread(this::loadNotes);
        });
    }

    private void confirmDeleteNote(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete '" + note.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executorService.execute(() -> {
                        if (preferences.DB_MODE_ROOM.equals(prefs.getDbMode())) {
                            noteDao.deleteById(note.getId());
                        } else {
                            sqliteHelper.deleteNote(note.getId());
                        }
                        runOnUiThread(this::loadNotes);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem dbItem = menu.findItem(R.id.action_db_mode);
        if (dbItem != null) {
            boolean isRoom = preferences.DB_MODE_ROOM.equals(prefs.getDbMode());
            dbItem.setTitle("Storage Engine: " + (isRoom ? "Room" : "SQLite"));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_db_mode) {
            toggleDbMode();
            return true;
        } else if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_export || id == R.id.action_import) {
            Intent intent = new Intent(this, ExportImportActivity.class);
            activityLauncher.launch(intent);
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            activityLauncher.launch(intent);
            return true;
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] options = {"Date (Newest First)", "Date (Oldest First)", "Title (A-Z)"};
        String[] keys = {"date_desc", "date_asc", "title_asc"};

        String currentSort = prefs.getSortOrder();
        int checkedItem = 0;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(currentSort)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Sort Notes By")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    prefs.setSortOrder(keys[which]);
                    dialog.dismiss();
                    loadNotes();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
