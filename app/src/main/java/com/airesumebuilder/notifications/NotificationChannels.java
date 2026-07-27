package com.airesumebuilder.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

/**
 * Creates all notification channels required by the app.
 * Safe to call repeatedly (Android ignores duplicates).
 */
public class NotificationChannels {

    public static final String CHANNEL_REMINDERS  = "reminders";
    public static final String CHANNEL_EXPORT      = "export";
    public static final String CHANNEL_TIPS        = "tips";

    public static void createAll(Context context) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        nm.createNotificationChannel(new NotificationChannel(
                CHANNEL_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT));

        nm.createNotificationChannel(new NotificationChannel(
                CHANNEL_EXPORT,
                "Export & Backup",
                NotificationManager.IMPORTANCE_LOW));

        nm.createNotificationChannel(new NotificationChannel(
                CHANNEL_TIPS,
                "Career Tips",
                NotificationManager.IMPORTANCE_MIN));
    }
}
