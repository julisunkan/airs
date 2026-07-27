package com.airesumebuilder.activities;

import android.os.Bundle;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.security.SecurityHelper;
import com.airesumebuilder.utils.PreferenceManager;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Security settings – PIN lock and biometric unlock management.
 */
public class SecurityActivity extends AppCompatActivity {

    private SecurityHelper    security;
    private PreferenceManager prefs;
    private SwitchMaterial    switchPin, switchBio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        security = new SecurityHelper(this);
        prefs    = new PreferenceManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        switchPin = findViewById(R.id.switchPinLock);
        switchBio = findViewById(R.id.switchBiometric);

        if (switchPin != null) {
            switchPin.setChecked(prefs.isPinEnabled());
            switchPin.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) showSetPinDialog();
                else         disablePin();
            });
        }

        if (switchBio != null) {
            switchBio.setChecked(prefs.isBiometricEnabled());
            switchBio.setOnCheckedChangeListener((btn, checked) ->
                    prefs.putBoolean(PreferenceManager.KEY_BIO_ENABLED, checked));
        }
    }

    private void showSetPinDialog() {
        TextInputLayout til = new TextInputLayout(this);
        TextInputEditText et = new TextInputEditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        et.setHint("Enter 4-6 digit PIN");
        til.addView(et);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.create_pin))
                .setView(til)
                .setPositiveButton("Set PIN", (d, w) -> {
                    String pin = UiUtils.getText(et);
                    if (pin.length() >= 4) {
                        security.savePin(pin);
                        prefs.putBoolean(PreferenceManager.KEY_PIN_ENABLED, true);
                        UiUtils.showSnackbar(
                                findViewById(android.R.id.content),
                                getString(R.string.pin_set));
                    } else {
                        switchPin.setChecked(false);
                        UiUtils.showSnackbar(
                                findViewById(android.R.id.content),
                                "PIN must be at least 4 digits");
                    }
                })
                .setNegativeButton("Cancel", (d, w) -> switchPin.setChecked(false))
                .show();
    }

    private void disablePin() {
        security.clearPin();
        prefs.putBoolean(PreferenceManager.KEY_PIN_ENABLED, false);
        UiUtils.showSnackbar(
                findViewById(android.R.id.content), "PIN lock disabled");
    }
}
