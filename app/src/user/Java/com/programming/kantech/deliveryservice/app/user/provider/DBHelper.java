package com.programming.kantech.deliveryservice.app.user.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by patrick keogh on 2017-06-24.
 * An sqlite db helper used for storing user locations locally
 *
 */

public class DBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    // Name of the sql database on the device
    private static final String DATABASE_NAME = "kantech_delivery_service.db";

    /**
     * Constructor for DatabaseHelper
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold the places data
        final String SQL_CREATE_PLACES_TABLE = "CREATE TABLE " + Contract_DeliveryService.PlaceEntry.TABLE_NAME + " (" +
                Contract_DeliveryService.PlaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Contract_DeliveryService.PlaceEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, " +
                "UNIQUE (" + Contract_DeliveryService.PlaceEntry.COLUMN_PLACE_ID + ") ON CONFLICT REPLACE" +
                "); ";

        db.execSQL(SQL_CREATE_PLACES_TABLE);

    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now simply drop the table and create a new one.
        db.execSQL("DROP TABLE IF EXISTS " + Contract_DeliveryService.PlaceEntry.TABLE_NAME);
        onCreate(db);

    }
}
