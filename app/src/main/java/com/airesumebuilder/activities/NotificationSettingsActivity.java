package com.airesumebuilder.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.utils.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Notification settings screen.
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private PreferenceManager prefs;

    private static final String KEY_NOTIF_REMINDERS = "notif_reminders";
    private static final String KEY_NOTIF_TIPS       = "notif_tips";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        prefs = new PreferenceManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bindSwitch(R.id.switchReminders, KEY_NOTIF_REMINDERS);
        bindSwitch(R.id.switchTips,      KEY_NOTIF_TIPS);
    }

    private void bindSwitch(int id, String key) {
        SwitchMaterial sw = findViewById(id);
        if (sw == null) return;
        sw.setChecked(prefs.getBoolean(key, true));
        sw.setOnCheckedChangeListener((btn, checked) -> prefs.putBoolean(key, checked));
    }
}
