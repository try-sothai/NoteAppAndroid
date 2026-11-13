package com.social7.androidnoteapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportImportActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvActiveDbInfo;
    private Button btnExport, btnImport;

    private preferences prefs;
    private SQLiteHelper sqliteHelper;
    private NoteDao noteDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> createDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
                if (uri != null) {
                    exportNotesToUri(uri);
                }
            });

    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    importNotesFromUri(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_export_import);

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
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvActiveDbInfo = findViewById(R.id.tvActiveDbInfo);
        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport);

        String dbName = preferences.DB_MODE_ROOM.equals(prefs.getDbMode()) ? "Room Database" : "SQLite Database";
        tvActiveDbInfo.setText("Active Database: " + dbName);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnExport.setOnClickListener(v -> checkAndExportNotes());

        btnImport.setOnClickListener(v -> {
            openDocumentLauncher.launch(new String[]{"application/json", "*/*"});
        });
    }

    private void checkAndExportNotes() {
        executorService.execute(() -> {
            List<Note> notes = fetchCurrentNotes();
            runOnUiThread(() -> {
                if (notes.isEmpty()) {
                    new AlertDialog.Builder(ExportImportActivity.this)
                            .setTitle("No Notes to Export")
                            .setMessage("Your current database contains no notes to export.")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    createDocumentLauncher.launch("my_notes_backup.json");
                }
            });
        });
    }

    private List<Note> fetchCurrentNotes() {
        List<Note> notes = new ArrayList<>();
        if (preferences.DB_MODE_ROOM.equals(prefs.getDbMode())) {
            List<NoteEntity> entities = noteDao.getAllNotesDesc();
            for (NoteEntity entity : entities) {
                notes.add(entity.toNote());
            }
        } else {
            notes = sqliteHelper.getAllNotes(prefs.getSortOrder());
        }
        return notes;
    }

    private void exportNotesToUri(Uri uri) {
        executorService.execute(() -> {
            try {
                List<Note> notes = fetchCurrentNotes();
                JSONArray jsonArray = new JSONArray();

                for (Note n : notes) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", n.getId());
                    obj.put("title", n.getTitle() != null ? n.getTitle() : "");
                    obj.put("content", n.getContent() != null ? n.getContent() : "");
                    obj.put("category", n.getCategory() != null ? n.getCategory() : "Personal");
                    obj.put("favorite", n.isFavorite());
                    obj.put("pinned", n.isPinned());
                    obj.put("createdAt", n.getCreatedAt());
                    obj.put("updatedAt", n.getUpdatedAt());
                    jsonArray.put(obj);
                }

                String jsonStr = jsonArray.toString(2);
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(jsonStr.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    }
                }

                final int exportedCount = notes.size();
                runOnUiThread(() -> new AlertDialog.Builder(ExportImportActivity.this)
                        .setTitle("Export Successful")
                        .setMessage("Successfully exported " + exportedCount + " notes to JSON backup.")
                        .setPositiveButton("OK", null)
                        .show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> new AlertDialog.Builder(ExportImportActivity.this)
                        .setTitle("Export Failed")
                        .setMessage("Failed to export notes: " + e.getLocalizedMessage())
                        .setPositiveButton("OK", null)
                        .show());
            }
        });
    }

    private void importNotesFromUri(Uri uri) {
        executorService.execute(() -> {
            try {
                StringBuilder builder = new StringBuilder();
                try (InputStream is = getContentResolver().openInputStream(uri)) {
                    if (is != null) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) != -1) {
                            builder.append(new String(buffer, 0, length, StandardCharsets.UTF_8));
                        }
                    }
                }

                String jsonStr = builder.toString().trim();
                if (jsonStr.isEmpty()) {
                    throw new IllegalArgumentException("Selected file is empty.");
                }

                JSONArray jsonArray;
                if (jsonStr.startsWith("[")) {
                    jsonArray = new JSONArray(jsonStr);
                } else if (jsonStr.startsWith("{")) {
                    jsonArray = new JSONArray();
                    jsonArray.put(new JSONObject(jsonStr));
                } else {
                    throw new IllegalArgumentException("File content is not a valid JSON object or array.");
                }

                int importedCount = 0;
                boolean isRoom = preferences.DB_MODE_ROOM.equals(prefs.getDbMode());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String title = obj.optString("title", "Imported Note");
                    String content = obj.optString("content", "");
                    String category = obj.optString("category", "Personal");
                    boolean favorite = obj.optBoolean("favorite", false);
                    boolean pinned = obj.optBoolean("pinned", false);
                    long createdAt = obj.optLong("createdAt", System.currentTimeMillis());
                    long updatedAt = obj.optLong("updatedAt", System.currentTimeMillis());

                    Note note = new Note(0, title, content, category, favorite, pinned, createdAt, updatedAt);

                    if (isRoom) {
                        noteDao.insert(NoteEntity.fromNote(note));
                    } else {
                        sqliteHelper.insertNote(note);
                    }
                    importedCount++;
                }

                final int finalCount = importedCount;
                runOnUiThread(() -> new AlertDialog.Builder(ExportImportActivity.this)
                        .setTitle("Import Successful")
                        .setMessage("Successfully imported " + finalCount + " notes into " + (isRoom ? "Room Database" : "SQLite Database") + ".")
                        .setPositiveButton("OK", (dialog, which) -> setResult(RESULT_OK))
                        .show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> new AlertDialog.Builder(ExportImportActivity.this)
                        .setTitle("Import Error")
                        .setMessage("Failed to import notes. Invalid or malformed JSON file: " + e.getLocalizedMessage())
                        .setPositiveButton("OK", null)
                        .show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
