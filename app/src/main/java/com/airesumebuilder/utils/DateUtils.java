package com.airesumebuilder.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date formatting helpers.
 */
public final class DateUtils {

    private DateUtils() {}

    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private static final SimpleDateFormat FULL_FMT =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    /**
     * Returns a relative string like "Just now", "2 hours ago", "Yesterday", or a date.
     *
     * @param epochSeconds Unix timestamp in seconds.
     */
    public static String formatRelative(long epochSeconds) {
        long nowMs    = System.currentTimeMillis();
        long thenMs   = epochSeconds * 1000L;
        long diffMs   = nowMs - thenMs;
        long diffMin  = TimeUnit.MILLISECONDS.toMinutes(diffMs);
        long diffHr   = TimeUnit.MILLISECONDS.toHours(diffMs);
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMs);

        if (diffMs  < 0)                return formatDate(epochSeconds);
        if (diffMin < 1)                return "Just now";
        if (diffMin < 60)               return diffMin + "m ago";
        if (diffHr  < 24)               return diffHr + "h ago";
        if (diffDays == 1)              return "Yesterday";
        if (diffDays < 7)               return diffDays + "d ago";
        return formatDate(epochSeconds);
    }

    /** Returns a time string like "14:32". */
    public static String formatTime(long epochMillis) {
        return TIME_FMT.format(new Date(epochMillis));
    }

    /** Returns a date string like "27 Jul 2026". */
    public static String formatDate(long epochSeconds) {
        return DATE_FMT.format(new Date(epochSeconds * 1000L));
    }

    /** Returns a full date+time string like "27 Jul 2026, 14:32". */
    public static String formatFull(long epochSeconds) {
        return FULL_FMT.format(new Date(epochSeconds * 1000L));
    }

    /** Returns the current date as "dd MMM yyyy". */
    public static String today() {
        return DATE_FMT.format(new Date());
    }
}
