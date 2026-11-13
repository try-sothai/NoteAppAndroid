package com.social7.androidnoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.social7.androidnoteapp.database.NoteDao;
import com.social7.androidnoteapp.database.NoteEntity;
import com.social7.androidnoteapp.database.NoteRoomDatabase;
import com.social7.androidnoteapp.database.SQLiteHelper;
import com.social7.androidnoteapp.model.Note;
import com.social7.androidnoteapp.preferences.preferences;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteEditorActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private EditText etTitle, etContent;
    private Spinner spinnerCategory;
    private CompoundButton cbFavorite, cbPinned;
    private TextView tvEditorHeader;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;

    private final String[] categories = {"Study", "Personal", "Work", "Shopping", "Other"};
    private long noteId = -1;
    private Note existingNote = null;
    private boolean isModified = false;

    private preferences prefs;
    private SQLiteHelper sqliteHelper;
    private NoteDao noteDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_editor);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        prefs = new preferences(this);
        sqliteHelper = new SQLiteHelper(this);
        noteDao = NoteRoomDatabase.getInstance(this).noteDao();

        initViews();
        setupCategorySpinner();
        checkIntent();
        setupListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleCancelOrBack();
            }
        });
    }

    private void initViews() {
        tvEditorHeader = findViewById(R.id.tvEditorHeader);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cbFavorite = findViewById(R.id.cbFavorite);
        cbPinned = findViewById(R.id.cbPinned);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        String defaultCat = prefs.getDefaultCategory();
        int defaultPos = Arrays.asList(categories).indexOf(defaultCat);
        if (defaultPos >= 0) {
            spinnerCategory.setSelection(defaultPos);
        }
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_NOTE_ID)) {
            noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1);
            if (noteId != -1) {
                tvEditorHeader.setText("Edit Note");
                loadNoteForEditing(noteId);
            }
        }
    }

    private void loadNoteForEditing(long id) {
        executorService.execute(() -> {
            Note note = null;
            if (preferences.DB_MODE_ROOM.equals(prefs.getDbMode())) {
                NoteEntity entity = noteDao.getNoteById(id);
                if (entity != null) note = entity.toNote();
            } else {
                note = sqliteHelper.getNoteById(id);
            }

            final Note loadedNote = note;
            runOnUiThread(() -> {
                if (loadedNote != null) {
                    existingNote = loadedNote;
                    etTitle.setText(loadedNote.getTitle());
                    etContent.setText(loadedNote.getContent());
                    cbFavorite.setChecked(loadedNote.isFavorite());
                    cbPinned.setChecked(loadedNote.isPinned());

                    int catPos = Arrays.asList(categories).indexOf(loadedNote.getCategory());
                    if (catPos >= 0) {
                        spinnerCategory.setSelection(catPos);
                    }
                    isModified = false;
                } else {
                    Toast.makeText(NoteEditorActivity.this, "Error loading note.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void setupListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isModified = true;
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTitle.addTextChangedListener(watcher);
        etContent.addTextChangedListener(watcher);

        cbFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> isModified = true);
        cbPinned.setOnCheckedChangeListener((buttonView, isChecked) -> isModified = true);

        btnBack.setOnClickListener(v -> handleCancelOrBack());
        btnCancel.setOnClickListener(v -> handleCancelOrBack());
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void handleCancelOrBack() {
        if (isModified) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                    .setPositiveButton("Discard", (dialog, which) -> finish())
                    .setNegativeButton("Keep Editing", null)
                    .show();
        } else {
            finish();
        }
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean favorite = cbFavorite.isChecked();
        boolean pinned = cbPinned.isChecked();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title cannot be empty");
            etTitle.requestFocus();
            return;
        }

        executorService.execute(() -> {
            long currentTime = System.currentTimeMillis();
            if (existingNote == null) {
                Note newNote = new Note(0, title, content, category, favorite, pinned, currentTime, currentTime);
                if (preferences.DB_MODE_ROOM.equals(prefs.getDbMode())) {
                    noteDao.insert(NoteEntity.fromNote(newNote));
                } else {
                    sqliteHelper.insertNote(newNote);
                }
            } else {
                existingNote.setTitle(title);
                existingNote.setContent(content);
                existingNote.setCategory(category);
                existingNote.setFavorite(favorite);
                existingNote.setPinned(pinned);
                existingNote.setUpdatedAt(currentTime);

                if (preferences.DB_MODE_ROOM.equals(prefs.getDbMode())) {
                    noteDao.update(NoteEntity.fromNote(existingNote));
                } else {
                    sqliteHelper.updateNote(existingNote);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(NoteEditorActivity.this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
