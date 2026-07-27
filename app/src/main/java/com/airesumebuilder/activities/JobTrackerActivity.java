package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.adapters.JobAdapter;
import com.airesumebuilder.models.JobApplication;
import com.airesumebuilder.repositories.JobTrackerRepository;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Job Tracker – lists all tracked job applications with filter chips.
 */
public class JobTrackerActivity extends AppCompatActivity {

    private JobTrackerRepository jobRepo;
    private JobAdapter           adapter;
    private View                 llEmpty;
    private String               activeFilter = null; // null = all

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_tracker);

        jobRepo = new JobTrackerRepository(this);
        llEmpty = findViewById(R.id.llEmpty);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        setupFilterChips();
        setupRecyclerView();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddJobActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJobs();
    }

    private void setupFilterChips() {
        ChipGroup group = findViewById(R.id.chipGroupFilter);
        String[] statuses = {"All", JobApplication.STATUS_APPLIED,
                JobApplication.STATUS_INTERVIEW, JobApplication.STATUS_OFFER,
                JobApplication.STATUS_REJECTED, JobApplication.STATUS_WITHDRAWN};

        for (String status : statuses) {
            Chip chip = new Chip(this);
            chip.setText(status);
            chip.setCheckable(true);
            chip.setChecked(status.equals("All"));
            chip.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) {
                    activeFilter = status.equals("All") ? null : status;
                    loadJobs();
                }
            });
            group.addView(chip);
        }
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvJobs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JobAdapter(new JobAdapter.OnJobActionListener() {
            @Override public void onJobClick(JobApplication job) { editJob(job); }
            @Override public void onEditClick(JobApplication job) { editJob(job); }
            @Override public void onDeleteClick(JobApplication job, int position) {
                confirmDelete(job, position);
            }
        });
        rv.setAdapter(adapter);
    }

    private void loadJobs() {
        executor.execute(() -> {
            List<JobApplication> list = activeFilter == null
                    ? jobRepo.getAll()
                    : jobRepo.getByStatus(activeFilter);
            handler.post(() -> {
                adapter.setItems(list);
                llEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void editJob(JobApplication job) {
        Intent i = new Intent(this, AddJobActivity.class);
        i.putExtra(AddJobActivity.EXTRA_JOB_ID, job.getId());
        startActivity(i);
    }

    private void confirmDelete(JobApplication job, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Job")
                .setMessage("Remove \"" + job.getPosition() + " at " + job.getCompany() + "\"?")
                .setPositiveButton("Delete", (d, w) -> executor.execute(() -> {
                    jobRepo.delete(job.getId());
                    handler.post(() -> {
                        adapter.removeItem(position);
                        UiUtils.showSnackbar(
                                findViewById(android.R.id.content), "Job removed");
                        if (adapter.getItemCount() == 0)
                            llEmpty.setVisibility(View.VISIBLE);
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
