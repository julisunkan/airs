package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.adapters.ResumeAdapter;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.repositories.ResumeRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Global search screen for resumes and jobs.
 */
public class SearchActivity extends AppCompatActivity {

    private ResumeRepository resumeRepo;
    private ResumeAdapter    adapter;
    private TextInputEditText etSearch;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());
    private final Handler         debounce = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        resumeRepo = new ResumeRepository(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etSearch);

        RecyclerView rv = findViewById(R.id.rvResults);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResumeAdapter(new ResumeAdapter.OnResumeActionListener() {
            @Override public void onResumeClick(Resume r) {
                Intent i = new Intent(SearchActivity.this, ResumeBuilderActivity.class);
                i.putExtra(ResumeBuilderActivity.EXTRA_RESUME_ID, r.getId());
                startActivity(i);
            }
            @Override public void onFavoriteToggle(Resume r, boolean fav) {}
            @Override public void onMoreClick(Resume r, View anchor) {}
        });
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                debounce.removeCallbacksAndMessages(null);
                debounce.postDelayed(() -> search(s.toString().trim()), 300);
            }
        });
    }

    private void search(String query) {
        if (query.isEmpty()) { adapter.setItems(null); return; }
        executor.execute(() -> {
            List<Resume> results = resumeRepo.search(query);
            handler.post(() -> adapter.setItems(results));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
