package com.airesumebuilder.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.utils.PreferenceManager;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Appearance settings – theme selection (Light / Dark / AMOLED / System).
 */
public class AppearanceActivity extends AppCompatActivity {

    private PreferenceManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance);

        prefs = new PreferenceManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        setupThemeButtons();
    }

    private void setupThemeButtons() {
        setThemeButton(R.id.btnThemeLight,  PreferenceManager.THEME_LIGHT,
                AppCompatDelegate.MODE_NIGHT_NO);
        setThemeButton(R.id.btnThemeDark,   PreferenceManager.THEME_DARK,
                AppCompatDelegate.MODE_NIGHT_YES);
        setThemeButton(R.id.btnThemeAmoled, PreferenceManager.THEME_AMOLED,
                AppCompatDelegate.MODE_NIGHT_YES);
        setThemeButton(R.id.btnThemeSystem, PreferenceManager.THEME_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private void setThemeButton(int id, String theme, int nightMode) {
        MaterialButton btn = findViewById(id);
        if (btn == null) return;
        btn.setOnClickListener(v -> {
            prefs.setTheme(theme);
            AppCompatDelegate.setDefaultNightMode(nightMode);
            UiUtils.showSnackbar(
                    findViewById(android.R.id.content), "Theme changed");
        });
    }
}
