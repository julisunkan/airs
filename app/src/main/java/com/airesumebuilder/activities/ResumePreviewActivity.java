package com.airesumebuilder.activities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.database.DatabaseHelper;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.repositories.ResumeRepository;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Resume preview screen rendered as HTML in a WebView.
 *
 * Renders all real saved sections (education, experience, skills, custom sections,
 * etc.) fetched from the local SQLite database.  The "Export PDF" button uses
 * Android's built-in PrintManager to render the WebView content as a PDF through
 * the system print dialog.
 */
public class ResumePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_RESUME_ID = "resume_id";

    private ResumeRepository  resumeRepo;
    private Resume            resume;
    private WebView           webView;
    private boolean           pageLoaded = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_preview);

        resumeRepo = new ResumeRepository(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.webView);
        if (webView != null) {
            webView.getSettings().setJavaScriptEnabled(false);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);

            webView.setWebViewClient(new android.webkit.WebViewClient() {
                @Override
                public void onPageFinished(android.webkit.WebView view, String url) {
                    pageLoaded = true;
                }
            });
        }

        long resumeId = getIntent().getLongExtra(EXTRA_RESUME_ID, -1L);
        if (resumeId > 0) loadResume(resumeId);

        MaterialButton btnExport = findViewById(R.id.btnExport);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> exportAsPdf());
        }
    }

    private void loadResume(long resumeId) {
        executor.execute(() -> {
            resume = resumeRepo.getById(resumeId);
            // Build HTML on background thread so we can safely query the DB
            final String html = (resume != null) ? buildHtml(resume) : null;
            handler.post(() -> {
                if (html != null && webView != null) {
                    webView.loadDataWithBaseURL(null,
                            html, "text/html", "UTF-8", null);
                }
            });
        });
    }

    // ── PDF export ────────────────────────────────────────────────────────────

    private void exportAsPdf() {
        if (webView == null) {
            UiUtils.showSnackbar(findViewById(android.R.id.content), "Nothing to export yet");
            return;
        }
        if (!pageLoaded) {
            UiUtils.showSnackbar(findViewById(android.R.id.content),
                    "Resume is still loading — please try again in a moment");
            return;
        }

        String jobName = (resume != null && resume.getTitle() != null)
                ? resume.getTitle() : "Resume";

        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        PrintAttributes attributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();
        printManager.print(jobName, printAdapter, attributes);
    }

    // ── HTML builder ──────────────────────────────────────────────────────────

    /**
     * Builds a full HTML document from the resume and all its saved sections.
     * Called on the background executor — safe to query the database directly.
     */
    private String buildHtml(Resume r) {
        String color   = r.getAccentColor() != null ? r.getAccentColor() : "#1565C0";
        long resumeId  = r.getId();
        SQLiteDatabase db = DatabaseHelper.getInstance(this).getReadableDatabase();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head>")
          .append("<meta charset='UTF-8'>")
          .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("<style>")
          .append("*{box-sizing:border-box}")
          .append("body{font-family:sans-serif;margin:24px auto;max-width:800px;color:#1a1a1a;font-size:14px}")
          .append("h1{color:").append(color).append(";border-bottom:3px solid ").append(color).append(";padding-bottom:8px;margin-bottom:4px;font-size:24px}")
          .append("h2{color:").append(color).append(";font-size:15px;margin-top:22px;margin-bottom:6px;border-bottom:1px solid #e0e0e0;padding-bottom:3px;text-transform:uppercase;letter-spacing:.5px}")
          .append(".entry{margin-bottom:12px;padding-bottom:8px;border-bottom:1px dashed #eee}")
          .append(".entry:last-child{border-bottom:none}")
          .append(".row{display:flex;justify-content:space-between;align-items:baseline}")
          .append(".etitle{font-weight:bold;font-size:14px}")
          .append(".esub{color:#444;font-size:13px}")
          .append(".edate{color:#888;font-size:12px;white-space:nowrap;margin-left:8px}")
          .append(".edesc{font-size:13px;margin-top:4px;color:#333;white-space:pre-wrap}")
          .append(".skills-list{display:flex;flex-wrap:wrap;gap:6px;margin-top:6px}")
          .append(".skill{background:#f0f4ff;border:1px solid ").append(color).append(";border-radius:12px;padding:3px 10px;font-size:12px}")
          .append(".contact{color:#555;font-size:13px;margin-bottom:4px}")
          .append(".empty{color:#999;font-style:italic;font-size:13px}")
          .append("</style></head><body>")
          .append("<h1>").append(h(r.getTitle())).append("</h1>");

        // ── Custom sections (summary, objective, and free-form custom sections) ──
        appendCustomSections(sb, db, resumeId);

        // ── Experience ────────────────────────────────────────────────────────
        appendExperience(sb, db, resumeId);

        // ── Education ─────────────────────────────────────────────────────────
        appendEducation(sb, db, resumeId);

        // ── Skills ────────────────────────────────────────────────────────────
        appendSkills(sb, db, resumeId);

        // ── Projects ──────────────────────────────────────────────────────────
        appendProjects(sb, db, resumeId);

        // ── Certifications ────────────────────────────────────────────────────
        appendCertifications(sb, db, resumeId);

        // ── Awards ────────────────────────────────────────────────────────────
        appendAwards(sb, db, resumeId);

        // ── Languages ─────────────────────────────────────────────────────────
        appendLanguages(sb, db, resumeId);

        // ── Volunteer ─────────────────────────────────────────────────────────
        appendVolunteer(sb, db, resumeId);

        // ── Publications ──────────────────────────────────────────────────────
        appendPublications(sb, db, resumeId);

        // ── References ────────────────────────────────────────────────────────
        appendReferences(sb, db, resumeId);

        sb.append("</body></html>");
        return sb.toString();
    }

    // ── Section renderers ─────────────────────────────────────────────────────

    private void appendCustomSections(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_CUSTOM_SECTIONS, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            while (c.moveToNext()) {
                String title   = col(c, "section_title");
                String content = col(c, "content");
                if (!title.isEmpty() || !content.isEmpty()) {
                    sb.append("<h2>").append(h(title.isEmpty() ? "Custom Section" : title)).append("</h2>");
                    if (!content.isEmpty()) {
                        sb.append("<p class='edesc'>").append(h(content)).append("</p>");
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void appendExperience(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_EXPERIENCE, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>Work Experience</h2>");
            while (c.moveToNext()) {
                String position = col(c, "position");
                String company  = col(c, "company");
                String location = col(c, "location");
                String start    = col(c, "start_date");
                String end      = col(c, "end_date");
                String desc     = col(c, "description");
                String achieve  = col(c, "achievements");

                sb.append("<div class='entry'>")
                  .append("<div class='row'>")
                  .append("<span class='etitle'>").append(h(position)).append("</span>");
                String dateRange = datePair(start, end);
                if (!dateRange.isEmpty())
                    sb.append("<span class='edate'>").append(h(dateRange)).append("</span>");
                sb.append("</div>");

                String sub = company;
                if (!location.isEmpty()) sub += (!sub.isEmpty() ? " · " : "") + location;
                if (!sub.isEmpty()) sb.append("<div class='esub'>").append(h(sub)).append("</div>");
                if (!desc.isEmpty())    sb.append("<div class='edesc'>").append(h(desc)).append("</div>");
                if (!achieve.isEmpty()) sb.append("<div class='edesc'><strong>Achievements:</strong> ").append(h(achieve)).append("</div>");
                sb.append("</div>");
            }
        } catch (Exception ignored) {}
    }

    private void appendEducation(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_EDUCATION, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>Education</h2>");
            while (c.moveToNext()) {
                String institution = col(c, "institution");
                String degree      = col(c, "degree");
                String field       = col(c, "field");
                String start       = col(c, "start_date");
                String end         = col(c, "end_date");
                String gpa         = col(c, "gpa");
                String desc        = col(c, "description");

                sb.append("<div class='entry'>")
                  .append("<div class='row'>")
                  .append("<span class='etitle'>").append(h(institution)).append("</span>");
                String dateRange = datePair(start, end);
                if (!dateRange.isEmpty())
                    sb.append("<span class='edate'>").append(h(dateRange)).append("</span>");
                sb.append("</div>");

                String sub = degree;
                if (!field.isEmpty()) sub += (!sub.isEmpty() ? ", " : "") + field;
                if (!sub.isEmpty())  sb.append("<div class='esub'>").append(h(sub)).append("</div>");
                if (!gpa.isEmpty())  sb.append("<div class='esub'>GPA: ").append(h(gpa)).append("</div>");
                if (!desc.isEmpty()) sb.append("<div class='edesc'>").append(h(desc)).append("</div>");
                sb.append("</div>");
            }
        } catch (Exception ignored) {}
    }

    private void appendSkills(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_SKILLS, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>Skills</h2><div class='skills-list'>");
            while (c.moveToNext()) {
                String name  = col(c, "name");
                String level = col(c, "level");
                String chip  = h(name) + (level.isEmpty() ? "" : " <small>(" + h(level) + ")</small>");
                sb.append("<span class='skill'>").append(chip).append("</span>");
            }
            sb.append("</div>");
        } catch (Exception ignored) {}
    }

    private void appendProjects(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_PROJECTS, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>Projects</h2>");
            while (c.moveToNext()) {
                String name  = col(c, "name");
                String tech  = col(c, "technologies");
                String url   = col(c, "url");
                String start = col(c, "start_date");
                String end   = col(c, "end_date");
                String desc  = col(c, "description");

                sb.append("<div class='entry'>")
                  .append("<div class='row'>")
                  .append("<span class='etitle'>").append(h(name)).append("</span>");
                String dateRange = datePair(start, end);
                if (!dateRange.isEmpty())
                    sb.append("<span class='edate'>").append(h(dateRange)).append("</span>");
                sb.append("</div>");
                if (!tech.isEmpty()) sb.append("<div class='esub'>").append(h(tech)).append("</div>");
                if (!url.isEmpty())  sb.append("<div class='esub'><a href='").append(h(url)).append("'>").append(h(url)).append("</a></div>");
                if (!desc.isEmpty()) sb.append("<div class='edesc'>").append(h(desc)).append("</div>");
                sb.append("</div>");
            }
        } catch (Exception ignored) {}
    }

    private void appendCertifications(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_CERTIFICATIONS, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>Certifications</h2>");
            while (c.moveToNext()) {
                String name   = col(c, "name");
                String issuer = col(c, "issuer");
                String date   = col(c, "issue_date");
                String expiry = col(c, "expiry_date");
                String credId = col(c, "credential_id");
                String url    = col(c, "url");

                sb.append("<div class='entry'>")
                  .append("<div class='row'>")
                  .append("<span class='etitle'>").append(h(name)).append("</span>");
                if (!date.isEmpty())
                    sb.append("<span class='edate'>").append(h(date)).append("</span>");
                sb.append("</div>");
                if (!issuer.isEmpty())  sb.append("<div class='esub'>").append(h(issuer)).append("</div>");
                if (!expiry.isEmpty())  sb.append("<div class='esub'>Expires: ").append(h(expiry)).append("</div>");
                if (!credId.isEmpty())  sb.append("<div class='esub'>ID: ").append(h(credId)).append("</div>");
                if (!url.isEmpty())     sb.append("<div class='esub'><a href='").append(h(url)).append("'>").append(h(url)).append("</a></div>");
                sb.append("</div>");
            }
        } catch (Exception ignored) {}
    }

    private void appendAwards(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_AWARDS, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>Awards &amp; Achievements</h2>");
            while (c.moveToNext()) {
                String title  = col(c, "title");
                String issuer = col(c, "issuer");
                String date   = col(c, "date");
                String desc   = col(c, "description");

                sb.append("<div class='entry'>")
                  .append("<div class='row'>")
                  .append("<span class='etitle'>").append(h(title)).append("</span>");
                if (!date.isEmpty())
                    sb.append("<span class='edate'>").append(h(date)).append("</span>");
                sb.append("</div>");
                if (!issuer.isEmpty()) sb.append("<div class='esub'>").append(h(issuer)).append("</div>");
                if (!desc.isEmpty())   sb.append("<div class='edesc'>").append(h(desc)).append("</div>");
                sb.append("</div>");
            }
        } catch (Exception ignored) {}
    }

    private void appendLanguages(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_LANGUAGES, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>Languages</h2><div class='skills-list'>");
            while (c.moveToNext()) {
                String name  = col(c, "name");
                String level = col(c, "proficiency");
                String chip  = h(name) + (level.isEmpty() ? "" : " <small>(" + h(level) + ")</small>");
                sb.append("<span class='skill'>").append(chip).append("</span>");
            }
            sb.append("</div>");
        } catch (Exception ignored) {}
    }

    private void appendVolunteer(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_VOLUNTEER, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>Volunteer Work</h2>");
            while (c.moveToNext()) {
                String org   = col(c, "organization");
                String role  = col(c, "role");
                String start = col(c, "start_date");
                String end   = col(c, "end_date");
                String desc  = col(c, "description");

                sb.append("<div class='entry'>")
                  .append("<div class='row'>")
                  .append("<span class='etitle'>").append(h(role.isEmpty() ? org : role)).append("</span>");
                String dateRange = datePair(start, end);
                if (!dateRange.isEmpty())
                    sb.append("<span class='edate'>").append(h(dateRange)).append("</span>");
                sb.append("</div>");
                if (!role.isEmpty() && !org.isEmpty())
                    sb.append("<div class='esub'>").append(h(org)).append("</div>");
                if (!desc.isEmpty())
                    sb.append("<div class='edesc'>").append(h(desc)).append("</div>");
                sb.append("</div>");
            }
        } catch (Exception ignored) {}
    }

    private void appendPublications(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_PUBLICATIONS, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>Publications</h2>");
            while (c.moveToNext()) {
                String title     = col(c, "title");
                String publisher = col(c, "publisher");
                String date      = col(c, "date");
                String url       = col(c, "url");
                String desc      = col(c, "description");

                sb.append("<div class='entry'>")
                  .append("<div class='row'>")
                  .append("<span class='etitle'>").append(h(title)).append("</span>");
                if (!date.isEmpty())
                    sb.append("<span class='edate'>").append(h(date)).append("</span>");
                sb.append("</div>");
                if (!publisher.isEmpty()) sb.append("<div class='esub'>").append(h(publisher)).append("</div>");
                if (!url.isEmpty())       sb.append("<div class='esub'><a href='").append(h(url)).append("'>").append(h(url)).append("</a></div>");
                if (!desc.isEmpty())      sb.append("<div class='edesc'>").append(h(desc)).append("</div>");
                sb.append("</div>");
            }
        } catch (Exception ignored) {}
    }

    private void appendReferences(StringBuilder sb, SQLiteDatabase db, long resumeId) {
        try (Cursor c = db.query(DatabaseHelper.TABLE_REFERENCES, null,
                "resume_id = ?", new String[]{String.valueOf(resumeId)},
                null, null, "sort_order ASC, id ASC")) {
            if (c.getCount() == 0) return;
            sb.append("<h2>References</h2>");
            while (c.moveToNext()) {
                String name    = col(c, "name");
                String title   = col(c, "title");
                String company = col(c, "company");
                String email   = col(c, "email");
                String phone   = col(c, "phone");

                sb.append("<div class='entry'>")
                  .append("<span class='etitle'>").append(h(name)).append("</span>");
                String sub = title;
                if (!company.isEmpty()) sub += (!sub.isEmpty() ? " @ " : "") + company;
                if (!sub.isEmpty())   sb.append("<div class='esub'>").append(h(sub)).append("</div>");
                if (!email.isEmpty()) sb.append("<div class='esub'>").append(h(email)).append("</div>");
                if (!phone.isEmpty()) sb.append("<div class='esub'>").append(h(phone)).append("</div>");
                sb.append("</div>");
            }
        } catch (Exception ignored) {}
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    /** HTML-escape a string. Returns "" for null. */
    private static String h(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /** Reads a column value from the cursor; returns "" if absent or null. */
    private static String col(Cursor c, String column) {
        int idx = c.getColumnIndex(column);
        if (idx < 0 || c.isNull(idx)) return "";
        String v = c.getString(idx);
        return v != null ? v.trim() : "";
    }

    /** Formats a date range like "Jan 2020 – Present". */
    private static String datePair(String start, String end) {
        if (start.isEmpty() && end.isEmpty()) return "";
        if (start.isEmpty()) return end;
        if (end.isEmpty())   return start;
        return start + " – " + end;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
