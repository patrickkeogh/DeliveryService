package com.programming.kantech.deliveryservice.app.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by patrick keogh on 2017-08-09.
 * @class Utils_General
 * @brief Helper methods shared by various Activities.
 */

public class Utils_General {

    public static String[] getSpinnerMonths(){

        String[] months = new String[12];

        for (int i = 1; i <= 12; i++) {
            String val = "";
            if (i < 10) {
                val = "0";
            }
            months[i - 1] = val + String.valueOf(i);

        }
        return months;
    }

    public static String[] getSpinnerYears(){

        String[] years = new String[12];

        for (int i = 1; i <= 12; i++) {
            years[i - 1] = String.valueOf(i + 2016);
        }
        return years;
    }

    /**
     * Show a toast message.
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is used to hide a keyboard after a user has finished typing
     * the url.
     */
    public static void hideKeyboard(Activity activity, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isAvailable() &&
                activeNetwork.isConnected();


    }

    /**
     * Returns a Calendar representing the current day at midnight (00:00:00).
     *
     * @return Calendar object for current date
     */
    public static Calendar getTodayAtStartTime(){

        Calendar dateAtMidnight =  new GregorianCalendar();

        // reset hour, minutes, seconds and millis
        dateAtMidnight.set(Calendar.HOUR_OF_DAY, 0);
        dateAtMidnight.set(Calendar.MINUTE, 0);
        dateAtMidnight.set(Calendar.SECOND, 0);
        dateAtMidnight.set(Calendar.MILLISECOND, 0);

        return dateAtMidnight;
    }

    /**
     * Returns a long representing the start of the supplied date.
     *
     * @return long the supplied date at 00:00:00 in milliseconds
     */
    public static long getStartTimeForDate( long date_in){
        Log.i(Constants.LOG_TAG, "getStartTimeForDate:" + date_in);

        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(date_in);


        // reset hour, minutes, seconds and millis
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        return date.getTimeInMillis();
    }

    /**
     * Returns a Calendar representing the current day at 1 second before new day (23:59:59).
     *
     * @return Calendar object for current date
     */
    public static Calendar getTodayAtEndTime(){

        Calendar dateAtMidnight =  new GregorianCalendar();

        // reset hour, minutes, seconds and millis
        dateAtMidnight.set(Calendar.HOUR_OF_DAY, 23);
        dateAtMidnight.set(Calendar.MINUTE, 59);
        dateAtMidnight.set(Calendar.SECOND, 59);
        dateAtMidnight.set(Calendar.MILLISECOND, 0);

        return dateAtMidnight;
    }

    public static String getFormattedLongDateStringFromLongDate(long dateIn) {

        String strTime;

        // create a calendar
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(dateIn);

        String[] suffixes =
                //    0     1     2     3     4     5     6     7     8     9
                {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    10    11    12    13    14    15    16    17    18    19
                        "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                        //    20    21    22    23    24    25    26    27    28    29
                        "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    30    31
                        "th", "st"};

        SimpleDateFormat formatDayOfMonth = new SimpleDateFormat("d");
        int day = Integer.parseInt(formatDayOfMonth.format(cal.getTime()));
        String strDay = day + suffixes[day];


        SimpleDateFormat dayOfWeek = new SimpleDateFormat("EEEE");
        String strDayOfWeek = dayOfWeek.format(cal.getTime());

        SimpleDateFormat monthOfYear = new SimpleDateFormat("MMMM");
        String strMonthOfYear = monthOfYear.format(cal.getTime());

        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        String strYear = year.format(cal.getTime());


        return strDayOfWeek + " " + strMonthOfYear + " " + strDay + ", " + strYear;

    }

    /**
     * Demonstrates converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon.
     */
    public static BitmapDescriptor vectorToBitmap(Context context, @DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Ensure this class is only used as a utility.
     */
    private Utils_General() {
        throw new AssertionError();
    }


}
