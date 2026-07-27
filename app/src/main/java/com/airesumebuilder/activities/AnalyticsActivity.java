package com.airesumebuilder.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.repositories.JobTrackerRepository;
import com.airesumebuilder.repositories.ResumeRepository;
import com.airesumebuilder.utils.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Analytics dashboard showing usage statistics.
 */
public class AnalyticsActivity extends AppCompatActivity {

    private ResumeRepository     resumeRepo;
    private JobTrackerRepository jobRepo;
    private PreferenceManager    prefs;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        resumeRepo = new ResumeRepository(this);
        jobRepo    = new JobTrackerRepository(this);
        prefs      = new PreferenceManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        loadStats();
    }

    private void loadStats() {
        executor.execute(() -> {
            int resumeCount = resumeRepo.count();
            int jobCount    = jobRepo.count();
            int aiUsage     = prefs.getAiUsageCount();
            int exports     = prefs.getExportCount();

            handler.post(() -> {
                setCount(R.id.tvTotalResumes, resumeCount);
                setCount(R.id.tvAiUsage,      aiUsage);
                setCount(R.id.tvExports,       exports);
                setCount(R.id.tvJobsTracked,   jobCount);
            });
        });
    }

    private void setCount(int viewId, int count) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(String.valueOf(count));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
