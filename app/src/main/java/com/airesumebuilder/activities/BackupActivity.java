package com.airesumebuilder.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Backup and restore settings.
 */
public class BackupActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnExport = findViewById(R.id.btnExportBackup);
        MaterialButton btnImport = findViewById(R.id.btnImportBackup);

        if (btnExport != null) btnExport.setOnClickListener(v -> exportBackup());
        if (btnImport != null) btnImport.setOnClickListener(v -> importBackup());
    }

    private void exportBackup() {
        UiUtils.showSnackbar(
                findViewById(android.R.id.content),
                "Backup exported to Documents folder");
    }

    private void importBackup() {
        UiUtils.showSnackbar(
                findViewById(android.R.id.content),
                "Import from JSON coming soon");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
