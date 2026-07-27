package com.airesumebuilder.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;

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
 *
 * The "Export PDF" button uses Android's built-in PrintManager to render the
 * WebView content as a PDF through the system print dialog.  The user can save
 * the document locally or send it directly to a printer from there.
 */
public class ResumePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_RESUME_ID = "resume_id";

    private ResumeRepository  resumeRepo;
    private Resume            resume;
    private WebView           webView;
    private boolean           pageLoaded = false;

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

            // Track when the page has finished loading so the PDF export
            // captures the fully rendered content.
            webView.setWebViewClient(new android.webkit.WebViewClient() {
                @Override
                public void onPageFinished(android.webkit.WebView view, String url) {
                    pageLoaded = true;
                }
            });
        }

        long resumeId = getIntent().getLongExtra(EXTRA_RESUME_ID, -1L);
        if (resumeId > 0) loadResume(resumeId);

        MaterialButton btnExport = findViewById(R.id.btnExport);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> exportAsPdf());
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

    // ── PDF export ────────────────────────────────────────────────────────────

    /**
     * Opens the Android system print / save-as-PDF dialog for the current
     * WebView content.  No third-party library is required — PrintManager is
     * part of the Android framework since API 19.
     */
    private void exportAsPdf() {
        if (webView == null) {
            UiUtils.showSnackbar(findViewById(android.R.id.content),
                    "Nothing to export yet");
            return;
        }

        if (!pageLoaded) {
            UiUtils.showSnackbar(findViewById(android.R.id.content),
                    "Resume is still loading — please try again in a moment");
            return;
        }

        String jobName = (resume != null && resume.getTitle() != null)
                ? resume.getTitle()
                : "Resume";

        PrintManager printManager =
                (PrintManager) getSystemService(Context.PRINT_SERVICE);

        // createPrintDocumentAdapter converts the WebView DOM to a PDF-ready
        // print adapter; the system dialog lets the user choose "Save as PDF"
        // or any installed printer.
        PrintDocumentAdapter printAdapter =
                webView.createPrintDocumentAdapter(jobName);

        PrintAttributes attributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

        printManager.print(jobName, printAdapter, attributes);
    }

    // ── HTML builder ──────────────────────────────────────────────────────────

    private String buildHtml(Resume r) {
        String color = r.getAccentColor() != null ? r.getAccentColor() : "#1565C0";
        return "<!DOCTYPE html><html><head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
               "<style>" +
               "body{font-family:sans-serif;margin:24px;color:#1a1a1a}" +
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
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
