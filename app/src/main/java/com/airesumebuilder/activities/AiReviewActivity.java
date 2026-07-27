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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI Resume Review screen – analyses the selected resume and returns scores + suggestions.
 */
public class AiReviewActivity extends AppCompatActivity {

    public static final String EXTRA_RESUME_ID = "resume_id";

    private ResumeRepository  resumeRepo;
    private GroqClient        groqClient;
    private List<Resume>      resumes = new ArrayList<>();
    private Resume            selectedResume;

    private AutoCompleteTextView actvResume;
    private MaterialButton       btnAnalyze;
    private MaterialCardView     cardScore;
    private TextView             tvOverallScore, tvAtsScore, tvKeywordScore, tvSuggestions;
    private View                 llLoading;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_review);

        resumeRepo  = new ResumeRepository(this);
        groqClient  = GroqClient.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        actvResume      = findViewById(R.id.actvResume);
        btnAnalyze      = findViewById(R.id.btnAnalyze);
        cardScore       = findViewById(R.id.cardScore);
        tvOverallScore  = findViewById(R.id.tvOverallScore);
        tvAtsScore      = findViewById(R.id.tvAtsScore);
        tvKeywordScore  = findViewById(R.id.tvKeywordScore);
        tvSuggestions   = findViewById(R.id.tvSuggestions);
        llLoading       = findViewById(R.id.llLoading);

        loadResumes();
        btnAnalyze.setOnClickListener(v -> analyzeResume());

        // Pre-select if launched with a specific resume
        long resumeId = getIntent().getLongExtra(EXTRA_RESUME_ID, -1L);
        if (resumeId > 0) {
            executor.execute(() -> {
                selectedResume = resumeRepo.getById(resumeId);
                runOnUiThread(() -> {
                    if (selectedResume != null) {
                        actvResume.setText(selectedResume.getTitle(), false);
                    }
                });
            });
        }
    }

    private void loadResumes() {
        executor.execute(() -> {
            resumes = resumeRepo.getAll();
            List<String> titles = new ArrayList<>();
            for (Resume r : resumes) titles.add(r.getTitle());
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, titles);
                actvResume.setAdapter(adapter);
                actvResume.setOnItemClickListener((parent, view, pos, id) -> {
                    selectedResume = resumes.get(pos);
                });
            });
        });
    }

    private void analyzeResume() {
        if (selectedResume == null) {
            UiUtils.showSnackbar(findViewById(android.R.id.content), "Please select a resume");
            return;
        }

        llLoading.setVisibility(View.VISIBLE);
        btnAnalyze.setEnabled(false);
        cardScore.setVisibility(View.GONE);
        tvSuggestions.setVisibility(View.GONE);

        String prompt = buildReviewPrompt(selectedResume);

        groqClient.complete(
            "You are a professional resume reviewer and ATS expert. " +
            "Analyse the resume data provided and return a structured review with: " +
            "1) ATS Score (0-100), 2) Keyword Score (0-100), 3) Overall Score (0-100), " +
            "4) Top 5 improvement suggestions. Format the scores as: " +
            "ATS_SCORE: XX, KEYWORD_SCORE: XX, OVERALL_SCORE: XX, then list the suggestions.",
            prompt,
            new GroqClient.AiCallback() {
                @Override
                public void onSuccess(String content) {
                    runOnUiThread(() -> displayResults(content));
                }
                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        llLoading.setVisibility(View.GONE);
                        btnAnalyze.setEnabled(true);
                        UiUtils.showSnackbar(
                                findViewById(android.R.id.content), errorMessage);
                    });
                }
            });
    }

    private String buildReviewPrompt(Resume r) {
        return "Resume Title: " + r.getTitle() + "\n" +
               "Template: " + r.getTemplate() + "\n" +
               "Please review this resume and provide detailed feedback.";
    }

    private void displayResults(String content) {
        llLoading.setVisibility(View.GONE);
        btnAnalyze.setEnabled(true);

        // Parse scores from AI response
        int ats     = parseScore(content, "ATS_SCORE:");
        int keyword = parseScore(content, "KEYWORD_SCORE:");
        int overall = parseScore(content, "OVERALL_SCORE:");

        if (ats > 0 || overall > 0) {
            tvOverallScore.setText(String.valueOf(overall > 0 ? overall : "--"));
            tvAtsScore.setText(ats > 0 ? ats + "%" : "--");
            tvKeywordScore.setText(keyword > 0 ? keyword + "%" : "--");
            cardScore.setVisibility(View.VISIBLE);

            // Save scores to DB
            final int finalAts     = ats;
            final int finalOverall = overall;
            executor.execute(() ->
                resumeRepo.updateScores(selectedResume.getId(), finalAts, finalOverall));
        }

        tvSuggestions.setText(content);
        tvSuggestions.setVisibility(View.VISIBLE);
    }

    private int parseScore(String text, String label) {
        try {
            int idx = text.indexOf(label);
            if (idx < 0) return 0;
            String sub = text.substring(idx + label.length()).trim();
            StringBuilder sb = new StringBuilder();
            for (char c : sub.toCharArray()) {
                if (Character.isDigit(c)) sb.append(c);
                else if (sb.length() > 0)  break;
            }
            return sb.length() > 0 ? Integer.parseInt(sb.toString()) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
