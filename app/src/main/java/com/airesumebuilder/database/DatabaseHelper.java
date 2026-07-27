package com.airesumebuilder.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Central SQLite database helper for AI Resume Builder.
 *
 * <p>Creates and manages all application tables using foreign-key relationships.
 * Call {@link #getInstance(Context)} to obtain the singleton instance.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DB_NAME    = "ai_resume_builder.db";
    public static final int    DB_VERSION = 1;

    // ── Table names ──────────────────────────────────────────────────────────
    public static final String TABLE_PROFILES      = "profiles";
    public static final String TABLE_RESUMES       = "resumes";
    public static final String TABLE_EDUCATION     = "education";
    public static final String TABLE_EXPERIENCE    = "experience";
    public static final String TABLE_SKILLS        = "skills";
    public static final String TABLE_PROJECTS      = "projects";
    public static final String TABLE_CERTIFICATIONS = "certifications";
    public static final String TABLE_AWARDS        = "awards";
    public static final String TABLE_LANGUAGES     = "languages";
    public static final String TABLE_REFERENCES    = "references_table";
    public static final String TABLE_PUBLICATIONS  = "publications";
    public static final String TABLE_VOLUNTEER     = "volunteer";
    public static final String TABLE_JOB_TRACKER   = "job_tracker";
    public static final String TABLE_ANALYTICS     = "analytics";
    public static final String TABLE_FAVORITES     = "favorites";
    public static final String TABLE_SETTINGS      = "settings";
    public static final String TABLE_CUSTOM_SECTIONS = "custom_sections";
    public static final String TABLE_RESUME_VERSIONS = "resume_versions";

    // ── Common columns ───────────────────────────────────────────────────────
    public static final String COL_ID         = "id";
    public static final String COL_RESUME_ID  = "resume_id";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    /** Returns the singleton {@link DatabaseHelper}, creating it if necessary. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database schema v" + DB_VERSION);
        db.execSQL(CREATE_PROFILES);
        db.execSQL(CREATE_RESUMES);
        db.execSQL(CREATE_EDUCATION);
        db.execSQL(CREATE_EXPERIENCE);
        db.execSQL(CREATE_SKILLS);
        db.execSQL(CREATE_PROJECTS);
        db.execSQL(CREATE_CERTIFICATIONS);
        db.execSQL(CREATE_AWARDS);
        db.execSQL(CREATE_LANGUAGES);
        db.execSQL(CREATE_REFERENCES);
        db.execSQL(CREATE_PUBLICATIONS);
        db.execSQL(CREATE_VOLUNTEER);
        db.execSQL(CREATE_JOB_TRACKER);
        db.execSQL(CREATE_ANALYTICS);
        db.execSQL(CREATE_FAVORITES);
        db.execSQL(CREATE_SETTINGS);
        db.execSQL(CREATE_CUSTOM_SECTIONS);
        db.execSQL(CREATE_RESUME_VERSIONS);
        createIndexes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading DB from v" + oldVersion + " to v" + newVersion);
        // Future migrations go here; for now just recreate on version bump.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESUME_VERSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOM_SECTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANALYTICS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOB_TRACKER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOLUNTEER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUBLICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REFERENCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LANGUAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AWARDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CERTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SKILLS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPERIENCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDUCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESUMES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
        onCreate(db);
    }

    // ── CREATE statements ────────────────────────────────────────────────────

    private static final String CREATE_PROFILES =
        "CREATE TABLE " + TABLE_PROFILES + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "first_name TEXT," +
        "last_name TEXT," +
        "headline TEXT," +
        "email TEXT," +
        "phone TEXT," +
        "address TEXT," +
        "city TEXT," +
        "state TEXT," +
        "country TEXT," +
        "linkedin TEXT," +
        "github TEXT," +
        "portfolio TEXT," +
        "website TEXT," +
        "bio TEXT," +
        "date_of_birth TEXT," +
        "photo_path TEXT," +
        "created_at INTEGER DEFAULT (strftime('%s','now'))," +
        "updated_at INTEGER DEFAULT (strftime('%s','now'))" +
        ")";

    private static final String CREATE_RESUMES =
        "CREATE TABLE " + TABLE_RESUMES + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "profile_id INTEGER," +
        "title TEXT NOT NULL," +
        "template TEXT DEFAULT 'modern'," +
        "accent_color TEXT DEFAULT '#1565C0'," +
        "font TEXT DEFAULT 'Default'," +
        "is_favorite INTEGER DEFAULT 0," +
        "ats_score INTEGER DEFAULT 0," +
        "overall_score INTEGER DEFAULT 0," +
        "tags TEXT," +
        "section_order TEXT," +
        "created_at INTEGER DEFAULT (strftime('%s','now'))," +
        "updated_at INTEGER DEFAULT (strftime('%s','now'))," +
        "FOREIGN KEY (profile_id) REFERENCES " + TABLE_PROFILES + "(id) ON DELETE SET NULL" +
        ")";

    private static final String CREATE_EDUCATION =
        "CREATE TABLE " + TABLE_EDUCATION + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "institution TEXT," +
        "degree TEXT," +
        "field TEXT," +
        "start_date TEXT," +
        "end_date TEXT," +
        "gpa TEXT," +
        "description TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_EXPERIENCE =
        "CREATE TABLE " + TABLE_EXPERIENCE + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "company TEXT," +
        "position TEXT," +
        "location TEXT," +
        "start_date TEXT," +
        "end_date TEXT," +
        "is_current INTEGER DEFAULT 0," +
        "description TEXT," +
        "achievements TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_SKILLS =
        "CREATE TABLE " + TABLE_SKILLS + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "name TEXT NOT NULL," +
        "level TEXT," +
        "category TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_PROJECTS =
        "CREATE TABLE " + TABLE_PROJECTS + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "name TEXT," +
        "description TEXT," +
        "technologies TEXT," +
        "url TEXT," +
        "start_date TEXT," +
        "end_date TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_CERTIFICATIONS =
        "CREATE TABLE " + TABLE_CERTIFICATIONS + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "name TEXT," +
        "issuer TEXT," +
        "issue_date TEXT," +
        "expiry_date TEXT," +
        "credential_id TEXT," +
        "url TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_AWARDS =
        "CREATE TABLE " + TABLE_AWARDS + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "title TEXT," +
        "issuer TEXT," +
        "date TEXT," +
        "description TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_LANGUAGES =
        "CREATE TABLE " + TABLE_LANGUAGES + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "name TEXT NOT NULL," +
        "proficiency TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_REFERENCES =
        "CREATE TABLE " + TABLE_REFERENCES + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "name TEXT," +
        "title TEXT," +
        "company TEXT," +
        "email TEXT," +
        "phone TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_PUBLICATIONS =
        "CREATE TABLE " + TABLE_PUBLICATIONS + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "title TEXT," +
        "publisher TEXT," +
        "date TEXT," +
        "url TEXT," +
        "description TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_VOLUNTEER =
        "CREATE TABLE " + TABLE_VOLUNTEER + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "organization TEXT," +
        "role TEXT," +
        "start_date TEXT," +
        "end_date TEXT," +
        "description TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_JOB_TRACKER =
        "CREATE TABLE " + TABLE_JOB_TRACKER + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "company TEXT NOT NULL," +
        "position TEXT NOT NULL," +
        "status TEXT DEFAULT 'Applied'," +
        "application_date TEXT," +
        "interview_date TEXT," +
        "offer_amount TEXT," +
        "notes TEXT," +
        "url TEXT," +
        "created_at INTEGER DEFAULT (strftime('%s','now'))," +
        "updated_at INTEGER DEFAULT (strftime('%s','now'))" +
        ")";

    private static final String CREATE_ANALYTICS =
        "CREATE TABLE " + TABLE_ANALYTICS + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "event_type TEXT NOT NULL," +
        "event_data TEXT," +
        "timestamp INTEGER DEFAULT (strftime('%s','now'))" +
        ")";

    private static final String CREATE_FAVORITES =
        "CREATE TABLE " + TABLE_FAVORITES + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "item_type TEXT NOT NULL," +
        "item_id INTEGER NOT NULL," +
        "created_at INTEGER DEFAULT (strftime('%s','now'))" +
        ")";

    private static final String CREATE_SETTINGS =
        "CREATE TABLE " + TABLE_SETTINGS + " (" +
        "key TEXT PRIMARY KEY," +
        "value TEXT" +
        ")";

    private static final String CREATE_CUSTOM_SECTIONS =
        "CREATE TABLE " + TABLE_CUSTOM_SECTIONS + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "section_title TEXT NOT NULL," +
        "content TEXT," +
        "sort_order INTEGER DEFAULT 0," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_RESUME_VERSIONS =
        "CREATE TABLE " + TABLE_RESUME_VERSIONS + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "resume_id INTEGER NOT NULL," +
        "version_name TEXT," +
        "snapshot_json TEXT NOT NULL," +
        "created_at INTEGER DEFAULT (strftime('%s','now'))," +
        "FOREIGN KEY (resume_id) REFERENCES " + TABLE_RESUMES + "(id) ON DELETE CASCADE" +
        ")";

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_resumes_profile ON " + TABLE_RESUMES + "(profile_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_education_resume ON " + TABLE_EDUCATION + "(resume_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_experience_resume ON " + TABLE_EXPERIENCE + "(resume_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_skills_resume ON " + TABLE_SKILLS + "(resume_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_projects_resume ON " + TABLE_PROJECTS + "(resume_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_job_tracker_status ON " + TABLE_JOB_TRACKER + "(status)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_analytics_event ON " + TABLE_ANALYTICS + "(event_type)");
    }
}
