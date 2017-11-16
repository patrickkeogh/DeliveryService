package com.programming.kantech.deliveryservice.app.driver.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

/**
 * Created by patrick keogh on 2017-09-07.
 * Not being used yet
 */

public class Service_AppClosing extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.i(Constants.LOG_TAG, "App is closed");

        // Handle application closing
        Utils_General.showToast(getApplicationContext(), "App is closing, Log Driver Out");

        // Destroy the service
        stopSelf();
    }
}
