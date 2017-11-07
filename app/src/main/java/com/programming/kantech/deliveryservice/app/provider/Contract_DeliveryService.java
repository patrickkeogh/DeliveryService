package com.programming.kantech.deliveryservice.app.provider;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-13.
 *
 */

public class Contract_DeliveryService {

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + Constants.CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_PLACES = Entry_Places.TABLE_NAME;
    public static final String PATH_DRIVERS = Entry_Drivers.TABLE_NAME;

    public static final class Entry_Places implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();

        public static final String TABLE_NAME = "places";

        public static final String COLUMN_PLACE_ID = "placeID";
    }

    public static final class Entry_Drivers implements BaseColumns {

        // IngredientEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DRIVERS).build();

        public static final String TABLE_NAME = "delivery_drivers";

        //public static final String _ID = "_id";
        public static final String COLUMN_DRIVER_UID = "driver_uid";
        public static final String COLUMN_DRIVER_DISPLAY_NAME = "driver_display_name";
        public static final String COLUMN_DRIVER_EMAIL = "driver_email";
        public static final String COLUMN_DRIVER_APPROVED = "driver_approved";

        /**
         * Return a Uri that points to the row containing a given id.
         *
         * @param id id of the step
         * @return Uri
         */
        public static Uri buildStepUri(Long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Create an Ingredient object with the data from a cursor.
         *
         * @param cursor cursor containing the ingredient object
         * @return Step
         */
        public static Driver getDriverFromCursor(Cursor cursor) {

            Driver driver = new Driver();

            driver.setUid(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DRIVER_UID)));
            driver.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DRIVER_EMAIL)));
            driver.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DRIVER_EMAIL)));
            driver.setDriverApproved(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRIVER_APPROVED)) != 0);







            return driver;
        }
    }









}
