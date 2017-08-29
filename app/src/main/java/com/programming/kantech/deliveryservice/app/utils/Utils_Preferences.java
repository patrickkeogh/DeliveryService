package com.programming.kantech.deliveryservice.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.programming.kantech.deliveryservice.app.R;

/**
 * Created by patrick keogh on 2017-08-15.
 */

public class Utils_Preferences {

    public static void saveHasTokenBeenSent(Context context, Boolean isSent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putBoolean(context.getString(R.string.key_pref_token_sent), isSent);
        spe.apply();
    }
    public static Boolean getHasTokenBeenSent(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.key_pref_token_sent), false);
    }

    /**
     * Ensure this class is only used as a utility.
     */
    private Utils_Preferences() {
        throw new AssertionError();
    }

}
