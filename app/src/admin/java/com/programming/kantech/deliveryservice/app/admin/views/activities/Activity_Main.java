package com.programming.kantech.deliveryservice.app.admin.views.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_CustomerAdd;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_CustomerDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_CustomerList;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_DriverDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_DriverList;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_MainDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_NewPhoneOrder;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_OrderDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_OrderList;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Customer;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick keogh on 2017-08-18.
 * Main activty for the Admin Flavor.  Manages multiple fragments at one time
 * All fragments have the OnFragmentLoaded(tag) callback
 */

public class Activity_Main extends AppCompatActivity implements
        Fragment_MainDetails.MainDetailsFragmentListener,
        Fragment_DriverList.DriverClickListener,
        Fragment_OrderList.OrderListFragmentListener,
        Fragment_OrderDetails.OrderDetailFragmentListener,
        Fragment_CustomerList.CustomerClickListener,
        Fragment_NewPhoneOrder.NewOrderListener,
        Fragment_DriverDetails.VerifyDriverClickListener,
        Fragment_CustomerAdd.SaveCustomerListener,
        Fragment_CustomerDetails.LocationAddedListener,
        NavigationView.OnNavigationItemSelectedListener {


    private FragmentManager mFragmentManager;

    private ActionBar mActionBar;

    // Track whether to display a two-column or single-column UI
    private boolean mLandscapeView;
    private boolean mIsFirstTimeLoaded = true;
    private ActionBarDrawerToggle mDrawerToggle;

    // 12:00 AM of the selected day
    private long mDisplayDateStartTimeInMillis;

    private boolean mShowDriverIcon = true;
    private boolean mShowDrivers = true;
    private boolean mShowOrderIcon = true;
    private boolean mShowOrders = true;

    private boolean mShowDateNav = true;
    private boolean mToolBarNavigationListenerIsRegistered;

    // Bind the layout views
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;

    @BindView(R.id.nav_view)
    NavigationView mNavView;

    // We cannot use butterknife for these because details may not be present all the time
    // and null will crash
    private FrameLayout container_details;
    private FrameLayout container_master;


    // Member variables for the Firebase database
    //private DatabaseReference mUserRef;
    private DatabaseReference mDriverRef;


    private MenuItem mMenuItem_Previous;
    private MenuItem mMenuItem_Next;
    private MenuItem mMenuItem_Date;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_DATE_FILTER)) {
                mDisplayDateStartTimeInMillis = savedInstanceState.getLong(Constants.STATE_INFO_DATE_FILTER);
            }
        }else{

            // Create the display date, today at 00:00 hrs
            Calendar date = new GregorianCalendar();
            mDisplayDateStartTimeInMillis = Utils_General.getStartTimeForDate(date.getTimeInMillis());
        }

        container_details = (FrameLayout) findViewById(R.id.container_details);
        container_master = (FrameLayout) findViewById(R.id.container_master);

        ButterKnife.bind(this);

        mFragmentManager = getSupportFragmentManager();



        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        // Initialize ActionBarDrawerToggle, which will control toggle of hamburger.
        // You set the values of R.string.open and R.string.close accordingly.
        // Also, you can implement drawer toggle listener if you want.
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // Setting the actionbarToggle to drawer layout
        mDrawer.addDrawerListener(mDrawerToggle);

        // Calling sync state is necessary to show your hamburger icon...
        // or so I hear. Doesn't hurt including it even if you find it works
        // without it on your test device(s)
        mDrawerToggle.syncState();

        mNavView.setNavigationItemSelectedListener(this);
        mToolBarNavigationListenerIsRegistered = true;

        Menu nav_Menu = mNavView.getMenu();
        nav_Menu.findItem(R.id.nav_admin_manage_drivers).setVisible(true);

        //mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ADMIN);
        mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);

        // Get the current orientation
        mLandscapeView = (findViewById(R.id.layout_for_two_cols) != null);
        Log.i(Constants.LOG_TAG, "Update Landscape variable to:" + mLandscapeView);

        if (savedInstanceState == null && mIsFirstTimeLoaded) {
            // add main fragment to master container on first start
            mIsFirstTimeLoaded = false;
            Fragment_MainDetails fragment = Fragment_MainDetails.newInstance(mDisplayDateStartTimeInMillis);
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN_DETAILS, fragment, true);

        }
    }

    private void enableViews(boolean enable, String title) {

        Log.i(Constants.LOG_TAG, "EnableViews called()" + enable);

        // Set the title
        // Remove back button
        if (mActionBar != null) {
            mActionBar.setTitle(title);
        }

        // To keep states of ActionBar and ActionBarDrawerToggle synchronized,
        // when you enable on one, you disable on the other.
        // And as you may notice, the order for this operation is disable first, then enable - VERY VERY IMPORTANT.
        if (enable) {
            // Remove hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(false);

            // Show back button
            if (mActionBar != null) {
                mActionBar.setDisplayHomeAsUpEnabled(true);
            }
            // when DrawerToggle is disabled i.e. setDrawerIndicatorEnabled(false), navigation icon
            // clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward
            // click events to this listener.
            if (!mToolBarNavigationListenerIsRegistered) {
                mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Doesn't have to be onBackPressed
                        onBackPressed();
                    }
                });

                mToolBarNavigationListenerIsRegistered = true;
            }

        } else {
            // Remove back button
            if (mActionBar != null) {
                mActionBar.setDisplayHomeAsUpEnabled(false);
            }
            // Show hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(true);

            // Remove the/any drawer toggle listener
            mDrawerToggle.setToolbarNavigationClickListener(null);
            mToolBarNavigationListenerIsRegistered = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // if we are in 2 pane mode, remove what ever is in the details container
        // when loading the lookup lists
        if (mLandscapeView) clearDetailsContainer();

        // Any nav item selected should be a root item so clear the backstack
        for (int i = 0; i < mFragmentManager.getBackStackEntryCount(); ++i) {
            mFragmentManager.popBackStack();
        }

        if (id == R.id.nav_admin_home) {

            Fragment_MainDetails fragment = Fragment_MainDetails.newInstance(mDisplayDateStartTimeInMillis);
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN_DETAILS, fragment, true);

        } else if (id == R.id.nav_admin_manage_drivers) {

            Fragment_DriverList frag_driver = Fragment_DriverList.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_DRIVER_LIST, frag_driver, false);

        } else if (id == R.id.nav_admin_manage_customers) {

            Fragment_CustomerList frag_customer = Fragment_CustomerList.newInstance(null);
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_LIST, frag_customer, false);

        } else if (id == R.id.nav_admin_manage_orders) {

            Fragment_OrderList frag_order_list = Fragment_OrderList.newInstance(mDisplayDateStartTimeInMillis);
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_LIST, frag_order_list, false);

        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clearDetailsContainer() {

        if (mFragmentManager.findFragmentById(R.id.container_details) != null) {

            mFragmentManager.beginTransaction()
                    .remove(mFragmentManager.findFragmentById(R.id.container_details)).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        mMenuItem_Previous = menu.findItem(R.id.action_admin_orders_previous);
        mMenuItem_Next = menu.findItem(R.id.action_admin_orders_next);
        mMenuItem_Date = menu.findItem(R.id.action_admin_orders_select_date);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i(Constants.LOG_TAG, "onPrepareOptionsMenu called");

        mMenuItem_Previous.setVisible(mShowDateNav);
        mMenuItem_Next.setVisible(mShowDateNav);
        mMenuItem_Date.setVisible(mShowDateNav);

//        mMenuItem_Driver.setVisible(mShowDriverIcon);
//        if (!mShowDrivers) {
//            mMenuItem_Driver.setIcon(R.drawable.ic_menu_drive);
//        } else {
//            mMenuItem_Driver.setIcon(R.drawable.ic_menu_drive_white);
//        }
//
//        mMenuItem_Order.setVisible(mShowOrderIcon);
//        if (!mShowOrders) {
//            mMenuItem_Order.setIcon(R.drawable.ic_shopping_cart);
//        } else {
//            mMenuItem_Order.setIcon(R.drawable.ic_shopping_cart_white);
//        }


        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_admin_orders_previous:

                mDisplayDateStartTimeInMillis -= Constants.DAY;

                //remove details frag if one is loaded
                if (mLandscapeView) {
                    clearDetailsContainer();
                }

                if (mFragmentManager.findFragmentById(R.id.container_master) != null) {

                    // Get the fragment in the master container

                    Fragment frag = mFragmentManager.findFragmentById(R.id.container_master);

                    if(frag instanceof Fragment_MainDetails){
                        Fragment_MainDetails fragment = Fragment_MainDetails.newInstance(mDisplayDateStartTimeInMillis);
                        replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN_DETAILS, fragment, true);
                    }else if(frag instanceof Fragment_OrderList){
                        Fragment_OrderList frag_order_list = Fragment_OrderList.newInstance(mDisplayDateStartTimeInMillis);
                        replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_LIST, frag_order_list, false);
                    }
                }



                //replaceMasterListFragment();

                return true;

            case R.id.action_admin_orders_next:

                mDisplayDateStartTimeInMillis += Constants.DAY;

                //remove details frag if one is loaded
                if (mLandscapeView) {
                    clearDetailsContainer();
                }

                if (mFragmentManager.findFragmentById(R.id.container_master) != null) {

                    // Get the fragment in the master container

                    Fragment frag = mFragmentManager.findFragmentById(R.id.container_master);

                    if(frag instanceof Fragment_MainDetails){
                        Fragment_MainDetails fragment = Fragment_MainDetails.newInstance(mDisplayDateStartTimeInMillis);
                        replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN_DETAILS, fragment, true);
                    }else if(frag instanceof Fragment_OrderList){
                        Fragment_OrderList frag_order_list = Fragment_OrderList.newInstance(mDisplayDateStartTimeInMillis);
                        replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_LIST, frag_order_list, false);
                    }
                }


                return true;

            case R.id.action_admin_orders_select_date:

                if (mLandscapeView) {
                    clearDetailsContainer();
                }

                Calendar cal = Calendar.getInstance(TimeZone.getDefault()); // Get current date

                // Create the DatePickerDialog instance
                DatePickerDialog datePicker = new DatePickerDialog(this,
                        R.style.ThemeOverlay_AppCompat_Dialog_Alert, datePickerListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH));
                datePicker.setCancelable(false);
                datePicker.setTitle("Select A Date");
                datePicker.show();

                return true;

            case R.id.action_sign_out:
                Log.i(Constants.LOG_TAG, "Sign out clicked:");
                AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Intent intent = new Intent(Activity_Main.this, Activity_Splash.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                });

                return true;
            case android.R.id.home:
                Log.i(Constants.LOG_TAG, "Home clicked");
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void replaceFragment(int container, String fragment_tag,
                                 Fragment fragment_in, boolean addToBackStack) {

        // Get a fragment transaction to replace fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (!Objects.equals(fragment_tag, Constants.TAG_FRAGMENT_MAIN_DETAILS)) {
            mShowDriverIcon = false;
            mShowOrderIcon = false;

        } else {
            mShowDriverIcon = true;
            mShowOrderIcon = true;
        }

        invalidateOptionsMenu();

        if (Objects.equals(fragment_tag, Constants.TAG_FRAGMENT_MAIN_LIST_START)) {
            transaction.add(container, fragment_in, fragment_tag);
            if (addToBackStack) transaction.addToBackStack(null);
        } else {
            transaction.replace(container, fragment_in, fragment_tag);
            if (addToBackStack) transaction.addToBackStack(null);
        }

        // Commit the transaction
        transaction.commit();

    }

    @Override
    public void onDriverSelected(Driver selectedDriver) {

        Fragment_DriverDetails fragment = Fragment_DriverDetails.newInstance(selectedDriver);

        if (mLandscapeView) {
            replaceFragment(R.id.container_details, Constants.TAG_FRAGMENT_DRIVER_DETAILS, fragment, false);
        } else {
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_DRIVER_DETAILS, fragment, true);
        }

    }

    @Override
    public void onVerifyDriverClicked(Driver driver) {

        driver.setDriverApproved(true);

        mDriverRef.child(driver.getUid()).setValue(driver);

    }

