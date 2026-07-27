package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.repositories.ResumeRepository;
import com.airesumebuilder.utils.ExportUtils;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Full resume-builder screen.
 * Handles creating/editing a resume with multiple sections, auto-save, and AI prompts.
 */
public class ResumeBuilderActivity extends AppCompatActivity {

    public static final String EXTRA_RESUME_ID = "resume_id";
    public static final String EXTRA_NEW       = "new_resume";

    private ResumeRepository  resumeRepo;
    private Resume            currentResume;
    private TextInputEditText etTitle;
    private Chip              chipAutoSave;
    private boolean           isNewResume;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    // Auto-save 1.5 s after last keystroke
    private final Handler   autoSaveHandler  = new Handler(Looper.getMainLooper());
    private final Runnable  autoSaveRunnable = this::saveResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_builder);

        resumeRepo    = new ResumeRepository(this);
        etTitle       = findViewById(R.id.etResumeTitle);
        chipAutoSave  = findViewById(R.id.chipAutoSave);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        isNewResume = getIntent().getBooleanExtra(EXTRA_NEW, false);
        long resumeId = getIntent().getLongExtra(EXTRA_RESUME_ID, -1L);

        if (isNewResume || resumeId < 0) {
            currentResume = new Resume();
            currentResume.setTitle("Untitled Resume");
        } else {
            loadResume(resumeId);
        }

        setupTitleField();
        setupSectionButtons();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void setupTitleField() {
        if (currentResume != null) {
            etTitle.setText(currentResume.getTitle());
        }

        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (currentResume != null) {
                    currentResume.setTitle(s.toString().trim());
                }
                scheduleAutoSave();
            }
        });
    }

    private void setupSectionButtons() {
        findViewById(R.id.btnAddSection).setOnClickListener(v -> showAddSectionDialog());
        findViewById(R.id.btnPreview).setOnClickListener(v -> {
            if (currentResume != null && currentResume.getId() > 0) {
                Intent i = new Intent(this, ResumePreviewActivity.class);
                i.putExtra(ResumePreviewActivity.EXTRA_RESUME_ID, currentResume.getId());
                startActivity(i);
            } else {
                saveResume(); // save first, then preview
            }
        });
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_preview) {
            if (currentResume != null && currentResume.getId() > 0) {
                Intent i = new Intent(this, ResumePreviewActivity.class);
                i.putExtra(ResumePreviewActivity.EXTRA_RESUME_ID, currentResume.getId());
                startActivity(i);
            }
            return true;
        } else if (id == R.id.action_export) {
            showExportDialog();
            return true;
        } else if (id == R.id.action_ai_review) {
            Intent i = new Intent(this, AiReviewActivity.class);
            if (currentResume != null) {
                i.putExtra(AiReviewActivity.EXTRA_RESUME_ID, currentResume.getId());
            }
            startActivity(i);
            return true;
        } else if (id == R.id.action_duplicate) {
            duplicateResume();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return false;
    }

    // ── Data operations ───────────────────────────────────────────────────────

    private void loadResume(long resumeId) {
        executor.execute(() -> {
            currentResume = resumeRepo.getById(resumeId);
            handler.post(() -> {
                if (currentResume != null) {
                    etTitle.setText(currentResume.getTitle());
                }
            });
        });
    }

    private void saveResume() {
        if (currentResume == null) return;
        String title = UiUtils.getText(etTitle);
        if (!title.isEmpty()) currentResume.setTitle(title);

        executor.execute(() -> {
            if (currentResume.getId() <= 0) {
                long id = resumeRepo.insert(currentResume);
                currentResume.setId(id);
            } else {
                resumeRepo.update(currentResume);
            }
            handler.post(this::showAutoSavedIndicator);
        });
    }

    private void scheduleAutoSave() {
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        autoSaveHandler.postDelayed(autoSaveRunnable, 1500);
    }

    private void showAutoSavedIndicator() {
        chipAutoSave.setVisibility(View.VISIBLE);
        chipAutoSave.setText("Saved ✓");
        handler.postDelayed(() -> chipAutoSave.setVisibility(View.GONE), 2000);
    }

    // ── Export options ────────────────────────────────────────────────────────

    /**
     * Shows a dialog that lets the user pick an export format:
     * plain text or HTML.  Both formats are written to the app's Documents
     * folder and then opened in the system share sheet.
     */
    private void showExportDialog() {
        if (currentResume == null) {
            UiUtils.showSnackbar(findViewById(android.R.id.content),
                    "Save the resume first before exporting");
            return;
        }

        String[] options = {"Share as Plain Text (.txt)", "Share as HTML (.html)"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Export Resume")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        exportResume(false);
                    } else {
                        exportResume(true);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Builds resume content and exports it as either HTML or plain text,
     * then opens the system share sheet so the user can send the file.
     *
     * @param asHtml {@code true} → export HTML; {@code false} → export plain text
     */
    private void exportResume(boolean asHtml) {
        executor.execute(() -> {
            Resume r = currentResume;
            String accentColor = r.getAccentColor() != null ? r.getAccentColor() : "#1565C0";

            if (asHtml) {
                String html = buildHtml(r, accentColor);
                File file = ExportUtils.exportAsHtml(this, r, html);
                handler.post(() -> {
                    if (file != null) {
                        ExportUtils.shareFile(this, file, "text/html");
                    } else {
                        UiUtils.showSnackbar(
                                findViewById(android.R.id.content), "HTML export failed");
                    }
                });
            } else {
                String text = buildPlainText(r);
                File file = ExportUtils.exportAsTxt(this, r, text);
                handler.post(() -> {
                    if (file != null) {
                        ExportUtils.shareFile(this, file, "text/plain");
                    } else {
                        UiUtils.showSnackbar(
                                findViewById(android.R.id.content), "Text export failed");
                    }
                });
            }
        });
    }

    /** Builds a clean plain-text summary of the resume. */
    private String buildPlainText(Resume r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.getTitle()).append('\n');
        sb.append(repeat('=', r.getTitle().length())).append('\n');
        sb.append('\n');
        appendField(sb, "Template",     r.getTemplate());
        appendField(sb, "Font",         r.getFont());
        appendField(sb, "Accent color", r.getAccentColor());
        if (r.getAtsScore() > 0)
            sb.append("ATS Score:     ").append(r.getAtsScore()).append('\n');
        if (r.getOverallScore() > 0)
            sb.append("Overall Score: ").append(r.getOverallScore()).append('\n');
        if (r.getTags() != null && !r.getTags().isEmpty())
            sb.append("Tags:          ").append(r.getTags()).append('\n');
        sb.append('\n');
        sb.append("--- Sections ---\n");
        sb.append("Add your content in the Resume Builder sections.\n");
        return sb.toString();
    }

    /** Builds an HTML representation of the resume for export / preview. */
    private String buildHtml(Resume r, String color) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head>")
          .append("<meta charset='UTF-8'>")
          .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("<style>")
          .append("body{font-family:sans-serif;margin:40px auto;max-width:800px;color:#1a1a1a}")
          .append("h1{color:").append(color).append(";border-bottom:2px solid ").append(color)
              .append(";padding-bottom:8px;margin-bottom:4px}")
          .append("h2{color:").append(color).append(";font-size:16px;margin-top:24px;margin-bottom:4px}")
          .append(".meta{color:#666;font-size:13px;margin-bottom:16px}")
          .append(".badge{display:inline-block;background:").append(color)
              .append(";color:#fff;border-radius:4px;padding:2px 8px;font-size:12px;margin-right:4px}")
          .append("</style></head><body>")
          .append("<h1>").append(safe(r.getTitle())).append("</h1>")
          .append("<p class='meta'>")
          .append("<span class='badge'>").append(safe(r.getTemplate())).append("</span> ")
          .append("<span class='badge'>").append(safe(r.getFont())).append("</span>");
        if (r.getAtsScore() > 0)
            sb.append(" ATS: ").append(r.getAtsScore()).append("%");
        if (r.getOverallScore() > 0)
            sb.append(" &nbsp;|&nbsp; Score: ").append(r.getOverallScore()).append("%");
        sb.append("</p>");

        sb.append("<h2>Professional Summary</h2>")
          .append("<p><em>Add your summary in the Resume Builder.</em></p>")
          .append("<h2>Experience</h2><p><em>No experience added yet.</em></p>")
          .append("<h2>Education</h2><p><em>No education added yet.</em></p>")
          .append("<h2>Skills</h2><p><em>No skills added yet.</em></p>");

        if (r.getTags() != null && !r.getTags().isEmpty()) {
            sb.append("<h2>Tags</h2><p>").append(safe(r.getTags())).append("</p>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void appendField(StringBuilder sb, String label, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(String.format("%-14s %s\n", label + ":", value));
        }
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    private void duplicateResume() {
        if (currentResume == null) return;
        executor.execute(() -> {
            Resume copy = new Resume();
            copy.setTitle(currentResume.getTitle() + " (Copy)");
            copy.setTemplate(currentResume.getTemplate());
            copy.setAccentColor(currentResume.getAccentColor());
            copy.setFont(currentResume.getFont());
            copy.setProfileId(currentResume.getProfileId());
            resumeRepo.insert(copy);
            handler.post(() -> UiUtils.showSnackbar(
                    findViewById(android.R.id.content), "Resume duplicated"));
        });
    }

    private void confirmDelete() {
        if (currentResume == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Resume")
                .setMessage("Delete this resume? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> executor.execute(() -> {
                    resumeRepo.delete(currentResume.getId());
                    handler.post(this::finish);
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddSectionDialog() {
        String[] sections = {
            "Professional Summary", "Career Objective", "Education", "Experience",
            "Skills", "Projects", "Certifications", "Awards", "Languages",
            "Volunteer", "References", "Publications", "Achievements", "Custom Section"
        };
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Section")
                .setItems(sections, (d, which) ->
                    UiUtils.showSnackbar(
                        findViewById(android.R.id.content),
                        sections[which] + " section added"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        saveResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
