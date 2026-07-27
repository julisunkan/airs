package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.adapters.DashboardAdapter;
import com.airesumebuilder.adapters.ResumeAdapter;
import com.airesumebuilder.models.DashboardItem;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.repositories.ResumeRepository;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main dashboard activity. Shows a grid of quick-access cards and recent resumes.
 */
public class MainActivity extends AppCompatActivity {

    private ResumeRepository  resumeRepo;
    private ResumeAdapter     recentAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resumeRepo = new ResumeRepository(this);

        setupToolbar();
        setupDashboardGrid();
        setupRecentResumes();
        setupFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentResumes();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return false;
    }

    private void setupDashboardGrid() {
        RecyclerView rv = findViewById(R.id.rvDashboard);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        List<DashboardItem> items = buildDashboardItems();
        DashboardAdapter adapter = new DashboardAdapter(items, action -> {
            handleDashboardAction(action);
        });
        rv.setAdapter(adapter);
    }

    private List<DashboardItem> buildDashboardItems() {
        List<DashboardItem> items = new ArrayList<>();
        items.add(new DashboardItem("📝", getString(R.string.create_resume),
                DashboardAdapter.ACTION_CREATE_RESUME));
        items.add(new DashboardItem("📄", getString(R.string.my_resumes),
                DashboardAdapter.ACTION_MY_RESUMES));
        items.add(new DashboardItem("🎨", getString(R.string.templates),
                DashboardAdapter.ACTION_TEMPLATES));
        items.add(new DashboardItem("🤖", getString(R.string.ai_review),
                DashboardAdapter.ACTION_AI_REVIEW));
        items.add(new DashboardItem("✉️", getString(R.string.cover_letter),
                DashboardAdapter.ACTION_COVER_LETTER));
        items.add(new DashboardItem("🎯", getString(R.string.interview_prep),
                DashboardAdapter.ACTION_INTERVIEW_PREP));
        items.add(new DashboardItem("💼", getString(R.string.career_coach),
                DashboardAdapter.ACTION_CAREER_COACH));
        items.add(new DashboardItem("🌐", getString(R.string.portfolio),
                DashboardAdapter.ACTION_PORTFOLIO));
        items.add(new DashboardItem("📊", getString(R.string.job_tracker),
                DashboardAdapter.ACTION_JOB_TRACKER));
        items.add(new DashboardItem("📈", getString(R.string.analytics),
                DashboardAdapter.ACTION_ANALYTICS));
        items.add(new DashboardItem("⭐", getString(R.string.favorites),
                DashboardAdapter.ACTION_FAVORITES));
        items.add(new DashboardItem("⚙️", getString(R.string.settings),
                DashboardAdapter.ACTION_SETTINGS));
        return items;
    }

    private void handleDashboardAction(int action) {
        switch (action) {
            case DashboardAdapter.ACTION_CREATE_RESUME:
                createNewResume(); break;
            case DashboardAdapter.ACTION_MY_RESUMES:
                startActivity(new Intent(this, ResumeListActivity.class)); break;
            case DashboardAdapter.ACTION_TEMPLATES:
                startActivity(new Intent(this, TemplatesActivity.class)); break;
            case DashboardAdapter.ACTION_AI_REVIEW:
                startActivity(new Intent(this, AiReviewActivity.class)); break;
            case DashboardAdapter.ACTION_COVER_LETTER:
                startActivity(new Intent(this, CoverLetterActivity.class)); break;
            case DashboardAdapter.ACTION_INTERVIEW_PREP:
                startActivity(new Intent(this, InterviewPrepActivity.class)); break;
            case DashboardAdapter.ACTION_CAREER_COACH:
                startActivity(new Intent(this, CareerCoachActivity.class)); break;
            case DashboardAdapter.ACTION_PORTFOLIO:
                startActivity(new Intent(this, PortfolioActivity.class)); break;
            case DashboardAdapter.ACTION_JOB_TRACKER:
                startActivity(new Intent(this, JobTrackerActivity.class)); break;
            case DashboardAdapter.ACTION_ANALYTICS:
                startActivity(new Intent(this, AnalyticsActivity.class)); break;
            case DashboardAdapter.ACTION_FAVORITES:
                startActivity(new Intent(this, FavoritesActivity.class)); break;
            case DashboardAdapter.ACTION_SETTINGS:
                startActivity(new Intent(this, SettingsActivity.class)); break;
        }
    }

    private void setupRecentResumes() {
        RecyclerView rv = findViewById(R.id.rvRecentResumes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        recentAdapter = new ResumeAdapter(new ResumeAdapter.OnResumeActionListener() {
            @Override
            public void onResumeClick(Resume resume) {
                openResume(resume.getId());
            }
            @Override
            public void onFavoriteToggle(Resume resume, boolean isFavorite) {
                toggleFavorite(resume, isFavorite);
            }
            @Override
            public void onMoreClick(Resume resume, View anchor) {
                showResumeMenu(resume, anchor);
            }
        });
        rv.setAdapter(recentAdapter);

        findViewById(R.id.btnSeeAllResumes).setOnClickListener(v ->
                startActivity(new Intent(this, ResumeListActivity.class)));
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> createNewResume());
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadRecentResumes() {
        executor.execute(() -> {
            List<Resume> recent = resumeRepo.getRecent(5);
            handler.post(() -> {
                recentAdapter.setItems(recent);
                View llEmpty = findViewById(R.id.llEmptyResumes);
                llEmpty.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void createNewResume() {
        Intent intent = new Intent(this, ResumeBuilderActivity.class);
        intent.putExtra(ResumeBuilderActivity.EXTRA_NEW, true);
        startActivity(intent);
    }

    private void openResume(long resumeId) {
        Intent intent = new Intent(this, ResumeBuilderActivity.class);
        intent.putExtra(ResumeBuilderActivity.EXTRA_RESUME_ID, resumeId);
        startActivity(intent);
    }

    private void toggleFavorite(Resume resume, boolean isFavorite) {
        executor.execute(() -> {
            resumeRepo.setFavorite(resume.getId(), isFavorite);
            resume.setFavorite(isFavorite);
            handler.post(this::loadRecentResumes);
        });
    }

    private void showResumeMenu(Resume resume, View anchor) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
        popup.getMenu().add("Open");
        popup.getMenu().add("Preview");
        popup.getMenu().add("Duplicate");
        popup.getMenu().add("Delete");
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Open":      openResume(resume.getId()); break;
                case "Preview":   previewResume(resume.getId()); break;
                case "Duplicate": duplicateResume(resume); break;
                case "Delete":    confirmDelete(resume); break;
            }
            return true;
        });
        popup.show();
    }

    private void previewResume(long resumeId) {
        Intent intent = new Intent(this, ResumePreviewActivity.class);
        intent.putExtra(ResumePreviewActivity.EXTRA_RESUME_ID, resumeId);
        startActivity(intent);
    }

    private void duplicateResume(Resume original) {
        executor.execute(() -> {
            Resume copy = new Resume();
            copy.setTitle(original.getTitle() + " (Copy)");
            copy.setTemplate(original.getTemplate());
            copy.setAccentColor(original.getAccentColor());
            copy.setFont(original.getFont());
            copy.setProfileId(original.getProfileId());
            resumeRepo.insert(copy);
            handler.post(() -> {
                UiUtils.showSnackbar(findViewById(android.R.id.content), "Resume duplicated");
                loadRecentResumes();
            });
        });
    }

    private void confirmDelete(Resume resume) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Resume")
                .setMessage("Delete \"" + resume.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    executor.execute(() -> {
                        resumeRepo.delete(resume.getId());
                        handler.post(() -> {
                            UiUtils.showSnackbar(
                                    findViewById(android.R.id.content), "Resume deleted");
                            loadRecentResumes();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
