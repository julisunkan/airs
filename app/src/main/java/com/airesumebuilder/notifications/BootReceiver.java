package com.airesumebuilder.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Re-schedules alarms after device reboot (Android clears alarms on restart).
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed – rescheduling alarms");
            // Alarm rescheduling logic goes here when AlarmManager is used.
        }
    }
}
