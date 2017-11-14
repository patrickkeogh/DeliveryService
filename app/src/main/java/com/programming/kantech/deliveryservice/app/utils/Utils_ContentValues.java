package com.programming.kantech.deliveryservice.app.utils;

import android.content.ContentValues;

import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;

public class Utils_ContentValues {

    /**
     * Extract the values from a recipe to be used with a database.
     *
     * @param driver The driver object to be extracted
     * @return result ContentValues instance with the value of the driver
     */
//    public static ContentValues extractDriverValues(Driver driver) {
//        ContentValues result = new ContentValues();
//        //result.put(GatheringEntry._ID, gathering.getId());
//        result.put(Contract_DeliveryService.Entry_Drivers.COLUMN_DRIVER_UID, driver.getUid());
//        result.put(Contract_DeliveryService.Entry_Drivers.COLUMN_DRIVER_DISPLAY_NAME, driver.getDisplayName());
//        result.put(Contract_DeliveryService.Entry_Drivers.COLUMN_DRIVER_EMAIL, driver.getEmail());
//        result.put(Contract_DeliveryService.Entry_Drivers.COLUMN_DRIVER_APPROVED, driver.getDriverApproved());
//
//        return result;
//
//    }

    /**
     * Ensure this class is only used as a utility.
     */
    private Utils_ContentValues() {
        throw new AssertionError();
    }



}
