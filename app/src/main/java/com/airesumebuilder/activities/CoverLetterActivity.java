package com.airesumebuilder.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.network.GroqClient;
import com.airesumebuilder.repositories.ResumeRepository;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cover Letter generator powered by Groq AI.
 */
public class CoverLetterActivity extends AppCompatActivity {

    private ResumeRepository   resumeRepo;
    private GroqClient         groqClient;
    private List<Resume>       resumes = new ArrayList<>();

    private AutoCompleteTextView actvResume;
    private TextInputEditText    etJobDescription, etCompany, etPosition, etHiringManager;
    private AutoCompleteTextView actvTone;
    private TextView             tvResult;
    private View                 llLoading;
    private MaterialButton       btnGenerate, btnCopy;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover_letter);

        resumeRepo = new ResumeRepository(this);
        groqClient = GroqClient.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        actvResume       = findViewById(R.id.actvResume);
        etJobDescription = findViewById(R.id.etJobDescription);
        etCompany        = findViewById(R.id.etCompany);
        etPosition       = findViewById(R.id.etPosition);
        etHiringManager  = findViewById(R.id.etHiringManager);
        actvTone         = findViewById(R.id.actvTone);
        tvResult         = findViewById(R.id.tvResult);
        llLoading        = findViewById(R.id.llLoading);
        btnGenerate      = findViewById(R.id.btnGenerate);
        btnCopy          = findViewById(R.id.btnCopy);

        setupDropdowns();
        loadResumes();

        btnGenerate.setOnClickListener(v -> generateCoverLetter());
        if (btnCopy != null) {
            btnCopy.setOnClickListener(v -> {
                String text = tvResult.getText().toString();
                if (!text.isEmpty()) {
                    UiUtils.copyToClipboard(this,
                            findViewById(android.R.id.content), text);
                }
            });
        }
    }

    private void setupDropdowns() {
        String[] tones = {"Professional", "Friendly", "Formal", "Enthusiastic", "Concise"};
        AutoCompleteTextView tone = actvTone;
        if (tone != null) {
            tone.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, tones));
            tone.setText("Professional", false);
        }
    }

    private void loadResumes() {
        executor.execute(() -> {
            resumes = resumeRepo.getAll();
            List<String> titles = new ArrayList<>();
            for (Resume r : resumes) titles.add(r.getTitle());
            runOnUiThread(() -> {
                if (actvResume != null) {
                    actvResume.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, titles));
                }
            });
        });
    }

    private void generateCoverLetter() {
        String company   = etCompany       != null ? UiUtils.getText(etCompany)        : "";
        String position  = etPosition      != null ? UiUtils.getText(etPosition)       : "";
        String jobDesc   = etJobDescription != null ? UiUtils.getText(etJobDescription) : "";
        String manager   = etHiringManager  != null ? UiUtils.getText(etHiringManager)  : "";
        String tone      = actvTone         != null ? UiUtils.getText(actvTone)          : "Professional";

        if (position.isEmpty() || company.isEmpty()) {
            UiUtils.showSnackbar(
                    findViewById(android.R.id.content), "Enter company and position");
            return;
        }

        if (llLoading != null) llLoading.setVisibility(View.VISIBLE);
        btnGenerate.setEnabled(false);

        String prompt = "Write a professional cover letter for a " + position +
                " position at " + company + "." +
                (manager.isEmpty() ? "" : " Addressed to " + manager + ".") +
                (jobDesc.isEmpty() ? "" : " Job description: " + jobDesc) +
                " Tone: " + tone + ". Make it compelling and concise.";

        groqClient.complete(
                "You are an expert cover letter writer. Write compelling, personalised " +
                "cover letters that showcase the candidate's strengths.",
                prompt,
                new GroqClient.AiCallback() {
                    @Override public void onSuccess(String content) {
                        runOnUiThread(() -> {
                            if (llLoading != null) llLoading.setVisibility(View.GONE);
                            btnGenerate.setEnabled(true);
                            tvResult.setText(content);
                            tvResult.setVisibility(View.VISIBLE);
                            if (btnCopy != null) btnCopy.setVisibility(View.VISIBLE);
                        });
                    }
                    @Override public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            if (llLoading != null) llLoading.setVisibility(View.GONE);
                            btnGenerate.setEnabled(true);
                            UiUtils.showSnackbar(
                                    findViewById(android.R.id.content), errorMessage);
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
