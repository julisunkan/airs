package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.security.SecurityHelper;
import com.airesumebuilder.utils.PreferenceManager;

/**
 * Splash / entry activity.
 * Shows the app logo for 1.5 s, then routes to {@link LockActivity} (if PIN is set)
 * or directly to {@link MainActivity}.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::navigate, SPLASH_DELAY_MS);
    }

    private void navigate() {
        SecurityHelper  security = new SecurityHelper(this);
        PreferenceManager prefs  = new PreferenceManager(this);

        boolean pinEnabled = prefs.isPinEnabled() && security.hasPin();
        boolean bioEnabled = prefs.isBiometricEnabled();

        Intent intent;
        if (pinEnabled || bioEnabled) {
            intent = new Intent(this, LockActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
