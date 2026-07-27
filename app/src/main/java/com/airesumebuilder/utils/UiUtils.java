package com.airesumebuilder.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;

/**
 * Common UI helpers used across the app.
 */
public final class UiUtils {

    private UiUtils() {}

    /** Copies text to the system clipboard and shows a snackbar. */
    public static void copyToClipboard(Context context, View rootView, String text) {
        ClipboardManager cm = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("AI Resume Builder", text));
        }
        showSnackbar(rootView, "Copied to clipboard");
    }

    /** Shows a short snackbar on the given root view. */
    public static void showSnackbar(View rootView, String message) {
        if (rootView != null) {
            Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /** Shows a snackbar with an action button. */
    public static void showSnackbarWithAction(View rootView, String message,
                                              String actionLabel, View.OnClickListener action) {
        if (rootView != null) {
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                    .setAction(actionLabel, action)
                    .show();
        }
    }

    /** Hides the soft keyboard. */
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /** Shows the soft keyboard for the given view. */
    public static void showKeyboard(Context context, View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /** Returns the string value of a TextView, or "" if null/blank. */
    public static String getText(android.widget.TextView tv) {
        if (tv == null || tv.getText() == null) return "";
        return tv.getText().toString().trim();
    }

    /** Returns true if a string is non-null and non-empty after trimming. */
    public static boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
