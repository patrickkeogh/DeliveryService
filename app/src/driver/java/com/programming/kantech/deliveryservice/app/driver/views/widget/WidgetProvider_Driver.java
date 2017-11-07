package com.programming.kantech.deliveryservice.app.driver.views.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.driver.services.Service_DriverWidget;
import com.programming.kantech.deliveryservice.app.driver.views.activities.Activity_Main;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider_Driver extends AppWidgetProvider {

    static AppWidgetManager mAppWidgetManager;
    public static final String ACTION_DATA_UPDATED = "data_updated";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = getRemoteView(context);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    /**
     * Creates and returns the RemoteViews to be displayed in the widget
     *
     * @param context The context
     * @return The RemoteViews for the widget
     */
    private static RemoteViews getRemoteView(Context context) {
        //Log.i(Constants.LOG_TAG, "getRemoteView() called in WidgetProvider_Driver");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_driver);

        // Set the WidgetService_Driver intent to act as the adapter for the ListView
        Intent intent_service = new Intent(context, Service_DriverWidget.class);
        // set the listview
        views.setRemoteAdapter(R.id.widget_list_view_orders, intent_service);

        // Create the display date, today at 00:00 hrs
        Calendar date = new GregorianCalendar();
        long mDisplayDateStartTimeInMillis = Utils_General.getStartTimeForDate(date.getTimeInMillis());

        String showDate = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);

        views.setTextViewText(R.id.tv_widget_date, showDate);

        // Handle empty favs list
        views.setEmptyView(R.id.widget_list_view_orders, R.id.empty_view);

        Intent intent = new Intent(context, Activity_Main.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.iv_widget_logo, pendingIntent);



        return views;

    }

    // Called anytime any widget option is changed
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {

        // Update widget

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(Constants.LOG_TAG, "getRemoteView() called in WidgetProvider_Driver");

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //Log.i(Constants.LOG_TAG, "onReceive");

        if (intent.getAction().equals(ACTION_DATA_UPDATED)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));

            synchronized (this) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_list_view_orders);
            }

            //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_list_view_orders);
        }
    }

    /**
     * Updates all widget instances given the widget Ids and display information
     *
     * @param context          The calling context
     * @param appWidgetManager The widget manager
     * @param appWidgetIds     Array of widget Ids to be updated
     */
    public static void updateDriverWidgets(Context context, AppWidgetManager appWidgetManager,
                                           int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}

