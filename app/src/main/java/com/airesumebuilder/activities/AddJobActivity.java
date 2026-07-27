package com.airesumebuilder.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.models.JobApplication;
import com.airesumebuilder.repositories.JobTrackerRepository;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Add or edit a job application.
 */
public class AddJobActivity extends AppCompatActivity {

    public static final String EXTRA_JOB_ID = "job_id";

    private JobTrackerRepository jobRepo;
    private JobApplication       currentJob;
    private boolean              isEdit;

    private TextInputEditText etPosition, etCompany, etAppDate, etInterviewDate, etNotes;
    private AutoCompleteTextView actvStatus;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job);

        jobRepo = new JobTrackerRepository(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etPosition      = findViewById(R.id.etPosition);
        etCompany       = findViewById(R.id.etCompany);
        etAppDate       = findViewById(R.id.etAppDate);
        etInterviewDate = findViewById(R.id.etInterviewDate);
        etNotes         = findViewById(R.id.etNotes);
        actvStatus      = findViewById(R.id.actvStatus);

        setupStatusDropdown();
        setupDatePickers();

        long jobId = getIntent().getLongExtra(EXTRA_JOB_ID, -1L);
        if (jobId > 0) {
            isEdit = true;
            toolbar.setTitle("Edit Job");
            loadJob(jobId);
        } else {
            isEdit = false;
            currentJob = new JobApplication();
            etAppDate.setText(com.airesumebuilder.utils.DateUtils.today());
        }

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveJob());
    }

    private void setupStatusDropdown() {
        String[] statuses = {
            JobApplication.STATUS_APPLIED,
            JobApplication.STATUS_INTERVIEW,
            JobApplication.STATUS_OFFER,
            JobApplication.STATUS_REJECTED,
            JobApplication.STATUS_WITHDRAWN
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatus.setAdapter(adapter);
        actvStatus.setText(JobApplication.STATUS_APPLIED, false);
    }

    private void setupDatePickers() {
        etAppDate.setOnClickListener(v -> showDatePicker(etAppDate));
        etInterviewDate.setOnClickListener(v -> showDatePicker(etInterviewDate));

        // Also trigger via TextInputLayout end icon
        findViewById(R.id.tilAppDate).setOnClickListener(v -> showDatePicker(etAppDate));
        findViewById(R.id.tilInterviewDate).setOnClickListener(v -> showDatePicker(etInterviewDate));
    }

    private void showDatePicker(TextInputEditText target) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            target.setText(String.format("%02d/%02d/%d", day, month + 1, year));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadJob(long jobId) {
        executor.execute(() -> {
            currentJob = jobRepo.getById(jobId);
            handler.post(() -> {
                if (currentJob != null) populateFields();
            });
        });
    }

    private void populateFields() {
        etPosition.setText(currentJob.getPosition());
        etCompany.setText(currentJob.getCompany());
        actvStatus.setText(currentJob.getStatus(), false);
        etAppDate.setText(currentJob.getApplicationDate());
        etInterviewDate.setText(currentJob.getInterviewDate());
        etNotes.setText(currentJob.getNotes());
    }

    private void saveJob() {
        String position = UiUtils.getText(etPosition);
        String company  = UiUtils.getText(etCompany);

        if (position.isEmpty()) {
            etPosition.setError(getString(R.string.error_empty_field));
            return;
        }
        if (company.isEmpty()) {
            etCompany.setError(getString(R.string.error_empty_field));
            return;
        }

        if (currentJob == null) currentJob = new JobApplication();
        currentJob.setPosition(position);
        currentJob.setCompany(company);
        currentJob.setStatus(UiUtils.getText(actvStatus));
        currentJob.setApplicationDate(UiUtils.getText(etAppDate));
        currentJob.setInterviewDate(UiUtils.getText(etInterviewDate));
        currentJob.setNotes(UiUtils.getText(etNotes));

        executor.execute(() -> {
            if (isEdit) {
                jobRepo.update(currentJob);
            } else {
                jobRepo.insert(currentJob);
            }
            handler.post(() -> {
                UiUtils.showSnackbar(
                        findViewById(android.R.id.content),
                        isEdit ? "Job updated" : "Job added");
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
