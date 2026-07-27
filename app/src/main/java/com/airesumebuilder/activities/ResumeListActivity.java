package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.adapters.ResumeAdapter;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.repositories.ResumeRepository;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Displays all saved resumes with search, sort, favourite, and context-menu actions.
 */
public class ResumeListActivity extends AppCompatActivity {

    private ResumeRepository resumeRepo;
    private ResumeAdapter    adapter;
    private View             llEmpty;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_list);

        resumeRepo = new ResumeRepository(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        llEmpty = findViewById(R.id.llEmpty);
        View btnCreateFirst = llEmpty.findViewById(R.id.btnCreateFirst);
        if (btnCreateFirst != null) {
            btnCreateFirst.setOnClickListener(v -> createNewResume());
        }

        setupRecyclerView();
        setupFab();

        // Search is handled via SearchBar + SearchView in layout; wire search text here.
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadResumes(null);
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvResumes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ResumeAdapter(new ResumeAdapter.OnResumeActionListener() {
            @Override
            public void onResumeClick(Resume resume) {
                Intent i = new Intent(ResumeListActivity.this, ResumeBuilderActivity.class);
                i.putExtra(ResumeBuilderActivity.EXTRA_RESUME_ID, resume.getId());
                startActivity(i);
            }

            @Override
            public void onFavoriteToggle(Resume resume, boolean isFavorite) {
                executor.execute(() -> {
                    resumeRepo.setFavorite(resume.getId(), isFavorite);
                    resume.setFavorite(isFavorite);
                    handler.post(() -> adapter.notifyItemChanged(
                            adapter.getPosition(resume.getId())));
                });
            }

            @Override
            public void onMoreClick(Resume resume, View anchor) {
                showPopup(resume, anchor);
            }
        });

        rv.setAdapter(adapter);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> createNewResume());
    }

    private void loadResumes(String query) {
        executor.execute(() -> {
            List<Resume> list = (query == null || query.isEmpty())
                    ? resumeRepo.getAll()
                    : resumeRepo.search(query);
            handler.post(() -> {
                adapter.setItems(list);
                llEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void createNewResume() {
        Intent i = new Intent(this, ResumeBuilderActivity.class);
        i.putExtra(ResumeBuilderActivity.EXTRA_NEW, true);
        startActivity(i);
    }

    private void showPopup(Resume resume, View anchor) {
        android.widget.PopupMenu menu = new android.widget.PopupMenu(this, anchor);
        menu.getMenu().add("Open");
        menu.getMenu().add("Preview");
        menu.getMenu().add("Export PDF");
        menu.getMenu().add("Duplicate");
        menu.getMenu().add("Delete");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Open":
                    Intent i = new Intent(this, ResumeBuilderActivity.class);
                    i.putExtra(ResumeBuilderActivity.EXTRA_RESUME_ID, resume.getId());
                    startActivity(i);
                    break;
                case "Preview":
                    Intent p = new Intent(this, ResumePreviewActivity.class);
                    p.putExtra(ResumePreviewActivity.EXTRA_RESUME_ID, resume.getId());
                    startActivity(p);
                    break;
                case "Duplicate":
                    duplicate(resume);
                    break;
                case "Delete":
                    confirmDelete(resume);
                    break;
            }
            return true;
        });
        menu.show();
    }

    private void duplicate(Resume original) {
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
                loadResumes(null);
            });
        });
    }

    private void confirmDelete(Resume resume) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Resume")
                .setMessage("Delete \"" + resume.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> executor.execute(() -> {
                    resumeRepo.delete(resume.getId());
                    handler.post(() -> {
                        UiUtils.showSnackbar(
                                findViewById(android.R.id.content), "Resume deleted");
                        loadResumes(null);
                    });
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
