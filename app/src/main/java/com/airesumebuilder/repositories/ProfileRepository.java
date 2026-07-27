package com.airesumebuilder.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.airesumebuilder.database.DatabaseHelper;
import com.airesumebuilder.models.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for user-profile CRUD operations.
 */
public class ProfileRepository {

    private final DatabaseHelper dbHelper;

    public ProfileRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Profile p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insertOrThrow(DatabaseHelper.TABLE_PROFILES, null, toContentValues(p));
    }

    public int update(Profile p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(p);
        cv.put("updated_at", System.currentTimeMillis() / 1000);
        return db.update(DatabaseHelper.TABLE_PROFILES, cv,
                "id = ?", new String[]{String.valueOf(p.getId())});
    }

    public int delete(long id) {
        return dbHelper.getWritableDatabase()
                .delete(DatabaseHelper.TABLE_PROFILES,
                        "id = ?", new String[]{String.valueOf(id)});
    }

    public List<Profile> getAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Profile> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_PROFILES,
                null, null, null, null, null, "updated_at DESC");
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public Profile getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_PROFILES,
                null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            return c.moveToFirst() ? fromCursor(c) : null;
        } finally {
            c.close();
        }
    }

    public int count() {
        Cursor c = dbHelper.getReadableDatabase()
                .rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PROFILES, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private ContentValues toContentValues(Profile p) {
        ContentValues cv = new ContentValues();
        cv.put("first_name",   p.getFirstName());
        cv.put("last_name",    p.getLastName());
        cv.put("headline",     p.getHeadline());
        cv.put("email",        p.getEmail());
        cv.put("phone",        p.getPhone());
        cv.put("address",      p.getAddress());
        cv.put("city",         p.getCity());
        cv.put("state",        p.getState());
        cv.put("country",      p.getCountry());
        cv.put("linkedin",     p.getLinkedin());
        cv.put("github",       p.getGithub());
        cv.put("portfolio",    p.getPortfolio());
        cv.put("website",      p.getWebsite());
        cv.put("bio",          p.getBio());
        cv.put("date_of_birth", p.getDateOfBirth());
        cv.put("photo_path",   p.getPhotoPath());
        return cv;
    }

    private Profile fromCursor(Cursor c) {
        Profile p = new Profile();
        p.setId(          c.getLong(  c.getColumnIndexOrThrow("id")));
        p.setFirstName(   c.getString(c.getColumnIndexOrThrow("first_name")));
        p.setLastName(    c.getString(c.getColumnIndexOrThrow("last_name")));
        p.setHeadline(    c.getString(c.getColumnIndexOrThrow("headline")));
        p.setEmail(       c.getString(c.getColumnIndexOrThrow("email")));
        p.setPhone(       c.getString(c.getColumnIndexOrThrow("phone")));
        p.setAddress(     c.getString(c.getColumnIndexOrThrow("address")));
        p.setCity(        c.getString(c.getColumnIndexOrThrow("city")));
        p.setState(       c.getString(c.getColumnIndexOrThrow("state")));
        p.setCountry(     c.getString(c.getColumnIndexOrThrow("country")));
        p.setLinkedin(    c.getString(c.getColumnIndexOrThrow("linkedin")));
        p.setGithub(      c.getString(c.getColumnIndexOrThrow("github")));
        p.setPortfolio(   c.getString(c.getColumnIndexOrThrow("portfolio")));
        p.setWebsite(     c.getString(c.getColumnIndexOrThrow("website")));
        p.setBio(         c.getString(c.getColumnIndexOrThrow("bio")));
        p.setDateOfBirth( c.getString(c.getColumnIndexOrThrow("date_of_birth")));
        p.setPhotoPath(   c.getString(c.getColumnIndexOrThrow("photo_path")));
        p.setCreatedAt(   c.getLong(  c.getColumnIndexOrThrow("created_at")));
        p.setUpdatedAt(   c.getLong(  c.getColumnIndexOrThrow("updated_at")));
        return p;
    }
}
