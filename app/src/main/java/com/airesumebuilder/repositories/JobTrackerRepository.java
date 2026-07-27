package com.airesumebuilder.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.airesumebuilder.database.DatabaseHelper;
import com.airesumebuilder.models.JobApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for job-tracker CRUD operations.
 */
public class JobTrackerRepository {

    private final DatabaseHelper dbHelper;

    public JobTrackerRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(JobApplication job) {
        return dbHelper.getWritableDatabase()
                .insertOrThrow(DatabaseHelper.TABLE_JOB_TRACKER, null, toContentValues(job));
    }

    public int update(JobApplication job) {
        ContentValues cv = toContentValues(job);
        cv.put("updated_at", System.currentTimeMillis() / 1000);
        return dbHelper.getWritableDatabase()
                .update(DatabaseHelper.TABLE_JOB_TRACKER, cv,
                        "id = ?", new String[]{String.valueOf(job.getId())});
    }

    public int delete(long id) {
        return dbHelper.getWritableDatabase()
                .delete(DatabaseHelper.TABLE_JOB_TRACKER,
                        "id = ?", new String[]{String.valueOf(id)});
    }

    public List<JobApplication> getAll() {
        return query(null, null, "created_at DESC");
    }

    public List<JobApplication> getByStatus(String status) {
        return query("status = ?", new String[]{status}, "created_at DESC");
    }

    public List<JobApplication> search(String q) {
        String like = "%" + q + "%";
        return query("company LIKE ? OR position LIKE ?",
                new String[]{like, like}, "created_at DESC");
    }

    public JobApplication getById(long id) {
        List<JobApplication> list = query("id = ?", new String[]{String.valueOf(id)}, null);
        return list.isEmpty() ? null : list.get(0);
    }

    public int count() {
        Cursor c = dbHelper.getReadableDatabase()
                .rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_JOB_TRACKER, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private List<JobApplication> query(String selection, String[] args, String orderBy) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<JobApplication> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_JOB_TRACKER,
                null, selection, args, null, null, orderBy);
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    private ContentValues toContentValues(JobApplication j) {
        ContentValues cv = new ContentValues();
        cv.put("company",          j.getCompany());
        cv.put("position",         j.getPosition());
        cv.put("status",           j.getStatus());
        cv.put("application_date", j.getApplicationDate());
        cv.put("interview_date",   j.getInterviewDate());
        cv.put("offer_amount",     j.getOfferAmount());
        cv.put("notes",            j.getNotes());
        cv.put("url",              j.getUrl());
        return cv;
    }

    private JobApplication fromCursor(Cursor c) {
        JobApplication j = new JobApplication();
        j.setId(              c.getLong(  c.getColumnIndexOrThrow("id")));
        j.setCompany(         c.getString(c.getColumnIndexOrThrow("company")));
        j.setPosition(        c.getString(c.getColumnIndexOrThrow("position")));
        j.setStatus(          c.getString(c.getColumnIndexOrThrow("status")));
        j.setApplicationDate( c.getString(c.getColumnIndexOrThrow("application_date")));
        j.setInterviewDate(   c.getString(c.getColumnIndexOrThrow("interview_date")));
        j.setOfferAmount(     c.getString(c.getColumnIndexOrThrow("offer_amount")));
        j.setNotes(           c.getString(c.getColumnIndexOrThrow("notes")));
        j.setUrl(             c.getString(c.getColumnIndexOrThrow("url")));
        j.setCreatedAt(       c.getLong(  c.getColumnIndexOrThrow("created_at")));
        j.setUpdatedAt(       c.getLong(  c.getColumnIndexOrThrow("updated_at")));
        return j;
    }
}
