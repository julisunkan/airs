package com.airesumebuilder.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.repositories.ResumeRepository;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Resume preview screen rendered as HTML in a WebView.
 */
public class ResumePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_RESUME_ID = "resume_id";

    private ResumeRepository  resumeRepo;
    private Resume            resume;
    private WebView           webView;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_preview);

        resumeRepo = new ResumeRepository(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.webView);
        if (webView != null) {
            webView.getSettings().setJavaScriptEnabled(false);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
        }

        long resumeId = getIntent().getLongExtra(EXTRA_RESUME_ID, -1L);
        if (resumeId > 0) loadResume(resumeId);

        MaterialButton btnExport = findViewById(R.id.btnExport);
        if (btnExport != null) {
            btnExport.setOnClickListener(v ->
                UiUtils.showSnackbar(
                        findViewById(android.R.id.content), "PDF export coming soon"));
        }
    }

    private void loadResume(long resumeId) {
        executor.execute(() -> {
            resume = resumeRepo.getById(resumeId);
            handler.post(() -> {
                if (resume != null && webView != null) {
                    webView.loadDataWithBaseURL(null,
                            buildHtml(resume), "text/html", "UTF-8", null);
                }
            });
        });
    }

    private String buildHtml(Resume r) {
        String color = r.getAccentColor() != null ? r.getAccentColor() : "#1565C0";
        return "<!DOCTYPE html><html><head>" +
               "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
               "<style>body{font-family:sans-serif;margin:24px;color:#1a1a1a}" +
               "h1{color:" + color + ";border-bottom:2px solid " + color + ";padding-bottom:8px}" +
               "h2{color:" + color + ";font-size:16px;margin-top:20px;margin-bottom:4px}" +
               ".meta{color:#666;font-size:13px}" +
               "</style></head><body>" +
               "<h1>" + safeHtml(r.getTitle()) + "</h1>" +
               "<p class='meta'>Template: " + safeHtml(r.getTemplate()) + "</p>" +
               "<h2>Professional Summary</h2>" +
               "<p><em>Add your summary in the Resume Builder.</em></p>" +
               "<h2>Experience</h2><p><em>No experience added yet.</em></p>" +
               "<h2>Education</h2><p><em>No education added yet.</em></p>" +
               "<h2>Skills</h2><p><em>No skills added yet.</em></p>" +
               "</body></html>";
    }

    private String safeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
