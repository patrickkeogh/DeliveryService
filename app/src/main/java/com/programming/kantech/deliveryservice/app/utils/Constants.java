package com.programming.kantech.deliveryservice.app.utils;


import com.programming.kantech.deliveryservice.app.provider.Contract_DeliveryService;

/**
 * Created by patrick keogh on 2017-08-08.
 * Class that contains all the Constants required in our Delivery Service App
 */

public class Constants {

    /**
     * Debugging tag used by the Android logger.
     */
    public final static String LOG_TAG = "Tech Delivery Service:";

    public final static String KEY_GOOGLE = "AIzaSyDMz2TtZ-euSTsXGk5j99v2g1xDKXPsPf0";

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    //public static final String DATE_FORMAT_ = "yyyyMMdd";
    public static final String DATE_FORMAT = "EEEE, MMMM dd, yyyy";
    public static final String DATE_TIME_FORMAT = "MMM-dd-yyyy HH:mm:ss";

    //public static final String DATE_FORMAT = "MMM-dd-yyyy";



    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.programming.kantech.deliveryservice.app";

    /**
     * Codes for activity results
     */
    public static final int REQUEST_CODE_SIGN_IN = 1;
    public static final int REQUEST_CODE_SELECT_PICKUP_LOCATION = 2;
    public static final int REQUEST_CODE_LOCATION_PICKER = 3;
    public static final int REQUEST_CODE_SELECT_CUSTOMER = 4;
    public static final int REQUEST_CODE_SELECT_DELIVERY_LOCATION = 5;

    /**
     * The Constants used for data added to savedInstanceState
     */

    public static final String STATE_INFO_CUSTOMER_KEY = "com.programming.kantech.bakingmagic.app.state.customer.key";
    public static final String STATE_INFO_CUSTOMER_NAME = "com.programming.kantech.bakingmagic.app.state.customer.name";

    /**
     * The Constants used for data added as extras to intents
     */
    public static final String EXTRA_DRIVER = "com.programming.kantech.deliveryservice.app.extra.driver";
    public static final String EXTRA_DRIVER_KEY = "com.programming.kantech.deliveryservice.app.extra.driver.key";
    public static final String EXTRA_CUSTOMER_KEY = "com.programming.kantech.deliveryservice.app.extra.customer.key";
    public static final String EXTRA_CUSTOMER_NAME = "com.programming.kantech.deliveryservice.app.extra.customer.name";
    public static final String EXTRA_CUSTOMER = "com.programming.kantech.deliveryservice.app.extra.customer";
    public static final String EXTRA_LOCATION = "com.programming.kantech.deliveryservice.app.extra.location";
    public static final String EXTRA_LOCATION_NAME = "com.programming.kantech.deliveryservice.app.extra.location.name";
    public static final String EXTRA_LOCATION_ADDRESS = "com.programming.kantech.deliveryservice.app.extra.location.address";
    public static final String EXTRA_ACTIVITY_REF = "com.programming.kantech.deliveryservice.app.extra.activity_ref";

    /**
     * The Constants used for fragment tags
     */
    public static final String TAG_FRAGMENT_MAIN_ADD =
            "com.programming.kantech.deliveryservice.app.admin.views.ui.fragment_main_add";

    public static final String TAG_FRAGMENT_MAIN =
            "com.programming.kantech.deliveryservice.app.admin.views.ui.fragment_main";

    public static final String TAG_FRAGMENT_DRIVER_LIST =
            "com.programming.kantech.deliveryservice.app.admin.views.ui.fragment_driver_list";

    public static final String TAG_FRAGMENT_DRIVER_DETAILS =
            "com.programming.kantech.deliveryservice.app.admin.views.ui.fragment_driver_details";

    public static final String TAG_FRAGMENT_CUSTOMER_ADD =
            "com.programming.kantech.deliveryservice.app.admin.views.ui.fragment_customer_list";

    public static final String TAG_FRAGMENT_CUSTOMER_LIST =
            "com.programming.kantech.deliveryservice.app.admin.views.ui.fragment_customer_add";

    public static final String TAG_FRAGMENT_ORDER_ADD =
            "com.programming.kantech.deliveryservice.app.admin.views.ui.fragment_order_add";

    public static final String TAG_FRAGMENT_ORDER_LIST =
            "com.programming.kantech.deliveryservice.app.admin.views.ui.fragment_order_list";

    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    public static final int DRIVER_DETAIL_LOADER = 1;

    /**
     * The Constants for Firebase nodes
     */
    // Root Nodes
    public static final String FIREBASE_NODE_ADMIN = "user_admin";
    public static final String FIREBASE_NODE_DRIVERS = "user_drivers";
    public static final String FIREBASE_NODE_USERS = "user_users";
    public static final String FIREBASE_NODE_ORDERS = "orders";

    // Child Fields
    public static final String FIREBASE_CHILD_MAIN_ADDRESS = "main_address";


    /*
     * The columns of data that we are interested in displaying within our activity list of
     * drivers
     */
    public static final String[] LOADER_DRIVER_DETAIL_COLUMNS = {
            Contract_DeliveryService.Entry_Drivers._ID,
            Contract_DeliveryService.Entry_Drivers.COLUMN_DRIVER_UID,
            Contract_DeliveryService.Entry_Drivers.COLUMN_DRIVER_DISPLAY_NAME,
            Contract_DeliveryService.Entry_Drivers.COLUMN_DRIVER_EMAIL,
            Contract_DeliveryService.Entry_Drivers.COLUMN_DRIVER_APPROVED
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int COL_DRIVER_ID = 0;
    public static final int COL_DRIVER_UID = 1;
    public static final int COL_DRIVER_DISPLAY_NAME = 2;
    public static final int COL_DRIVER_EMAIL = 3;
    public static final int COL_DRIVER_APPROVED = 4;

    public static final String ORDER_TYPE_PHONE = "PHONE";
    public static final String ORDER_TYPE_USER = "USER";

    public static final String ORDER_STATUS_BOOKED = "BOOKED";
    public static final String ORDER_STATUS_ASSIGNED = "ASSIGNED";




}

