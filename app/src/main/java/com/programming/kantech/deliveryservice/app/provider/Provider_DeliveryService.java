package com.programming.kantech.deliveryservice.app.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-14.
 *
 */

public class Provider_DeliveryService extends ContentProvider {

    // Declare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /**
     * Use DBHelper to manage database creation and version
     * management.
     */
    private DBHelper mOpenHelper;

    // Define final integer constants for the directory of tasks and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int DRIVERS_ALL = 100;
    public static final int DRIVERS_BY_ID = 101;

    /**
     * Define a static buildUriMatcher method that associates URI's with their int match
     *
     * @return UriMatcher
     */
    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */
        uriMatcher.addURI(Constants.CONTENT_AUTHORITY, Contract_DeliveryService.PATH_DRIVERS, DRIVERS_ALL);

        // replaces # with int id
        uriMatcher.addURI(Constants.CONTENT_AUTHORITY, Contract_DeliveryService.PATH_DRIVERS + "/#", DRIVERS_BY_ID);

        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        // Initialize the database helper
        mOpenHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);

        // Cursor returned
        Cursor retCursor;

        // Query for the tasks directory and write a default case
        switch (match) {
            case DRIVERS_ALL:
                //Log.i(Constants.LOG_TAG, "QUERY URI MATCH:MOVIES_ALL:" + uri);
                retCursor = db.query(Contract_DeliveryService.Entry_Drivers.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        // Get access to the database (to write new data to)
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);
        //Log.i(Constants.LOG_TAG, "Match:" + match);

        // URI to be returned
        Uri returnUri;

        // id of rec inserted
        long id;

        switch (match) {
            case DRIVERS_ALL:
                //Log.i(Constants.LOG_TAG, "INSERT INGREDIENTS_ALL called");
                id = db.insert(Contract_DeliveryService.Entry_Drivers.TABLE_NAME, null, values);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(Contract_DeliveryService.Entry_Drivers.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;


            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String
            s, @Nullable String[] strings) {
        return 0;
    }
}
