package com.programming.kantech.deliveryservice.app.driver.services;


import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.driver.views.widget.WidgetProvider_Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;


/**
 * Created by patrick keogh on 2017-10-10.
 * <p>
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class Service_WidgetUpdate extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public Service_WidgetUpdate() {
        super("WidgetUpdateService");
    }

    /**
     * Starts this service to perform UpdateBakingWidget action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateBakingWidget(Context context) {
        Intent intent = new Intent(context, Service_WidgetUpdate.class);
        intent.setAction(Constants.ACTION_UPDATE_DRIVER_WIDGET);
        context.startService(intent);
    }

    /**
     * @param intent contains the action to take
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_UPDATE_DRIVER_WIDGET.equals(action)) {
                handleActionUpdateDriverWidget();
            }
        }

    }

    /**
     * Handle action UpdateDriverWidget in the provided background thread
     */
    private void handleActionUpdateDriverWidget() {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager
                .getAppWidgetIds(new ComponentName(this, WidgetProvider_Driver.class));

        //Trigger data update to handle the ListView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view_orders);

        //Now update all widgets
        WidgetProvider_Driver.updateDriverWidgets(this, appWidgetManager, appWidgetIds);


    }
}
