package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

/**
 * Settings hub that routes to individual settings screens.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Route cards to their destinations
        card(R.id.cardProfile,       ProfileActivity.class);
        card(R.id.cardAiSettings,    AiSettingsActivity.class);
        card(R.id.cardAppearance,    AppearanceActivity.class);
        card(R.id.cardSecurity,      SecurityActivity.class);
        card(R.id.cardBackup,        BackupActivity.class);
        card(R.id.cardNotifications, NotificationSettingsActivity.class);
        card(R.id.cardAbout,         AboutActivity.class);
    }

    private void card(int id, Class<?> target) {
        MaterialCardView card = findViewById(id);
        if (card != null) {
            card.setOnClickListener(v -> startActivity(new Intent(this, target)));
        }
    }
}
