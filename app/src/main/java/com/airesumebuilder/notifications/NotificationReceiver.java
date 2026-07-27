package com.airesumebuilder.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.airesumebuilder.R;

/**
 * BroadcastReceiver that fires scheduled notifications (reminders, tips, etc.).
 */
public class NotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_TITLE   = "title";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_ID      = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title   = intent.getStringExtra(EXTRA_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        int    id      = intent.getIntExtra(EXTRA_ID, 1);

        if (title == null)   title   = "AI Resume Builder";
        if (message == null) message = "Time to update your resume!";

        Notification notification = new NotificationCompat.Builder(
                context, NotificationChannels.CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(id, notification);
    }
}
