package com.airesumebuilder.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.adapters.SectionsAdapter;
import com.airesumebuilder.database.DatabaseHelper;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.repositories.ResumeRepository;
import com.airesumebuilder.utils.ExportUtils;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Full resume-builder screen.
 * Handles creating/editing a resume with multiple sections, auto-save, and AI prompts.
 */
public class ResumeBuilderActivity extends AppCompatActivity {

    private static final String TAG = "ResumeBuilderActivity";

    public static final String EXTRA_RESUME_ID = "resume_id";
    public static final String EXTRA_NEW       = "new_resume";

    private ResumeRepository  resumeRepo;
    private Resume            currentResume;
    private TextInputEditText etTitle;
    private Chip              chipAutoSave;
    private SectionsAdapter   sectionsAdapter;

    private final ExecutorService executor       = Executors.newSingleThreadExecutor();
    private final Handler         handler        = new Handler(Looper.getMainLooper());
    private final Handler         autoSaveHandler = new Handler(Looper.getMainLooper());
    private final Runnable        autoSaveRunnable = this::saveResume;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_builder);

        resumeRepo   = new ResumeRepository(this);
        etTitle      = findViewById(R.id.etResumeTitle);
        chipAutoSave = findViewById(R.id.chipAutoSave);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        boolean isNew   = getIntent().getBooleanExtra(EXTRA_NEW, false);
        long    resumeId = getIntent().getLongExtra(EXTRA_RESUME_ID, -1L);

        if (isNew || resumeId < 0) {
            currentResume = new Resume();
            currentResume.setTitle("Untitled Resume");
        } else {
            loadResume(resumeId);
        }

        setupTitleField();
        setupSectionsRecyclerView();
        setupBottomButtons();
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

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void setupTitleField() {
        if (currentResume != null) {
            etTitle.setText(currentResume.getTitle());
        }
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (currentResume != null) currentResume.setTitle(s.toString().trim());
                scheduleAutoSave();
            }
        });
    }

    private void setupSectionsRecyclerView() {
        sectionsAdapter = new SectionsAdapter(this::onDeleteSection, this::onEditSection);
        RecyclerView rv = findViewById(R.id.rvSections);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(sectionsAdapter);
    }

    private void setupBottomButtons() {
        findViewById(R.id.btnAddSection).setOnClickListener(v -> showAddSectionDialog());
        findViewById(R.id.btnPreview).setOnClickListener(v -> {
            if (currentResume != null && currentResume.getId() > 0) {
                openPreview();
            } else {
                saveResume();
                UiUtils.showSnackbar(findViewById(android.R.id.content),
                        "Resume saved — tap Preview again to open it");
            }
        });
    }

    private void openPreview() {
        Intent i = new Intent(this, ResumePreviewActivity.class);
        i.putExtra(ResumePreviewActivity.EXTRA_RESUME_ID, currentResume.getId());
        startActivity(i);
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_preview) {
            if (currentResume != null && currentResume.getId() > 0) openPreview();
            return true;
        } else if (id == R.id.action_export) {
            showExportDialog();
            return true;
        } else if (id == R.id.action_ai_review) {
            Intent i = new Intent(this, AiReviewActivity.class);
            if (currentResume != null) i.putExtra(AiReviewActivity.EXTRA_RESUME_ID, currentResume.getId());
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

    // ── Resume persistence ────────────────────────────────────────────────────

    private void loadResume(long resumeId) {
        executor.execute(() -> {
            currentResume = resumeRepo.getById(resumeId);
            handler.post(() -> {
                if (currentResume != null) {
                    etTitle.setText(currentResume.getTitle());
                    loadSections();
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
        if (chipAutoSave == null) return;
        chipAutoSave.setVisibility(View.VISIBLE);
        chipAutoSave.setText("Saved ✓");
        handler.postDelayed(() -> chipAutoSave.setVisibility(View.GONE), 2000);
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
        new AlertDialog.Builder(this)
                .setTitle("Delete Resume")
                .setMessage("Delete this resume? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> executor.execute(() -> {
                    resumeRepo.delete(currentResume.getId());
                    handler.post(this::finish);
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Section list ──────────────────────────────────────────────────────────

    /** Reloads all section entries for the current resume and refreshes the list. */
    private void loadSections() {
        if (currentResume == null || currentResume.getId() <= 0) return;
        long resumeId = currentResume.getId();
        executor.execute(() -> {
            List<SectionsAdapter.SectionItem> items = buildSectionList(resumeId);
            handler.post(() -> sectionsAdapter.setItems(items));
        });
    }

    /** Queries every section table and returns a combined, ordered list. */
    private List<SectionsAdapter.SectionItem> buildSectionList(long resumeId) {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).getReadableDatabase();
        List<SectionsAdapter.SectionItem> all = new ArrayList<>();

        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_EDUCATION,
                "Education", "degree", "institution"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_EXPERIENCE,
                "Experience", "position", "company"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_SKILLS,
                "Skills", "name", "level"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_PROJECTS,
                "Projects", "name", "technologies"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_CERTIFICATIONS,
                "Certifications", "name", "issuer"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_AWARDS,
                "Awards", "title", "issuer"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_LANGUAGES,
                "Languages", "name", "proficiency"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_VOLUNTEER,
                "Volunteer", "role", "organization"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_REFERENCES,
                "References", "name", "company"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_PUBLICATIONS,
                "Publications", "title", "publisher"));
        all.addAll(querySection(db, resumeId, DatabaseHelper.TABLE_CUSTOM_SECTIONS,
                "Custom", "section_title", "content"));

        return all;
    }

    private List<SectionsAdapter.SectionItem> querySection(
            SQLiteDatabase db, long resumeId, String table, String type,
            String titleCol, String subtitleCol) {
        List<SectionsAdapter.SectionItem> list = new ArrayList<>();
        try {
            Cursor c = db.query(table, null, "resume_id = ?",
                    new String[]{String.valueOf(resumeId)},
                    null, null, "sort_order ASC, id ASC");
            try {
                while (c.moveToNext()) {
                    long   id    = c.getLong(c.getColumnIndexOrThrow("id"));
                    String title = safeGet(c, titleCol);
                    String sub   = safeGet(c, subtitleCol);
                    if (title.isEmpty()) title = type;
                    if (sub.length() > 60) sub = sub.substring(0, 57) + "…";
                    list.add(new SectionsAdapter.SectionItem(type, title, sub, id, table));
                }
            } finally {
                c.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "querySection failed: " + table, e);
        }
        return list;
    }

    private String safeGet(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        if (idx < 0 || c.isNull(idx)) return "";
        return c.getString(idx);
    }

    /** Deletes a section entry from the DB and refreshes the list. */
    private void onDeleteSection(SectionsAdapter.SectionItem item) {
        executor.execute(() -> {
            DatabaseHelper.getInstance(this).getWritableDatabase()
                    .delete(item.table, "id = ?", new String[]{String.valueOf(item.id)});
            long resumeId = (currentResume != null) ? currentResume.getId() : -1;
            List<SectionsAdapter.SectionItem> updated =
                    (resumeId > 0) ? buildSectionList(resumeId) : new ArrayList<>();
            handler.post(() -> {
                sectionsAdapter.setItems(updated);
                UiUtils.showSnackbar(findViewById(android.R.id.content),
                        item.type + " entry removed");
            });
        });
    }

    // ── Edit section ──────────────────────────────────────────────────────────

    /** Tapped the pencil on a section row — load its DB row and open edit dialog. */
    private void onEditSection(SectionsAdapter.SectionItem item) {
        executor.execute(() -> {
            SQLiteDatabase db = DatabaseHelper.getInstance(this).getReadableDatabase();
            Bundle data = new Bundle();
            try (Cursor c = db.query(item.table, null, "id = ?",
                    new String[]{String.valueOf(item.id)}, null, null, null)) {
                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        data.putString(c.getColumnName(i), c.isNull(i) ? "" : c.getString(i));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "onEditSection load failed: " + item.table, e);
            }
            handler.post(() -> routeToEditDialog(item, data));
        });
    }

    /** Dispatch to the right pre-populated edit dialog on the UI thread. */
    private void routeToEditDialog(SectionsAdapter.SectionItem item, Bundle data) {
        switch (item.table) {
            case DatabaseHelper.TABLE_EDUCATION:
                showEditEducationDialog(item.id, data);      break;
            case DatabaseHelper.TABLE_EXPERIENCE:
                showEditExperienceDialog(item.id, data);     break;
            case DatabaseHelper.TABLE_SKILLS:
                showEditSkillDialog(item.id, data);          break;
            case DatabaseHelper.TABLE_PROJECTS:
                showEditProjectDialog(item.id, data);        break;
            case DatabaseHelper.TABLE_CERTIFICATIONS:
                showEditCertificationDialog(item.id, data);  break;
            case DatabaseHelper.TABLE_AWARDS:
                showEditAwardDialog(item.id, data);          break;
            case DatabaseHelper.TABLE_LANGUAGES:
                showEditLanguageDialog(item.id, data);       break;
            case DatabaseHelper.TABLE_VOLUNTEER:
                showEditVolunteerDialog(item.id, data);      break;
            case DatabaseHelper.TABLE_REFERENCES:
                showEditReferenceDialog(item.id, data);      break;
            case DatabaseHelper.TABLE_PUBLICATIONS:
                showEditPublicationDialog(item.id, data);    break;
            case DatabaseHelper.TABLE_CUSTOM_SECTIONS:
                showEditCustomSectionDialog(item.id, data);  break;
            default:
                snack("Edit not supported for this section type");
        }
    }

    // ── Edit dialogs (pre-populated) ──────────────────────────────────────────

    private void showEditEducationDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etInstitution = makeFieldPre(layout, "Institution *",        d.getString("institution"));
        TextInputEditText etDegree      = makeFieldPre(layout, "Degree",               d.getString("degree"));
        TextInputEditText etField       = makeFieldPre(layout, "Field of Study",       d.getString("field"));
        TextInputEditText etStart       = makeFieldPre(layout, "Start Year",           d.getString("start_date"));
        TextInputEditText etEnd         = makeFieldPre(layout, "End Year",             d.getString("end_date"));
        TextInputEditText etGpa         = makeFieldPre(layout, "GPA",                  d.getString("gpa"));
        TextInputEditText etDesc        = makeMultilineFieldPre(layout, "Description", d.getString("description"));

        showFormDialog("Edit Education", layout, "Save", () -> {
            String institution = UiUtils.getText(etInstitution);
            if (institution.isEmpty()) { snack("Institution is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("institution", institution);
            cv.put("degree",      UiUtils.getText(etDegree));
            cv.put("field",       UiUtils.getText(etField));
            cv.put("start_date",  UiUtils.getText(etStart));
            cv.put("end_date",    UiUtils.getText(etEnd));
            cv.put("gpa",         UiUtils.getText(etGpa));
            cv.put("description", UiUtils.getText(etDesc));
            updateSection(DatabaseHelper.TABLE_EDUCATION, cv, rowId, "Education");
        });
    }

    private void showEditExperienceDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etCompany  = makeFieldPre(layout, "Company *",              d.getString("company"));
        TextInputEditText etPosition = makeFieldPre(layout, "Job Title *",            d.getString("position"));
        TextInputEditText etLocation = makeFieldPre(layout, "Location",               d.getString("location"));
        TextInputEditText etStart    = makeFieldPre(layout, "Start Date",             d.getString("start_date"));
        TextInputEditText etEnd      = makeFieldPre(layout, "End Date ('Present'…)",  d.getString("end_date"));
        TextInputEditText etDesc     = makeMultilineFieldPre(layout, "Responsibilities",  d.getString("description"));
        TextInputEditText etAchieve  = makeMultilineFieldPre(layout, "Key Achievements",  d.getString("achievements"));

        showFormDialog("Edit Experience", layout, "Save", () -> {
            String company  = UiUtils.getText(etCompany);
            String position = UiUtils.getText(etPosition);
            if (company.isEmpty() || position.isEmpty()) {
                snack("Company and Job Title are required"); return;
            }
            ContentValues cv = new ContentValues();
            cv.put("company",      company);
            cv.put("position",     position);
            cv.put("location",     UiUtils.getText(etLocation));
            cv.put("start_date",   UiUtils.getText(etStart));
            cv.put("end_date",     UiUtils.getText(etEnd));
            cv.put("description",  UiUtils.getText(etDesc));
            cv.put("achievements", UiUtils.getText(etAchieve));
            String end = UiUtils.getText(etEnd);
            cv.put("is_current",   end.equalsIgnoreCase("present") ? 1 : 0);
            updateSection(DatabaseHelper.TABLE_EXPERIENCE, cv, rowId, "Experience");
        });
    }

    private void showEditSkillDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName     = makeFieldPre(layout, "Skill Name *",  d.getString("name"));
        TextInputEditText etLevel    = makeFieldPre(layout, "Level",         d.getString("level"));
        TextInputEditText etCategory = makeFieldPre(layout, "Category",      d.getString("category"));

        showFormDialog("Edit Skill", layout, "Save", () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Skill name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",     name);
            cv.put("level",    UiUtils.getText(etLevel));
            cv.put("category", UiUtils.getText(etCategory));
            updateSection(DatabaseHelper.TABLE_SKILLS, cv, rowId, "Skill");
        });
    }

    private void showEditProjectDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName  = makeFieldPre(layout, "Project Name *",  d.getString("name"));
        TextInputEditText etDesc  = makeMultilineFieldPre(layout, "Description",  d.getString("description"));
        TextInputEditText etTech  = makeFieldPre(layout, "Technologies",    d.getString("technologies"));
        TextInputEditText etUrl   = makeFieldPre(layout, "Project URL",     d.getString("url"));
        TextInputEditText etStart = makeFieldPre(layout, "Start Date",      d.getString("start_date"));
        TextInputEditText etEnd   = makeFieldPre(layout, "End Date",        d.getString("end_date"));

        showFormDialog("Edit Project", layout, "Save", () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Project name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",         name);
            cv.put("description",  UiUtils.getText(etDesc));
            cv.put("technologies", UiUtils.getText(etTech));
            cv.put("url",          UiUtils.getText(etUrl));
            cv.put("start_date",   UiUtils.getText(etStart));
            cv.put("end_date",     UiUtils.getText(etEnd));
            updateSection(DatabaseHelper.TABLE_PROJECTS, cv, rowId, "Project");
        });
    }

    private void showEditCertificationDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName   = makeFieldPre(layout, "Certification Name *", d.getString("name"));
        TextInputEditText etIssuer = makeFieldPre(layout, "Issuing Organisation",  d.getString("issuer"));
        TextInputEditText etDate   = makeFieldPre(layout, "Issue Date",            d.getString("issue_date"));
        TextInputEditText etExpiry = makeFieldPre(layout, "Expiry Date",           d.getString("expiry_date"));
        TextInputEditText etCredId = makeFieldPre(layout, "Credential ID",         d.getString("credential_id"));
        TextInputEditText etUrl    = makeFieldPre(layout, "Credential URL",        d.getString("url"));

        showFormDialog("Edit Certification", layout, "Save", () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Certification name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",          name);
            cv.put("issuer",        UiUtils.getText(etIssuer));
            cv.put("issue_date",    UiUtils.getText(etDate));
            cv.put("expiry_date",   UiUtils.getText(etExpiry));
            cv.put("credential_id", UiUtils.getText(etCredId));
            cv.put("url",           UiUtils.getText(etUrl));
            updateSection(DatabaseHelper.TABLE_CERTIFICATIONS, cv, rowId, "Certification");
        });
    }

    private void showEditAwardDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etTitle  = makeFieldPre(layout, "Title *",        d.getString("title"));
        TextInputEditText etIssuer = makeFieldPre(layout, "Awarded By",     d.getString("issuer"));
        TextInputEditText etDate   = makeFieldPre(layout, "Date",           d.getString("date"));
        TextInputEditText etDesc   = makeMultilineFieldPre(layout, "Description", d.getString("description"));

        showFormDialog("Edit Award / Achievement", layout, "Save", () -> {
            String title = UiUtils.getText(etTitle);
            if (title.isEmpty()) { snack("Title is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("title",       title);
            cv.put("issuer",      UiUtils.getText(etIssuer));
            cv.put("date",        UiUtils.getText(etDate));
            cv.put("description", UiUtils.getText(etDesc));
            updateSection(DatabaseHelper.TABLE_AWARDS, cv, rowId, "Award");
        });
    }

    private void showEditLanguageDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName  = makeFieldPre(layout, "Language *",    d.getString("name"));
        TextInputEditText etLevel = makeFieldPre(layout, "Proficiency",   d.getString("proficiency"));

        showFormDialog("Edit Language", layout, "Save", () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Language name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",        name);
            cv.put("proficiency", UiUtils.getText(etLevel));
            updateSection(DatabaseHelper.TABLE_LANGUAGES, cv, rowId, "Language");
        });
    }

    private void showEditVolunteerDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etOrg   = makeFieldPre(layout, "Organisation *", d.getString("organization"));
        TextInputEditText etRole  = makeFieldPre(layout, "Role",           d.getString("role"));
        TextInputEditText etStart = makeFieldPre(layout, "Start Date",     d.getString("start_date"));
        TextInputEditText etEnd   = makeFieldPre(layout, "End Date",       d.getString("end_date"));
        TextInputEditText etDesc  = makeMultilineFieldPre(layout, "Description", d.getString("description"));

        showFormDialog("Edit Volunteer Work", layout, "Save", () -> {
            String org = UiUtils.getText(etOrg);
            if (org.isEmpty()) { snack("Organisation is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("organization", org);
            cv.put("role",         UiUtils.getText(etRole));
            cv.put("start_date",   UiUtils.getText(etStart));
            cv.put("end_date",     UiUtils.getText(etEnd));
            cv.put("description",  UiUtils.getText(etDesc));
            updateSection(DatabaseHelper.TABLE_VOLUNTEER, cv, rowId, "Volunteer Work");
        });
    }

    private void showEditReferenceDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName    = makeFieldPre(layout, "Reference Name *", d.getString("name"));
        TextInputEditText etTitle   = makeFieldPre(layout, "Job Title",        d.getString("title"));
        TextInputEditText etCompany = makeFieldPre(layout, "Company",          d.getString("company"));
        TextInputEditText etEmail   = makeFieldPre(layout, "Email",            d.getString("email"));
        TextInputEditText etPhone   = makeFieldPre(layout, "Phone",            d.getString("phone"));

        showFormDialog("Edit Reference", layout, "Save", () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Reference name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",    name);
            cv.put("title",   UiUtils.getText(etTitle));
            cv.put("company", UiUtils.getText(etCompany));
            cv.put("email",   UiUtils.getText(etEmail));
            cv.put("phone",   UiUtils.getText(etPhone));
            updateSection(DatabaseHelper.TABLE_REFERENCES, cv, rowId, "Reference");
        });
    }

    private void showEditPublicationDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etTitle     = makeFieldPre(layout, "Publication Title *", d.getString("title"));
        TextInputEditText etPublisher = makeFieldPre(layout, "Publisher / Journal", d.getString("publisher"));
        TextInputEditText etDate      = makeFieldPre(layout, "Publication Date",    d.getString("date"));
        TextInputEditText etUrl       = makeFieldPre(layout, "URL / DOI",           d.getString("url"));
        TextInputEditText etDesc      = makeMultilineFieldPre(layout, "Description / Abstract", d.getString("description"));

        showFormDialog("Edit Publication", layout, "Save", () -> {
            String title = UiUtils.getText(etTitle);
            if (title.isEmpty()) { snack("Publication title is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("title",       title);
            cv.put("publisher",   UiUtils.getText(etPublisher));
            cv.put("date",        UiUtils.getText(etDate));
            cv.put("url",         UiUtils.getText(etUrl));
            cv.put("description", UiUtils.getText(etDesc));
            updateSection(DatabaseHelper.TABLE_PUBLICATIONS, cv, rowId, "Publication");
        });
    }

    private void showEditCustomSectionDialog(long rowId, Bundle d) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etSectionTitle = makeFieldPre(layout, "Section Title *", d.getString("section_title"));
        TextInputEditText etContent      = makeMultilineFieldPre(layout, "Content", d.getString("content"));

        showFormDialog("Edit Section", layout, "Save", () -> {
            String sectionTitle = UiUtils.getText(etSectionTitle);
            if (sectionTitle.isEmpty()) { snack("Section title is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("section_title", sectionTitle);
            cv.put("content",       UiUtils.getText(etContent));
            updateSection(DatabaseHelper.TABLE_CUSTOM_SECTIONS, cv, rowId, sectionTitle);
        });
    }

    // ── Update helper ─────────────────────────────────────────────────────────

    /**
     * Updates a single row in {@code table} and refreshes the section list.
     * Always called from the UI thread; runs DB work on the background executor.
     */
    private void updateSection(String table, ContentValues cv, long rowId, String label) {
        executor.execute(() -> {
            try {
                DatabaseHelper.getInstance(this).getWritableDatabase()
                        .update(table, cv, "id = ?", new String[]{String.valueOf(rowId)});
                long resumeId = (currentResume != null) ? currentResume.getId() : -1;
                List<SectionsAdapter.SectionItem> updated =
                        (resumeId > 0) ? buildSectionList(resumeId) : new ArrayList<>();
                handler.post(() -> {
                    sectionsAdapter.setItems(updated);
                    showAutoSavedIndicator();
                    UiUtils.showSnackbar(
                            findViewById(android.R.id.content), label + " updated");
                });
            } catch (Exception e) {
                Log.e(TAG, "updateSection failed: " + table, e);
                handler.post(() -> UiUtils.showSnackbar(
                        findViewById(android.R.id.content),
                        "Failed to update " + label + ": " + e.getMessage()));
            }
        });
    }

    // ── Insert helper ─────────────────────────────────────────────────────────

    /**
     * Saves the current resume (if not yet persisted), inserts a row into
     * {@code table} with the given values, then refreshes the section list.
     * Always called from the UI thread; runs DB work on the background executor.
     */
    private void insertSection(String table, ContentValues cv, String label) {
        executor.execute(() -> {
            try {
                // Persist resume first so we have a valid resume_id
                if (currentResume.getId() <= 0) {
                    String title = currentResume.getTitle();
                    if (title == null || title.isEmpty()) currentResume.setTitle("Untitled Resume");
                    long newId = resumeRepo.insert(currentResume);
                    if (newId <= 0) {
                        handler.post(() -> UiUtils.showSnackbar(
                                findViewById(android.R.id.content), "Failed to save resume"));
                        return;
                    }
                    currentResume.setId(newId);
                }

                cv.put("resume_id", currentResume.getId());
                DatabaseHelper.getInstance(this).getWritableDatabase()
                        .insertOrThrow(table, null, cv);

                List<SectionsAdapter.SectionItem> updated =
                        buildSectionList(currentResume.getId());

                handler.post(() -> {
                    sectionsAdapter.setItems(updated);
                    showAutoSavedIndicator();
                    UiUtils.showSnackbar(
                            findViewById(android.R.id.content), label + " added");
                });
            } catch (Exception e) {
                Log.e(TAG, "insertSection failed: " + table, e);
                handler.post(() -> UiUtils.showSnackbar(
                        findViewById(android.R.id.content),
                        "Failed to save " + label + ": " + e.getMessage()));
            }
        });
    }

    // ── Add-section picker ────────────────────────────────────────────────────

    private void showAddSectionDialog() {
        String[] sections = {
            "Professional Summary", "Career Objective",
            "Education", "Experience", "Skills", "Projects",
            "Certifications", "Awards", "Languages", "Volunteer",
            "References", "Publications", "Achievements", "Custom Section"
        };
        new AlertDialog.Builder(this)
                .setTitle("Add Section")
                .setItems(sections, (d, which) -> routeToSectionDialog(sections[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void routeToSectionDialog(String sectionName) {
        switch (sectionName) {
            case "Professional Summary":
            case "Career Objective":
                showSummaryDialog(sectionName);          break;
            case "Education":
                showEducationDialog();                   break;
            case "Experience":
                showExperienceDialog();                  break;
            case "Skills":
                showSkillDialog();                       break;
            case "Projects":
                showProjectDialog();                     break;
            case "Certifications":
                showCertificationDialog();               break;
            case "Awards":
            case "Achievements":
                showAwardDialog(sectionName);            break;
            case "Languages":
                showLanguageDialog();                    break;
            case "Volunteer":
                showVolunteerDialog();                   break;
            case "References":
                showReferenceDialog();                   break;
            case "Publications":
                showPublicationDialog();                 break;
            default: // "Custom Section"
                showCustomSectionDialog();               break;
        }
    }

    // ── Section dialogs ───────────────────────────────────────────────────────

    private void showSummaryDialog(String sectionTitle) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etContent = makeMultilineField(layout, sectionTitle + " text *");

        showFormDialog("Add " + sectionTitle, layout, () -> {
            String content = UiUtils.getText(etContent);
            if (content.isEmpty()) { snack("Please enter text"); return; }
            ContentValues cv = new ContentValues();
            cv.put("section_title", sectionTitle);
            cv.put("content",       content);
            insertSection(DatabaseHelper.TABLE_CUSTOM_SECTIONS, cv, sectionTitle);
        });
    }

    private void showEducationDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etInstitution = makeField(layout, "Institution *");
        TextInputEditText etDegree      = makeField(layout, "Degree (e.g. B.Sc.)");
        TextInputEditText etField       = makeField(layout, "Field of Study");
        TextInputEditText etStart       = makeField(layout, "Start Year");
        TextInputEditText etEnd         = makeField(layout, "End Year (or Expected)");
        TextInputEditText etGpa         = makeField(layout, "GPA");
        TextInputEditText etDesc        = makeMultilineField(layout, "Description / Achievements");

        showFormDialog("Add Education", layout, () -> {
            String institution = UiUtils.getText(etInstitution);
            if (institution.isEmpty()) { snack("Institution is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("institution", institution);
            cv.put("degree",      UiUtils.getText(etDegree));
            cv.put("field",       UiUtils.getText(etField));
            cv.put("start_date",  UiUtils.getText(etStart));
            cv.put("end_date",    UiUtils.getText(etEnd));
            cv.put("gpa",         UiUtils.getText(etGpa));
            cv.put("description", UiUtils.getText(etDesc));
            insertSection(DatabaseHelper.TABLE_EDUCATION, cv, "Education");
        });
    }

    private void showExperienceDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etCompany   = makeField(layout, "Company *");
        TextInputEditText etPosition  = makeField(layout, "Job Title *");
        TextInputEditText etLocation  = makeField(layout, "Location");
        TextInputEditText etStart     = makeField(layout, "Start Date (e.g. Jan 2021)");
        TextInputEditText etEnd       = makeField(layout, "End Date (or 'Present')");
        TextInputEditText etDesc      = makeMultilineField(layout, "Responsibilities");
        TextInputEditText etAchieve   = makeMultilineField(layout, "Key Achievements");

        showFormDialog("Add Experience", layout, () -> {
            String company  = UiUtils.getText(etCompany);
            String position = UiUtils.getText(etPosition);
            if (company.isEmpty() || position.isEmpty()) {
                snack("Company and Job Title are required"); return;
            }
            ContentValues cv = new ContentValues();
            cv.put("company",      company);
            cv.put("position",     position);
            cv.put("location",     UiUtils.getText(etLocation));
            cv.put("start_date",   UiUtils.getText(etStart));
            cv.put("end_date",     UiUtils.getText(etEnd));
            cv.put("description",  UiUtils.getText(etDesc));
            cv.put("achievements", UiUtils.getText(etAchieve));
            String end = UiUtils.getText(etEnd);
            cv.put("is_current",   end.equalsIgnoreCase("present") ? 1 : 0);
            insertSection(DatabaseHelper.TABLE_EXPERIENCE, cv, "Experience");
        });
    }

    private void showSkillDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName     = makeField(layout, "Skill Name * (e.g. Java, Leadership)");
        TextInputEditText etLevel    = makeField(layout, "Level (Beginner / Intermediate / Advanced / Expert)");
        TextInputEditText etCategory = makeField(layout, "Category (e.g. Programming, Soft Skills)");

        showFormDialog("Add Skill", layout, () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Skill name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",     name);
            cv.put("level",    UiUtils.getText(etLevel));
            cv.put("category", UiUtils.getText(etCategory));
            insertSection(DatabaseHelper.TABLE_SKILLS, cv, "Skill");
        });
    }

    private void showProjectDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName   = makeField(layout, "Project Name *");
        TextInputEditText etDesc   = makeMultilineField(layout, "Description");
        TextInputEditText etTech   = makeField(layout, "Technologies Used");
        TextInputEditText etUrl    = makeField(layout, "Project URL");
        TextInputEditText etStart  = makeField(layout, "Start Date");
        TextInputEditText etEnd    = makeField(layout, "End Date");

        showFormDialog("Add Project", layout, () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Project name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",         name);
            cv.put("description",  UiUtils.getText(etDesc));
            cv.put("technologies", UiUtils.getText(etTech));
            cv.put("url",          UiUtils.getText(etUrl));
            cv.put("start_date",   UiUtils.getText(etStart));
            cv.put("end_date",     UiUtils.getText(etEnd));
            insertSection(DatabaseHelper.TABLE_PROJECTS, cv, "Project");
        });
    }

    private void showCertificationDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName    = makeField(layout, "Certification Name *");
        TextInputEditText etIssuer  = makeField(layout, "Issuing Organisation");
        TextInputEditText etDate    = makeField(layout, "Issue Date");
        TextInputEditText etExpiry  = makeField(layout, "Expiry Date (if any)");
        TextInputEditText etCredId  = makeField(layout, "Credential ID");
        TextInputEditText etUrl     = makeField(layout, "Credential URL");

        showFormDialog("Add Certification", layout, () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Certification name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",          name);
            cv.put("issuer",        UiUtils.getText(etIssuer));
            cv.put("issue_date",    UiUtils.getText(etDate));
            cv.put("expiry_date",   UiUtils.getText(etExpiry));
            cv.put("credential_id", UiUtils.getText(etCredId));
            cv.put("url",           UiUtils.getText(etUrl));
            insertSection(DatabaseHelper.TABLE_CERTIFICATIONS, cv, "Certification");
        });
    }

    private void showAwardDialog(String sectionName) {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etTitle  = makeField(layout, sectionName + " Title *");
        TextInputEditText etIssuer = makeField(layout, "Awarded By");
        TextInputEditText etDate   = makeField(layout, "Date");
        TextInputEditText etDesc   = makeMultilineField(layout, "Description");

        showFormDialog("Add " + sectionName, layout, () -> {
            String title = UiUtils.getText(etTitle);
            if (title.isEmpty()) { snack("Title is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("title",       title);
            cv.put("issuer",      UiUtils.getText(etIssuer));
            cv.put("date",        UiUtils.getText(etDate));
            cv.put("description", UiUtils.getText(etDesc));
            insertSection(DatabaseHelper.TABLE_AWARDS, cv, sectionName);
        });
    }

    private void showLanguageDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName  = makeField(layout, "Language *");
        TextInputEditText etLevel = makeField(layout, "Proficiency (Basic / Conversational / Fluent / Native)");

        showFormDialog("Add Language", layout, () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Language name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",        name);
            cv.put("proficiency", UiUtils.getText(etLevel));
            insertSection(DatabaseHelper.TABLE_LANGUAGES, cv, "Language");
        });
    }

    private void showVolunteerDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etOrg   = makeField(layout, "Organisation *");
        TextInputEditText etRole  = makeField(layout, "Role");
        TextInputEditText etStart = makeField(layout, "Start Date");
        TextInputEditText etEnd   = makeField(layout, "End Date");
        TextInputEditText etDesc  = makeMultilineField(layout, "Description");

        showFormDialog("Add Volunteer Work", layout, () -> {
            String org = UiUtils.getText(etOrg);
            if (org.isEmpty()) { snack("Organisation is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("organization", org);
            cv.put("role",         UiUtils.getText(etRole));
            cv.put("start_date",   UiUtils.getText(etStart));
            cv.put("end_date",     UiUtils.getText(etEnd));
            cv.put("description",  UiUtils.getText(etDesc));
            insertSection(DatabaseHelper.TABLE_VOLUNTEER, cv, "Volunteer Work");
        });
    }

    private void showReferenceDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etName    = makeField(layout, "Reference Name *");
        TextInputEditText etTitle   = makeField(layout, "Job Title");
        TextInputEditText etCompany = makeField(layout, "Company");
        TextInputEditText etEmail   = makeField(layout, "Email");
        TextInputEditText etPhone   = makeField(layout, "Phone");

        showFormDialog("Add Reference", layout, () -> {
            String name = UiUtils.getText(etName);
            if (name.isEmpty()) { snack("Reference name is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("name",    name);
            cv.put("title",   UiUtils.getText(etTitle));
            cv.put("company", UiUtils.getText(etCompany));
            cv.put("email",   UiUtils.getText(etEmail));
            cv.put("phone",   UiUtils.getText(etPhone));
            insertSection(DatabaseHelper.TABLE_REFERENCES, cv, "Reference");
        });
    }

    private void showPublicationDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etTitle     = makeField(layout, "Publication Title *");
        TextInputEditText etPublisher = makeField(layout, "Publisher / Journal");
        TextInputEditText etDate      = makeField(layout, "Publication Date");
        TextInputEditText etUrl       = makeField(layout, "URL / DOI");
        TextInputEditText etDesc      = makeMultilineField(layout, "Description / Abstract");

        showFormDialog("Add Publication", layout, () -> {
            String title = UiUtils.getText(etTitle);
            if (title.isEmpty()) { snack("Publication title is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("title",       title);
            cv.put("publisher",   UiUtils.getText(etPublisher));
            cv.put("date",        UiUtils.getText(etDate));
            cv.put("url",         UiUtils.getText(etUrl));
            cv.put("description", UiUtils.getText(etDesc));
            insertSection(DatabaseHelper.TABLE_PUBLICATIONS, cv, "Publication");
        });
    }

    private void showCustomSectionDialog() {
        LinearLayout layout = makeFormLayout();
        TextInputEditText etSectionTitle = makeField(layout, "Section Title *");
        TextInputEditText etContent      = makeMultilineField(layout, "Content");

        showFormDialog("Add Custom Section", layout, () -> {
            String sectionTitle = UiUtils.getText(etSectionTitle);
            if (sectionTitle.isEmpty()) { snack("Section title is required"); return; }
            ContentValues cv = new ContentValues();
            cv.put("section_title", sectionTitle);
            cv.put("content",       UiUtils.getText(etContent));
            insertSection(DatabaseHelper.TABLE_CUSTOM_SECTIONS, cv, sectionTitle);
        });
    }

    // ── Dialog / form helpers ─────────────────────────────────────────────────

    /**
     * Builds an AlertDialog with a scrollable form and a positive-button action.
     * The action is NOT called if the dialog is dismissed via Cancel or Back.
     * Validation inside the action can call {@link #snack} and return early.
     */
    private void showFormDialog(String title, LinearLayout formLayout, Runnable onConfirm) {
        showFormDialog(title, formLayout, "Add", onConfirm);
    }

    private void showFormDialog(String title, LinearLayout formLayout,
                                String positiveLabel, Runnable onConfirm) {
        ScrollView scroll = new ScrollView(this);
        scroll.addView(formLayout);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(scroll)
                .setPositiveButton(positiveLabel, (d, w) -> onConfirm.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private LinearLayout makeFormLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int px = dp(24);
        layout.setPadding(px, px / 2, px, px / 2);
        return layout;
    }

    private TextInputEditText makeField(LinearLayout parent, String hint) {
        TextInputLayout til = new TextInputLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, 0);
        til.setLayoutParams(lp);
        til.setHint(hint);

        TextInputEditText et = new TextInputEditText(this);
        et.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        til.addView(et);
        parent.addView(til);
        return et;
    }

    private TextInputEditText makeMultilineField(LinearLayout parent, String hint) {
        TextInputEditText et = makeField(parent, hint);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        et.setMinLines(3);
        et.setGravity(Gravity.TOP | Gravity.START);
        return et;
    }

    /** Same as {@link #makeField} but pre-populated with {@code value}. */
    private TextInputEditText makeFieldPre(LinearLayout parent, String hint, String value) {
        TextInputEditText et = makeField(parent, hint);
        if (value != null && !value.isEmpty()) et.setText(value);
        return et;
    }

    /** Same as {@link #makeMultilineField} but pre-populated with {@code value}. */
    private TextInputEditText makeMultilineFieldPre(LinearLayout parent, String hint, String value) {
        TextInputEditText et = makeMultilineField(parent, hint);
        if (value != null && !value.isEmpty()) et.setText(value);
        return et;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void snack(String msg) {
        UiUtils.showSnackbar(findViewById(android.R.id.content), msg);
    }

    // ── Export ────────────────────────────────────────────────────────────────

    private void showExportDialog() {
        if (currentResume == null) {
            snack("Save the resume first before exporting"); return;
        }
        String[] options = {"Share as Plain Text (.txt)", "Share as HTML (.html)"};
        new AlertDialog.Builder(this)
                .setTitle("Export Resume")
                .setItems(options, (d, which) -> exportResume(which == 1))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportResume(boolean asHtml) {
        executor.execute(() -> {
            Resume r = currentResume;
            String color = r.getAccentColor() != null ? r.getAccentColor() : "#1565C0";
            if (asHtml) {
                File file = ExportUtils.exportAsHtml(this, r, buildHtml(r, color));
                handler.post(() -> {
                    if (file != null) ExportUtils.shareFile(this, file, "text/html");
                    else snack("HTML export failed");
                });
            } else {
                File file = ExportUtils.exportAsTxt(this, r, buildPlainText(r));
                handler.post(() -> {
                    if (file != null) ExportUtils.shareFile(this, file, "text/plain");
                    else snack("Text export failed");
                });
            }
        });
    }

    private String buildPlainText(Resume r) {
        String title = r.getTitle() != null ? r.getTitle() : "Resume";
        StringBuilder sb = new StringBuilder();
        sb.append(title).append('\n');
        sb.append(repeat('=', title.length())).append("\n\n");
        appendField(sb, "Template",     r.getTemplate());
        appendField(sb, "Font",         r.getFont());
        appendField(sb, "Accent Color", r.getAccentColor());
        if (r.getAtsScore() > 0)     sb.append("ATS Score:     ").append(r.getAtsScore()).append('\n');
        if (r.getOverallScore() > 0) sb.append("Overall Score: ").append(r.getOverallScore()).append('\n');
        return sb.toString();
    }

    private String buildHtml(Resume r, String color) {
        return "<!DOCTYPE html><html><head>"
             + "<meta charset='UTF-8'>"
             + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
             + "<style>body{font-family:sans-serif;margin:40px auto;max-width:800px;color:#1a1a1a}"
             + "h1{color:" + color + ";border-bottom:2px solid " + color + ";padding-bottom:8px}"
             + "h2{color:" + color + ";font-size:16px;margin-top:24px}</style></head><body>"
             + "<h1>" + safe(r.getTitle()) + "</h1>"
             + "<p style='color:#666;font-size:13px'>Template: " + safe(r.getTemplate()) + "</p>"
             + "</body></html>";
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void appendField(StringBuilder sb, String label, String value) {
        if (value != null && !value.isEmpty())
            sb.append(String.format("%-14s %s\n", label + ":", value));
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }
}
