package com.airesumebuilder.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.network.GroqClient;
import com.airesumebuilder.security.SecurityHelper;
import com.airesumebuilder.utils.PreferenceManager;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AI Settings screen for managing Groq API key and model parameters.
 */
public class AiSettingsActivity extends AppCompatActivity {

    private SecurityHelper    security;
    private PreferenceManager prefs;
    private GroqClient        groqClient;

    private TextInputEditText    etApiKey;
    private AutoCompleteTextView actvModel;
    private Slider               sliderTemperature;
    private TextView             tvTemperatureValue;
    private TextInputEditText    etMaxTokens;
    private SwitchMaterial       switchStreaming;

    private static final String[] MODELS = {
        "llama-3.3-70b-versatile",
        "llama-3.1-70b-versatile",
        "llama-3.1-8b-instant",
        "gemma2-9b-it",
        "mixtral-8x7b-32768"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_settings);

        security   = new SecurityHelper(this);
        prefs      = new PreferenceManager(this);
        groqClient = GroqClient.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etApiKey           = findViewById(R.id.etApiKey);
        actvModel          = findViewById(R.id.actvModel);
        sliderTemperature  = findViewById(R.id.sliderTemperature);
        tvTemperatureValue = findViewById(R.id.tvTemperatureValue);
        etMaxTokens        = findViewById(R.id.etMaxTokens);
        switchStreaming     = findViewById(R.id.switchStreaming);

        setupModelDropdown();
        loadCurrentSettings();

        // Slider label
        sliderTemperature.addOnChangeListener((slider, value, fromUser) ->
                tvTemperatureValue.setText(String.format("%.1f", value)));

        // Buttons
        ((MaterialButton) findViewById(R.id.btnSaveKey))
                .setOnClickListener(v -> saveApiKey());
        ((MaterialButton) findViewById(R.id.btnTestKey))
                .setOnClickListener(v -> testApiKey());
        ((MaterialButton) findViewById(R.id.btnDeleteKey))
                .setOnClickListener(v -> deleteApiKey());
        ((MaterialButton) findViewById(R.id.btnGetApiKey))
                .setOnClickListener(v -> openGroqConsole());
    }

    private void setupModelDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, MODELS);
        actvModel.setAdapter(adapter);
    }

    private void loadCurrentSettings() {
        // Show masked API key if one exists
        if (security.hasApiKey()) {
            String key = security.getApiKey();
            if (key != null && key.length() > 8) {
                etApiKey.setHint("Key saved (" + key.substring(0, 4) + "…" +
                        key.substring(key.length() - 4) + ")");
            }
        }

        actvModel.setText(prefs.getAiModel(), false);
        sliderTemperature.setValue(prefs.getAiTemperature());
        tvTemperatureValue.setText(String.format("%.1f", prefs.getAiTemperature()));
        etMaxTokens.setText(String.valueOf(prefs.getAiMaxTokens()));
        switchStreaming.setChecked(prefs.isStreamingEnabled());
    }

    private void saveApiKey() {
        String key = UiUtils.getText(etApiKey);
        if (key.isEmpty()) {
            UiUtils.showSnackbar(
                    findViewById(android.R.id.content), "Enter an API key first");
            return;
        }
        security.saveApiKey(key);

        // Save model params
        prefs.putString(PreferenceManager.KEY_AI_MODEL,
                UiUtils.getText(actvModel));
        prefs.putFloat(PreferenceManager.KEY_AI_TEMPERATURE,
                sliderTemperature.getValue());
        try {
            prefs.putInt(PreferenceManager.KEY_AI_MAX_TOKENS,
                    Integer.parseInt(UiUtils.getText(etMaxTokens)));
        } catch (NumberFormatException ignored) {}
        prefs.putBoolean(PreferenceManager.KEY_AI_STREAMING,
                switchStreaming.isChecked());

        etApiKey.setText("");
        UiUtils.showSnackbar(
                findViewById(android.R.id.content), getString(R.string.api_key_saved));
    }

    private void testApiKey() {
        UiUtils.showSnackbar(
                findViewById(android.R.id.content), "Testing API key…");
        groqClient.testApiKey(new GroqClient.AiCallback() {
            @Override public void onSuccess(String content) {
                runOnUiThread(() -> UiUtils.showSnackbar(
                        findViewById(android.R.id.content), "✓ API key is valid"));
            }
            @Override public void onError(String errorMessage) {
                runOnUiThread(() -> UiUtils.showSnackbar(
                        findViewById(android.R.id.content), "✗ " + errorMessage));
            }
        });
    }

    private void deleteApiKey() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete API Key")
                .setMessage("Remove the saved Groq API key?")
                .setPositiveButton("Delete", (d, w) -> {
                    security.deleteApiKey();
                    UiUtils.showSnackbar(
                            findViewById(android.R.id.content), "API key deleted");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openGroqConsole() {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://console.groq.com/keys")));
    }
}
