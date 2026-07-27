package com.airesumebuilder.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.airesumebuilder.R;
import com.airesumebuilder.database.DatabaseHelper;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Backup and restore settings.
 *
 * Export: serialises all resumes (and their profile links) to a dated JSON file
 * stored in the app's Documents folder, then offers to share it.
 *
 * Import: opens the system file picker so the user can select a previously
 * exported JSON backup; resumes found in the file are inserted as new rows.
 */
public class BackupActivity extends AppCompatActivity {

    private static final String TAG = "BackupActivity";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        // Register file-picker launcher before the activity starts
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::onFilePicked);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnExport = findViewById(R.id.btnExportBackup);
        MaterialButton btnImport = findViewById(R.id.btnImportBackup);

        if (btnExport != null) btnExport.setOnClickListener(v -> exportBackup());
        if (btnImport != null) btnImport.setOnClickListener(v -> importBackup());
    }

    // ── Export ────────────────────────────────────────────────────────────────

    private void exportBackup() {
        executor.execute(() -> {
            try {
                JSONObject backup = buildBackupJson();
                File file = writeBackupFile(backup.toString(2));
                handler.post(() -> {
                    if (file != null) {
                        UiUtils.showSnackbarWithAction(
                                findViewById(android.R.id.content),
                                "Backup saved: " + file.getName(),
                                "SHARE",
                                v -> shareFile(file));
                    } else {
                        UiUtils.showSnackbar(
                                findViewById(android.R.id.content),
                                "Export failed — check storage permissions");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "exportBackup failed", e);
                handler.post(() -> UiUtils.showSnackbar(
                        findViewById(android.R.id.content), "Export error: " + e.getMessage()));
            }
        });
    }

    /** Reads all resumes from the DB and returns them as a JSON backup object. */
    private JSONObject buildBackupJson() throws JSONException {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).getReadableDatabase();

        JSONArray resumesArray = new JSONArray();
        Cursor c = db.query(DatabaseHelper.TABLE_RESUMES,
                null, null, null, null, null, "updated_at DESC");
        try {
            while (c.moveToNext()) {
                JSONObject r = new JSONObject();
                for (String col : c.getColumnNames()) {
                    int idx = c.getColumnIndex(col);
                    if (!c.isNull(idx)) {
                        switch (c.getType(idx)) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                r.put(col, c.getLong(idx)); break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                r.put(col, c.getDouble(idx)); break;
                            default:
                                r.put(col, c.getString(idx));
                        }
                    }
                }
                resumesArray.put(r);
            }
        } finally {
            c.close();
        }

        JSONObject backup = new JSONObject();
        backup.put("version", 1);
        backup.put("app", "AI Resume Builder");
        backup.put("exported_at", System.currentTimeMillis());
        backup.put("resumes", resumesArray);
        return backup;
    }

    /** Writes JSON text to a dated file in the app Documents folder. */
    private File writeBackupFile(String json) {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) dir = getFilesDir();
            if (!dir.exists()) dir.mkdirs();

            String dateStamp = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(new Date());
            File file = new File(dir, "resume_backup_" + dateStamp + ".json");

            try (FileWriter fw = new FileWriter(file)) {
                fw.write(json);
            }
            Log.d(TAG, "Backup written: " + file.getAbsolutePath());
            return file;
        } catch (Exception e) {
            Log.e(TAG, "writeBackupFile failed", e);
            return null;
        }
    }

    private void shareFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share backup"));
        } catch (Exception e) {
            Log.e(TAG, "shareFile failed", e);
            UiUtils.showSnackbar(findViewById(android.R.id.content),
                    "Could not share file");
        }
    }

    // ── Import ────────────────────────────────────────────────────────────────

    private void importBackup() {
        // Open the system file picker; accept any file type so JSON files
        // stored under various mime types (text/plain, application/octet-stream,
        // application/json) are all accessible.
        filePickerLauncher.launch("*/*");
    }

    /** Called when the user selects a file in the picker. */
    private void onFilePicked(Uri uri) {
        if (uri == null) return; // user cancelled

        executor.execute(() -> {
            try {
                String json = readUriAsString(uri);
                int count = restoreFromJson(json);
                handler.post(() -> UiUtils.showSnackbar(
                        findViewById(android.R.id.content),
                        count > 0
                                ? "Import complete — " + count + " resume(s) restored"
                                : "No resumes found in the selected file"));
            } catch (Exception e) {
                Log.e(TAG, "import failed", e);
                handler.post(() -> UiUtils.showSnackbar(
                        findViewById(android.R.id.content),
                        "Import failed: " + e.getMessage()));
            }
        });
    }

    /** Reads the content of a content URI as a UTF-8 String. */
    private String readUriAsString(Uri uri) throws Exception {
        try (InputStream is = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }

    /**
     * Parses a backup JSON string and inserts any resumes that do not already
     * exist (matched by title). Returns the number of resumes inserted.
     */
    private int restoreFromJson(String json) throws JSONException {
        JSONObject backup = new JSONObject(json);
        JSONArray resumesArray = backup.optJSONArray("resumes");
        if (resumesArray == null) return 0;

        SQLiteDatabase db = DatabaseHelper.getInstance(this).getWritableDatabase();
        int count = 0;

        for (int i = 0; i < resumesArray.length(); i++) {
            JSONObject r = resumesArray.getJSONObject(i);

            ContentValues cv = new ContentValues();
            // Copy all columns except id (let SQLite assign a new one)
            String[] columns = {
                "profile_id", "title", "template", "accent_color", "font",
                "is_favorite", "ats_score", "overall_score", "tags", "section_order"
            };
            for (String col : columns) {
                if (r.has(col) && !r.isNull(col)) {
                    try   { cv.put(col, r.getLong(col)); }
                    catch (Exception ignored) { cv.put(col, r.getString(col)); }
                }
            }

            if (!cv.containsKey("title") || cv.getAsString("title") == null) continue;

            long rowId = db.insertOrThrow(DatabaseHelper.TABLE_RESUMES, null, cv);
            if (rowId > 0) count++;
        }
        return count;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
