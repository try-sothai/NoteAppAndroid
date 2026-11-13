package com.social7.androidnoteapp;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.social7.androidnoteapp.database.NoteDao;
import com.social7.androidnoteapp.database.NoteEntity;
import com.social7.androidnoteapp.database.NoteRoomDatabase;
import com.social7.androidnoteapp.database.SQLiteHelper;
import com.social7.androidnoteapp.model.Note;
import com.social7.androidnoteapp.preferences.preferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private TextView tvTitle, tvContent, tvCategory, tvCreatedAt, tvUpdatedAt, tvFavoriteStatus, tvPinnedStatus;
    private ImageButton btnBack;
    private MaterialButton btnShare, btnEdit, btnDelete;

    private long noteId = -1;
    private Note currentNote;
    private preferences prefs;
    private SQLiteHelper sqliteHelper;
    private NoteDao noteDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault());

    private final ActivityResultLauncher<Intent> editNoteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadNoteDetails();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_detail);

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
        checkIntent();
        setupListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        tvCategory = findViewById(R.id.tvCategory);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvUpdatedAt = findViewById(R.id.tvUpdatedAt);
        tvFavoriteStatus = findViewById(R.id.tvFavoriteStatus);
        tvPinnedStatus = findViewById(R.id.tvPinnedStatus);

        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_NOTE_ID)) {
            noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1);
            if (noteId != -1) {
                loadNoteDetails();
            } else {
                Toast.makeText(this, "Invalid note ID", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No note specified", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadNoteDetails() {
        executorService.execute(() -> {
            Note note = null;
            if (preferences.DB_MODE_ROOM.equals(prefs.getDbMode())) {
                NoteEntity entity = noteDao.getNoteById(noteId);
                if (entity != null) note = entity.toNote();
            } else {
                note = sqliteHelper.getNoteById(noteId);
            }

            final Note fetchedNote = note;
            runOnUiThread(() -> {
                if (fetchedNote != null) {
                    currentNote = fetchedNote;
                    displayNoteData(fetchedNote);
                } else {
                    Toast.makeText(NoteDetailActivity.this, "Note not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void displayNoteData(Note note) {
        tvTitle.setText(TextUtils.isEmpty(note.getTitle()) ? "(Untitled)" : note.getTitle());
        tvContent.setText(TextUtils.isEmpty(note.getContent()) ? "" : note.getContent());
        tvCategory.setText(note.getCategory());
        applyCategoryStyle(tvCategory, note.getCategory());

        tvCreatedAt.setText("Created: " + dateFormat.format(new Date(note.getCreatedAt())));
        tvUpdatedAt.setText("Updated: " + dateFormat.format(new Date(note.getUpdatedAt())));

        tvFavoriteStatus.setVisibility(note.isFavorite() ? View.VISIBLE : View.GONE);
        tvPinnedStatus.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);
    }

    private void applyCategoryStyle(TextView tvCategory, String category) {
        int bgColor;
        int textColor;

        if ("Study".equalsIgnoreCase(category)) {
            bgColor = ContextCompat.getColor(this, R.color.cat_study_bg);
            textColor = ContextCompat.getColor(this, R.color.cat_study_text);
        } else if ("Personal".equalsIgnoreCase(category)) {
            bgColor = ContextCompat.getColor(this, R.color.cat_personal_bg);
            textColor = ContextCompat.getColor(this, R.color.cat_personal_text);
        } else if ("Work".equalsIgnoreCase(category)) {
            bgColor = ContextCompat.getColor(this, R.color.cat_work_bg);
            textColor = ContextCompat.getColor(this, R.color.cat_work_text);
        } else if ("Shopping".equalsIgnoreCase(category)) {
            bgColor = ContextCompat.getColor(this, R.color.cat_shopping_bg);
            textColor = ContextCompat.getColor(this, R.color.cat_shopping_text);
        } else {
            bgColor = ContextCompat.getColor(this, R.color.cat_other_bg);
            textColor = ContextCompat.getColor(this, R.color.cat_other_text);
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(30f);
        drawable.setColor(bgColor);

        tvCategory.setBackground(drawable);
        tvCategory.setTextColor(textColor);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            if (currentNote != null) {
                Intent intent = new Intent(NoteDetailActivity.this, NoteEditorActivity.class);
                intent.putExtra(NoteEditorActivity.EXTRA_NOTE_ID, currentNote.getId());
                editNoteLauncher.launch(intent);
            }
        });

        btnShare.setOnClickListener(v -> shareNote());

        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void shareNote() {
        if (currentNote == null) return;
        String shareBody = currentNote.getTitle() + "\n\n" + currentNote.getContent();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentNote.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Share note via"));
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNote())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNote() {
        if (currentNote == null) return;

        executorService.execute(() -> {
            if (preferences.DB_MODE_ROOM.equals(prefs.getDbMode())) {
                noteDao.deleteById(currentNote.getId());
            } else {
                sqliteHelper.deleteNote(currentNote.getId());
            }

            runOnUiThread(() -> {
                Toast.makeText(NoteDetailActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
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
