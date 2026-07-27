package com.airesumebuilder.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.airesumebuilder.database.DatabaseHelper;
import com.airesumebuilder.models.Resume;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for all resume CRUD operations.
 * All methods are synchronous; callers must invoke them from a background thread.
 */
public class ResumeRepository {

    private static final String TAG = "ResumeRepository";

    private final DatabaseHelper dbHelper;

    public ResumeRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // ── Insert ───────────────────────────────────────────────────────────────

    /**
     * Inserts a new resume and returns its generated row ID, or -1 on failure.
     */
    public long insert(Resume resume) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cv = toContentValues(resume);
            long id = db.insertOrThrow(DatabaseHelper.TABLE_RESUMES, null, cv);
            Log.d(TAG, "Inserted resume id=" + id);
            return id;
        } catch (Exception e) {
            Log.e(TAG, "insert failed", e);
            return -1;
        }
    }

    // ── Update ───────────────────────────────────────────────────────────────

    /** Updates an existing resume; returns number of rows affected. */
    public int update(Resume resume) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(resume);
        cv.put("updated_at", System.currentTimeMillis() / 1000);
        return db.update(DatabaseHelper.TABLE_RESUMES, cv,
                "id = ?", new String[]{String.valueOf(resume.getId())});
    }

    /** Toggles the favourite flag for a given resume. */
    public int setFavorite(long resumeId, boolean isFavorite) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_favorite", isFavorite ? 1 : 0);
        return db.update(DatabaseHelper.TABLE_RESUMES, cv,
                "id = ?", new String[]{String.valueOf(resumeId)});
    }

    /** Updates the ATS and overall scores for a resume. */
    public int updateScores(long resumeId, int atsScore, int overallScore) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ats_score",     atsScore);
        cv.put("overall_score", overallScore);
        cv.put("updated_at",    System.currentTimeMillis() / 1000);
        return db.update(DatabaseHelper.TABLE_RESUMES, cv,
                "id = ?", new String[]{String.valueOf(resumeId)});
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    /** Deletes a resume and all its cascaded child rows. */
    public int delete(long resumeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_RESUMES,
                "id = ?", new String[]{String.valueOf(resumeId)});
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    /** Returns all resumes ordered by most-recently-updated. */
    public List<Resume> getAll() {
        return query(null, null, "updated_at DESC");
    }

    /** Returns the N most-recently-updated resumes. */
    public List<Resume> getRecent(int limit) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Resume> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_RESUMES,
                null, null, null, null, null,
                "updated_at DESC", String.valueOf(limit));
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    /** Returns only favourite resumes. */
    public List<Resume> getFavorites() {
        return query("is_favorite = 1", null, "updated_at DESC");
    }

    /** Returns a single resume by ID, or null if not found. */
    public Resume getById(long id) {
        List<Resume> list = query("id = ?",
                new String[]{String.valueOf(id)}, null);
        return list.isEmpty() ? null : list.get(0);
    }

    /** Full-text search across resume titles and tags. */
    public List<Resume> search(String query) {
        String like = "%" + query + "%";
        return this.query("title LIKE ? OR tags LIKE ?",
                new String[]{like, like}, "updated_at DESC");
    }

    /** Returns the total number of resumes. */
    public int count() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RESUMES, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private List<Resume> query(String selection, String[] args, String orderBy) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Resume> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_RESUMES,
                null, selection, args, null, null, orderBy);
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    private ContentValues toContentValues(Resume r) {
        ContentValues cv = new ContentValues();
        if (r.getProfileId() > 0)     cv.put("profile_id",    r.getProfileId());
        if (r.getTitle() != null)      cv.put("title",         r.getTitle());
        if (r.getTemplate() != null)   cv.put("template",      r.getTemplate());
        if (r.getAccentColor() != null) cv.put("accent_color", r.getAccentColor());
        if (r.getFont() != null)       cv.put("font",          r.getFont());
        cv.put("is_favorite",   r.isFavorite() ? 1 : 0);
        cv.put("ats_score",     r.getAtsScore());
        cv.put("overall_score", r.getOverallScore());
        if (r.getTags() != null)         cv.put("tags",          r.getTags());
        if (r.getSectionOrder() != null) cv.put("section_order", r.getSectionOrder());
        return cv;
    }

    private Resume fromCursor(Cursor c) {
        Resume r = new Resume();
        r.setId(           c.getLong(  c.getColumnIndexOrThrow("id")));
        r.setProfileId(    c.getLong(  c.getColumnIndexOrThrow("profile_id")));
        r.setTitle(        c.getString(c.getColumnIndexOrThrow("title")));
        r.setTemplate(     c.getString(c.getColumnIndexOrThrow("template")));
        r.setAccentColor(  c.getString(c.getColumnIndexOrThrow("accent_color")));
        r.setFont(         c.getString(c.getColumnIndexOrThrow("font")));
        r.setFavorite(     c.getInt(   c.getColumnIndexOrThrow("is_favorite")) == 1);
        r.setAtsScore(     c.getInt(   c.getColumnIndexOrThrow("ats_score")));
        r.setOverallScore( c.getInt(   c.getColumnIndexOrThrow("overall_score")));
        r.setTags(         c.getString(c.getColumnIndexOrThrow("tags")));
        r.setSectionOrder( c.getString(c.getColumnIndexOrThrow("section_order")));
        r.setCreatedAt(    c.getLong(  c.getColumnIndexOrThrow("created_at")));
        r.setUpdatedAt(    c.getLong(  c.getColumnIndexOrThrow("updated_at")));
        return r;
    }
}
