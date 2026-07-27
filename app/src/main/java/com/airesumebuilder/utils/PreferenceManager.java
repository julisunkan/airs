package com.airesumebuilder.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Centralised preference keys and helper methods.
 * All non-secret preferences are stored in a single SharedPreferences file.
 */
public class PreferenceManager {

    public static final String PREF_FILE = "ai_resume_prefs";

    // ─── Theme ───────────────────────────────────────────────────────────────
    public static final String KEY_THEME          = "theme";
    public static final String THEME_SYSTEM       = "system";
    public static final String THEME_LIGHT        = "light";
    public static final String THEME_DARK         = "dark";
    public static final String THEME_AMOLED       = "amoled";

    // ─── Security ────────────────────────────────────────────────────────────
    public static final String KEY_PIN_ENABLED    = "pin_enabled";
    public static final String KEY_PIN_HASH       = "pin_hash";
    public static final String KEY_BIO_ENABLED    = "bio_enabled";
    public static final String KEY_AUTO_LOCK_MIN  = "auto_lock_minutes";  // 0 = immediate

    // ─── AI defaults ─────────────────────────────────────────────────────────
    public static final String KEY_AI_MODEL       = "ai_model";
    public static final String KEY_AI_TEMPERATURE = "ai_temperature";
    public static final String KEY_AI_MAX_TOKENS  = "ai_max_tokens";
    public static final String KEY_AI_STREAMING   = "ai_streaming";

    // ─── Analytics ───────────────────────────────────────────────────────────
    public static final String KEY_AI_USAGE       = "ai_usage_count";
    public static final String KEY_EXPORT_COUNT   = "export_count";

    // ─── Misc ─────────────────────────────────────────────────────────────────
    public static final String KEY_FIRST_LAUNCH   = "first_launch";
    public static final String KEY_LAST_BACKUP    = "last_backup";
    public static final String KEY_ACTIVE_PROFILE = "active_profile_id";

    // ─── Defaults ────────────────────────────────────────────────────────────
    public static final String DEFAULT_MODEL      = "llama-3.3-70b-versatile";
    public static final float  DEFAULT_TEMPERATURE = 0.7f;
    public static final int    DEFAULT_MAX_TOKENS  = 2048;

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    // ── Generic helpers ──────────────────────────────────────────────────────

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public float getFloat(String key, float defaultValue) {
        return prefs.getFloat(key, defaultValue);
    }

    public void putFloat(String key, float value) {
        prefs.edit().putFloat(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

    public void putLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }

    // ── Convenience wrappers ─────────────────────────────────────────────────

    public String getTheme() {
        return getString(KEY_THEME, THEME_SYSTEM);
    }

    public void setTheme(String theme) {
        putString(KEY_THEME, theme);
    }

    public boolean isPinEnabled() {
        return getBoolean(KEY_PIN_ENABLED, false);
    }

    public boolean isBiometricEnabled() {
        return getBoolean(KEY_BIO_ENABLED, false);
    }

    public String getAiModel() {
        return getString(KEY_AI_MODEL, DEFAULT_MODEL);
    }

    public float getAiTemperature() {
        return getFloat(KEY_AI_TEMPERATURE, DEFAULT_TEMPERATURE);
    }

    public int getAiMaxTokens() {
        return getInt(KEY_AI_MAX_TOKENS, DEFAULT_MAX_TOKENS);
    }

    public boolean isStreamingEnabled() {
        return getBoolean(KEY_AI_STREAMING, false);
    }

    public void incrementAiUsage() {
        putInt(KEY_AI_USAGE, getInt(KEY_AI_USAGE, 0) + 1);
    }

    public int getAiUsageCount() {
        return getInt(KEY_AI_USAGE, 0);
    }

    public void incrementExportCount() {
        putInt(KEY_EXPORT_COUNT, getInt(KEY_EXPORT_COUNT, 0) + 1);
    }

    public int getExportCount() {
        return getInt(KEY_EXPORT_COUNT, 0);
    }

    public boolean isFirstLaunch() {
        return getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchDone() {
        putBoolean(KEY_FIRST_LAUNCH, false);
    }

    public long getActiveProfileId() {
        return getLong(KEY_ACTIVE_PROFILE, -1L);
    }

    public void setActiveProfileId(long id) {
        putLong(KEY_ACTIVE_PROFILE, id);
    }
}
