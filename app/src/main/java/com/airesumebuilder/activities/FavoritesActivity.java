package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.adapters.ResumeAdapter;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.repositories.ResumeRepository;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shows all favourite resumes.
 */
public class FavoritesActivity extends AppCompatActivity {

    private ResumeRepository resumeRepo;
    private ResumeAdapter    adapter;
    private View             llEmpty;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        resumeRepo = new ResumeRepository(this);
        llEmpty = findViewById(R.id.llEmpty);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvFavorites);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResumeAdapter(new ResumeAdapter.OnResumeActionListener() {
            @Override public void onResumeClick(Resume r) {
                Intent i = new Intent(FavoritesActivity.this, ResumeBuilderActivity.class);
                i.putExtra(ResumeBuilderActivity.EXTRA_RESUME_ID, r.getId());
                startActivity(i);
            }
            @Override public void onFavoriteToggle(Resume r, boolean fav) {
                executor.execute(() -> {
                    resumeRepo.setFavorite(r.getId(), fav);
                    handler.post(() -> loadFavorites());
                });
            }
            @Override public void onMoreClick(Resume r, View anchor) {}
        });
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        executor.execute(() -> {
            List<Resume> list = resumeRepo.getFavorites();
            handler.post(() -> {
                adapter.setItems(list);
                if (llEmpty != null)
                    llEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
