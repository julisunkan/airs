package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.airesumebuilder.R;
import com.airesumebuilder.security.SecurityHelper;
import com.airesumebuilder.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executor;

/**
 * PIN / biometric lock screen shown when the user has security enabled.
 */
public class LockActivity extends AppCompatActivity {

    private SecurityHelper    security;
    private PreferenceManager prefs;
    private StringBuilder     pinInput = new StringBuilder();
    private TextView          tvError;
    private View[]            dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        security = new SecurityHelper(this);
        prefs    = new PreferenceManager(this);

        tvError = findViewById(R.id.tvErrorPin);
        dots    = new View[]{
            findViewById(R.id.dot1), findViewById(R.id.dot2),
            findViewById(R.id.dot3), findViewById(R.id.dot4),
            findViewById(R.id.dot5), findViewById(R.id.dot6)
        };

        MaterialButton btnBio = findViewById(R.id.btnBiometric);

        // Show biometric button only when enrolled
        boolean bioAvailable = prefs.isBiometricEnabled() &&
                BiometricManager.from(this).canAuthenticate(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK)
                        == BiometricManager.BIOMETRIC_SUCCESS;

        if (bioAvailable) {
            btnBio.setVisibility(View.VISIBLE);
            btnBio.setOnClickListener(v -> showBiometricPrompt());
            // Auto-trigger biometric on first open
            showBiometricPrompt();
        } else {
            btnBio.setVisibility(View.GONE);
        }

        buildKeypad();
    }

    // ── PIN keypad ────────────────────────────────────────────────────────────

    private void buildKeypad() {
        androidx.gridlayout.widget.GridLayout grid = findViewById(R.id.gridPin);
        grid.setColumnCount(3);

        String[] keys = {"1","2","3","4","5","6","7","8","9","⌫","0","✓"};
        for (String key : keys) {
            MaterialButton btn = new MaterialButton(this,
                    null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(key);
            btn.setTextSize(20);

            androidx.gridlayout.widget.GridLayout.LayoutParams lp =
                    new androidx.gridlayout.widget.GridLayout.LayoutParams();
            lp.width  = 0;
            lp.height = 0;
            lp.columnSpec = androidx.gridlayout.widget.GridLayout.spec(
                    androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f);
            lp.rowSpec = androidx.gridlayout.widget.GridLayout.spec(
                    androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f);
            lp.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(lp);

            btn.setOnClickListener(v -> onKeyPressed(key));
            grid.addView(btn);
        }
    }

    private void onKeyPressed(String key) {
        switch (key) {
            case "⌫":
                if (pinInput.length() > 0) {
                    pinInput.deleteCharAt(pinInput.length() - 1);
                    updateDots();
                }
                break;
            case "✓":
                validatePin();
                break;
            default:
                if (pinInput.length() < 6) {
                    pinInput.append(key);
                    updateDots();
                    if (pinInput.length() == 6) validatePin();
                }
                break;
        }
    }

    private void updateDots() {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(
                    i < pinInput.length()
                            ? R.drawable.bg_pin_dot_filled
                            : R.drawable.bg_pin_dot_empty);
        }
    }

    private void validatePin() {
        String pin = pinInput.toString();
        if (security.verifyPin(pin)) {
            unlockApp();
        } else {
            tvError.setText(getString(R.string.wrong_pin));
            pinInput.setLength(0);
            updateDots();
        }
    }

    // ── Biometric prompt ──────────────────────────────────────────────────────

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt prompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        unlockApp();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // Fall back to PIN silently
                    }
                });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setNegativeButtonText(getString(R.string.unlock_with_pin))
                .build();

        prompt.authenticate(info);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void unlockApp() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent dismissing the lock screen with Back
        finishAffinity();
    }
}
