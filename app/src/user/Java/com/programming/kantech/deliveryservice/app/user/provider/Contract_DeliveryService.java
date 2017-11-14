package com.programming.kantech.deliveryservice.app.user.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-13.
 *
 */

public class Contract_DeliveryService {

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + Constants.CONTENT_AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "places" directory
    public static final String PATH_PLACES = "places";

    public static final class PlaceEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();

        public static final String TABLE_NAME = "places";
        public static final String COLUMN_PLACE_ID = "placeID";
    }









}
