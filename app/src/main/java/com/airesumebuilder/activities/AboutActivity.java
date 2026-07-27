package com.airesumebuilder.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * About screen showing app version and links.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Version label
        TextView tvVersion = findViewById(R.id.tvVersion);
        if (tvVersion != null) {
            try {
                String ver = getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName;
                tvVersion.setText("Version " + ver);
            } catch (Exception ignored) {
                tvVersion.setText("Version 1.0.0");
            }
        }

        MaterialButton btnPrivacy = findViewById(R.id.btnPrivacy);
        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://example.com/privacy"))));
        }
    }
}
