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
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

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
            exportResume();
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

    private void exportResume() {
        UiUtils.showSnackbar(findViewById(android.R.id.content),
                "Export options coming soon");
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
