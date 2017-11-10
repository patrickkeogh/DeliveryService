package com.programming.kantech.deliveryservice.app.application;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by patrick keogh on 2017-11-10.
 * Checks to see if the activity was moved to the background
 * ref:https://stackoverflow.com/questions/5593115/run-code-when-android-app-is-closed-sent-to-background
 */

class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {
    @Override
    protected Boolean doInBackground(Context... params) {
        final Context context = params[0];
        return isAppOnForeground(context);
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
