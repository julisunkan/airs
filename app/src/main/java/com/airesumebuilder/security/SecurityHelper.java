package com.airesumebuilder.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Handles secure storage of the Groq API key (via EncryptedSharedPreferences)
 * and PIN hashing.
 */
public class SecurityHelper {

    private static final String TAG          = "SecurityHelper";
    private static final String SECURE_PREFS = "ai_resume_secure";
    private static final String KEY_API      = "groq_api_key";
    private static final String KEY_PIN      = "app_pin_hash";

    private SharedPreferences securePrefs;

    public SecurityHelper(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            securePrefs = EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFS,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

        } catch (Exception e) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences, falling back", e);
            // Fallback to plain prefs (should not happen on API 26+)
            securePrefs = context.getSharedPreferences(SECURE_PREFS, Context.MODE_PRIVATE);
        }
    }

    // ── API Key ──────────────────────────────────────────────────────────────

    /** Saves the API key securely. */
    public void saveApiKey(String apiKey) {
        securePrefs.edit().putString(KEY_API, apiKey).apply();
    }

    /** Returns the stored API key, or null if not set. */
    public String getApiKey() {
        return securePrefs.getString(KEY_API, null);
    }

    /** Returns true if an API key has been saved. */
    public boolean hasApiKey() {
        String key = getApiKey();
        return key != null && !key.isEmpty();
    }

    /** Deletes the stored API key. */
    public void deleteApiKey() {
        securePrefs.edit().remove(KEY_API).apply();
    }

    // ── PIN ──────────────────────────────────────────────────────────────────

    /**
     * Hashes and stores a PIN.
     *
     * @param pin Plain-text PIN string (4–6 digits).
     */
    public void savePin(String pin) {
        securePrefs.edit().putString(KEY_PIN, sha256(pin)).apply();
    }

    /**
     * Returns true if the provided PIN matches the stored hash.
     */
    public boolean verifyPin(String pin) {
        String stored = securePrefs.getString(KEY_PIN, null);
        if (stored == null) return false;
        return stored.equals(sha256(pin));
    }

    /** Returns true if a PIN has been set. */
    public boolean hasPin() {
        return securePrefs.getString(KEY_PIN, null) != null;
    }

    /** Clears the stored PIN. */
    public void clearPin() {
        securePrefs.edit().remove(KEY_PIN).apply();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "sha256 failed", e);
            return input; // fallback (should never happen)
        }
    }
}
