package com.airesumebuilder.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.airesumebuilder.models.Resume;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper class for exporting resumes in various formats.
 */
public final class ExportUtils {

    private static final String TAG = "ExportUtils";

    private ExportUtils() {}

    /**
     * Exports the plain-text representation of a resume to the Downloads folder
     * and returns the file, or null on failure.
     */
    public static File exportAsTxt(Context context, Resume resume, String content) {
        try {
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) dir = context.getFilesDir();
            if (!dir.exists()) dir.mkdirs();

            String safeName = sanitiseFilename(resume.getTitle()) + ".txt";
            File   file     = new File(dir, safeName);

            try (FileWriter fw = new FileWriter(file)) {
                fw.write(content);
            }

            Log.d(TAG, "Exported TXT: " + file.getAbsolutePath());
            return file;

        } catch (IOException e) {
            Log.e(TAG, "exportAsTxt failed", e);
            return null;
        }
    }

    /**
     * Exports an HTML representation of a resume and returns the file.
     */
    public static File exportAsHtml(Context context, Resume resume, String htmlContent) {
        try {
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) dir = context.getFilesDir();
            if (!dir.exists()) dir.mkdirs();

            String safeName = sanitiseFilename(resume.getTitle()) + ".html";
            File   file     = new File(dir, safeName);

            try (FileWriter fw = new FileWriter(file)) {
                fw.write(htmlContent);
            }

            return file;

        } catch (IOException e) {
            Log.e(TAG, "exportAsHtml failed", e);
            return null;
        }
    }

    /** Launches the system share sheet for a given file. */
    public static void shareFile(Context context, File file, String mimeType) {
        try {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Share Resume"));
        } catch (Exception e) {
            Log.e(TAG, "shareFile failed", e);
        }
    }

    /** Removes characters that are unsafe in filenames. */
    private static String sanitiseFilename(String name) {
        if (name == null || name.isEmpty()) return "resume";
        return name.replaceAll("[^a-zA-Z0-9._\\- ]", "_").trim();
    }
}
