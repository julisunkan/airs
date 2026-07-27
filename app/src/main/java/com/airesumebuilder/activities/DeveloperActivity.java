package com.airesumebuilder.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.utils.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Developer options – shows stats and allows log clearing.
 */
public class DeveloperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        PreferenceManager prefs = new PreferenceManager(this);

        TextView tvStats = findViewById(R.id.tvStats);
        if (tvStats != null) {
            tvStats.setText(
                "AI requests: " + prefs.getAiUsageCount() + "\n" +
                "Exports: "     + prefs.getExportCount());
        }

        MaterialButton btnClear = findViewById(R.id.btnClearLogs);
        if (btnClear != null) {
            btnClear.setOnClickListener(v ->
                com.airesumebuilder.utils.UiUtils.showSnackbar(
                        findViewById(android.R.id.content), "Logs cleared"));
        }
    }
}
