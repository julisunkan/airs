package com.airesumebuilder.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.network.GroqClient;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Portfolio Generator – generates a professional portfolio bio and sections.
 */
public class PortfolioActivity extends AppCompatActivity {

    private GroqClient groqClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        groqClient = GroqClient.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnGenerate = findViewById(R.id.btnGenerate);
        if (btnGenerate != null) {
            btnGenerate.setOnClickListener(v -> generatePortfolio());
        }
    }

    private void generatePortfolio() {
        TextInputEditText etName  = findViewById(R.id.etName);
        TextInputEditText etTitle = findViewById(R.id.etTitle);
        TextInputEditText etSkills = findViewById(R.id.etSkills);
        TextView tvResult = findViewById(R.id.tvResult);
        View llLoading = findViewById(R.id.llLoading);

        String name   = etName  != null ? UiUtils.getText(etName)   : "";
        String title  = etTitle != null ? UiUtils.getText(etTitle)  : "";
        String skills = etSkills != null ? UiUtils.getText(etSkills) : "";

        if (name.isEmpty()) {
            UiUtils.showSnackbar(
                    findViewById(android.R.id.content), "Enter your name");
            return;
        }

        if (llLoading != null) llLoading.setVisibility(View.VISIBLE);

        String prompt = "Create a professional portfolio About Me section for " + name +
                (title.isEmpty() ? "" : ", a " + title) +
                (skills.isEmpty() ? "" : ". Key skills: " + skills) +
                ". Make it engaging, concise (150-200 words), and first-person.";

        groqClient.complete(
                "You are an expert personal branding consultant.",
                prompt,
                new GroqClient.AiCallback() {
                    @Override public void onSuccess(String content) {
                        runOnUiThread(() -> {
                            if (llLoading != null) llLoading.setVisibility(View.GONE);
                            if (tvResult  != null) {
                                tvResult.setText(content);
                                tvResult.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    @Override public void onError(String err) {
                        runOnUiThread(() -> {
                            if (llLoading != null) llLoading.setVisibility(View.GONE);
                            UiUtils.showSnackbar(
                                    findViewById(android.R.id.content), err);
                        });
                    }
                });
    }
}