//    @Override
//    public void onGetCustomerSelected() {
//        Log.i(Constants.LOG_TAG, "onGetCustomerSelected called");
//
//        //Fragment_CustomerList fragment = Fragment_CustomerList.newInstance();
//        //replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_LIST, fragment);
//
//        Intent intent = new Intent(this, Activity_SelectCustomer.class);
//        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_CUSTOMER);
//
//    }
//
//    @Override
//    public void onGetLocationPickup(Customer customer) {
//        Log.i(Constants.LOG_TAG, "onGetLocationPickup called CustKey:" + customer.getId());
//
//        Intent intent = new Intent(this, Activity_SelectLocation.class);
//
//        intent.putExtra(Constants.EXTRA_CUSTOMER_KEY, customer.getId());
//        intent.putExtra(Constants.EXTRA_CUSTOMER_NAME, customer.getCompany());
//        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION);
//
//    }
//
//    @Override
//    public void onGetLocationDelivery() {
//
//    }

    // Callback from Fragment_CustomerList
    @Override
    public void onCustomerSelected(Customer customer) {
        //Log.i(Constants.LOG_TAG, "onCustomerSelected called CustKey:" + customer);

        Fragment_CustomerDetails fragment = Fragment_CustomerDetails.newInstance(customer);

        if (mLandscapeView) {
            replaceFragment(R.id.container_details, Constants.TAG_FRAGMENT_CUSTOMER_DETAILS, fragment, false);
        } else {
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_DETAILS, fragment, true);
        }
    }

    @Override
    public void onAddCustomerClicked() {

        Log.i(Constants.LOG_TAG, "onAddCustomerClicked() called");

        Fragment_CustomerAdd fragment = Fragment_CustomerAdd.newInstance();
        replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_ADD, fragment, true);

    }

    @Override
    public void onOrderClicked(Order order) {
        Log.i(Constants.LOG_TAG, "onOrderSelected() called");

        Fragment_OrderDetails fragment = Fragment_OrderDetails.newInstance(order);

        if (mLandscapeView) {
            replaceFragment(R.id.container_details, Constants.TAG_FRAGMENT_ORDER_DETAILS, fragment, false);

        } else {
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_DETAILS, fragment, true);
        }
    }

    @Override
    public void onAddOrderClicked() {
        // Called from Fragment_Order_List

        // Replace master container with the new order form.
        // This will be full screen in all views
        Fragment_NewPhoneOrder frag_order = Fragment_NewPhoneOrder.newInstance(null);
        replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_ADD, frag_order, true);

    }

    @Override
    public void onFragmentLoaded(String tag) {
        Log.i(Constants.LOG_TAG, "onFragmentLoaded Calleed():" + tag);

        if (mLandscapeView) {

            switch (tag) {
                // first the frags we want to show in full screen
                case Constants.TAG_FRAGMENT_MAIN_DETAILS:
                case Constants.TAG_FRAGMENT_CUSTOMER_ADD:
                case Constants.TAG_FRAGMENT_ORDER_ADD:

                    container_details.setVisibility(View.GONE);
                    container_master.setLayoutParams(new LinearLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                    break;
                default:
                    // everything else we have a normal master/details setup
                    container_details.setVisibility(View.VISIBLE);

                    final float scale = getResources().getDisplayMetrics().density;
                    int requiredWidth = getResources().getInteger(R.integer.container_width);

                    int pixels = (int) (requiredWidth * scale + 0.5f);

                    container_master.setLayoutParams(new LinearLayout.LayoutParams(
                            pixels, FrameLayout.LayoutParams.MATCH_PARENT));

            }
        }

        String showDate = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);


        switch (tag) {
            case Constants.TAG_FRAGMENT_MAIN_DETAILS:
                mShowDateNav = true;
                enableViews(false, showDate);
                break;

            case Constants.TAG_FRAGMENT_ORDER_LIST:
                mShowDateNav = true;
                enableViews(false, showDate);
                break;

            case Constants.TAG_FRAGMENT_ORDER_DETAILS:
                if (!mLandscapeView) enableViews(true, "Order Details");
                if (!mLandscapeView) mShowDateNav = false;;
                break;

            case Constants.TAG_FRAGMENT_CUSTOMER_LIST:
                mShowDateNav = false;
                enableViews(false, "Manage Customers");
                break;

            case Constants.TAG_FRAGMENT_CUSTOMER_DETAILS:
                mShowDateNav = false;
                if (!mLandscapeView) enableViews(true, "Customer Details");

                break;

            case Constants.TAG_FRAGMENT_DRIVER_LIST:
                mShowDateNav = false;
                enableViews(false, "Manage Drivers");
                break;

            case Constants.TAG_FRAGMENT_DRIVER_DETAILS:
                mShowDateNav = false;
                if (!mLandscapeView) enableViews(true, "Driver Details");
                break;

            case Constants.TAG_FRAGMENT_CUSTOMER_ADD:
                mShowDateNav = false;
                enableViews(true, "Add New Customer");
                break;

            case Constants.TAG_FRAGMENT_ORDER_ADD:
                mShowDateNav = false;
                enableViews(true, "Add New Order");
                break;
        }

        // Repaint the menu
        invalidateOptionsMenu();
    }

    @Override
    public void onCustomerSaved(Customer customer) {

        mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Fragment_CustomerDetails fragment = Fragment_CustomerDetails.newInstance(customer);


        if (mLandscapeView) {
            replaceFragment(R.id.container_details, Constants.TAG_FRAGMENT_CUSTOMER_DETAILS, fragment, true);
        } else {
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_DETAILS, fragment, true);
        }

    }

    @Override
    public void onLocationAdded(Customer customer) {
        //Log.i(Constants.LOG_TAG, "onCustomerSelected called CustKey:" + customer);

        mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Fragment_CustomerDetails fragment = Fragment_CustomerDetails.newInstance(customer);

        if (mLandscapeView) {
            replaceFragment(R.id.container_details, Constants.TAG_FRAGMENT_CUSTOMER_DETAILS, fragment, true);
        } else {
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_DETAILS, fragment, true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(Constants.LOG_TAG, "onSaveInstanceState() called in Activity_Main");

        outState.putLong(Constants.STATE_INFO_DATE_FILTER, mDisplayDateStartTimeInMillis);


        Fragment frag_details = mFragmentManager.findFragmentById(R.id.container_details);

        if (frag_details != null) {
            String tag_details = frag_details.getTag();
            Log.i(Constants.LOG_TAG, "fragment tag stored for details" + tag_details);
            outState.putString(Constants.STATE_INFO_FRAGMENT_DETAILS, tag_details);
        }

        Fragment frag_master = mFragmentManager.findFragmentById(R.id.container_master);

        if (frag_master != null) {
            String tag_details = frag_master.getTag();
            Log.i(Constants.LOG_TAG, "fragment tag stored for details" + tag_details);
            outState.putString(Constants.STATE_INFO_FRAGMENT_DETAILS, tag_details);
        }
    }

    // Date picker listener
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            final Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, selectedYear);
            c.set(Calendar.MONTH, selectedMonth);
            c.set(Calendar.DAY_OF_MONTH, selectedDay);

            c.set(Calendar.HOUR, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);


            mDisplayDateStartTimeInMillis = Utils_General.getStartTimeForDate(c.getTimeInMillis());

            Log.i(Constants.LOG_TAG, Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis));

            //remove details frag if one is loaded
            if (mLandscapeView) {
                clearDetailsContainer();
            }

            if (mFragmentManager.findFragmentById(R.id.container_master) != null) {

                // Get the fragment in the master container

                Fragment frag = mFragmentManager.findFragmentById(R.id.container_master);

                if(frag instanceof Fragment_MainDetails){
                    Fragment_MainDetails fragment = Fragment_MainDetails.newInstance(mDisplayDateStartTimeInMillis);
                    replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN_DETAILS, fragment, true);
                }else if(frag instanceof Fragment_OrderList){
                    Fragment_OrderList frag_order_list = Fragment_OrderList.newInstance(mDisplayDateStartTimeInMillis);
                    replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_LIST, frag_order_list, false);
                }
            }

            //replaceMasterListFragment();

        }
    };
}
