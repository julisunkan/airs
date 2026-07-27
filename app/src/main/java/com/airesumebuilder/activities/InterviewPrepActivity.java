package com.airesumebuilder.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.network.GroqClient;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Interview Preparation – generates interview questions and model answers.
 */
public class InterviewPrepActivity extends AppCompatActivity {

    private GroqClient groqClient;

    private TextInputEditText    etJobTitle, etCompany;
    private AutoCompleteTextView actvType;
    private TextView             tvResult;
    private View                 llLoading;
    private MaterialButton       btnGenerate, btnCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_prep);

        groqClient = GroqClient.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etJobTitle  = findViewById(R.id.etJobTitle);
        etCompany   = findViewById(R.id.etCompany);
        actvType    = findViewById(R.id.actvType);
        tvResult    = findViewById(R.id.tvResult);
        llLoading   = findViewById(R.id.llLoading);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnCopy     = findViewById(R.id.btnCopy);

        if (actvType != null) {
            String[] types = {"HR / Behavioural", "Technical", "STAR Responses",
                              "Coding Questions", "Management", "General"};
            actvType.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, types));
            actvType.setText("HR / Behavioural", false);
        }

        if (btnGenerate != null) btnGenerate.setOnClickListener(v -> generate());
        if (btnCopy != null) btnCopy.setOnClickListener(v -> {
            String t = tvResult.getText().toString();
            if (!t.isEmpty()) UiUtils.copyToClipboard(this,
                    findViewById(android.R.id.content), t);
        });
    }

    private void generate() {
        String job     = etJobTitle != null ? UiUtils.getText(etJobTitle) : "";
        String company = etCompany  != null ? UiUtils.getText(etCompany)  : "";
        String type    = actvType   != null ? UiUtils.getText(actvType)    : "HR";

        if (job.isEmpty()) {
            UiUtils.showSnackbar(
                    findViewById(android.R.id.content), "Enter the job title");
            return;
        }

        if (llLoading != null) llLoading.setVisibility(View.VISIBLE);
        if (btnGenerate != null) btnGenerate.setEnabled(false);

        String prompt = "Generate 10 " + type + " interview questions for a " + job +
                " role" + (company.isEmpty() ? "" : " at " + company) +
                ". For each question, provide a model answer. Format clearly with Q: and A:";

        groqClient.complete(
                "You are an expert interview coach. Generate realistic, challenging " +
                "interview questions with detailed model answers.",
                prompt,
                new GroqClient.AiCallback() {
                    @Override public void onSuccess(String content) {
                        runOnUiThread(() -> {
                            if (llLoading != null) llLoading.setVisibility(View.GONE);
                            if (btnGenerate != null) btnGenerate.setEnabled(true);
                            if (tvResult != null) {
                                tvResult.setText(content);
                                tvResult.setVisibility(View.VISIBLE);
                            }
                            if (btnCopy != null) btnCopy.setVisibility(View.VISIBLE);
                        });
                    }
                    @Override public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            if (llLoading != null) llLoading.setVisibility(View.GONE);
                            if (btnGenerate != null) btnGenerate.setEnabled(true);
                            UiUtils.showSnackbar(
                                    findViewById(android.R.id.content), errorMessage);
                        });
                    }
                });
    }
}
