package com.airesumebuilder;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.airesumebuilder.database.DatabaseHelper;
import com.airesumebuilder.notifications.NotificationChannels;
import com.airesumebuilder.utils.PreferenceManager;

/**
 * Application class for AI Resume Builder.
 * Initialises global singletons and applies the saved theme on startup.
 */
public class AIResumeApp extends Application {

    private static AIResumeApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Apply saved theme before any activity starts
        applyTheme();

        // Pre-initialise the database
        DatabaseHelper.getInstance(this);

        // Create notification channels (safe to call repeatedly)
        NotificationChannels.createAll(this);
    }

    public static AIResumeApp getInstance() {
        return instance;
    }

    /** Reads the saved theme preference and applies the matching Night Mode. */
    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(PreferenceManager.PREF_FILE, MODE_PRIVATE);
        String theme = prefs.getString(PreferenceManager.KEY_THEME,
                PreferenceManager.THEME_SYSTEM);

        switch (theme) {
            case PreferenceManager.THEME_DARK:
            case PreferenceManager.THEME_AMOLED:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case PreferenceManager.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
