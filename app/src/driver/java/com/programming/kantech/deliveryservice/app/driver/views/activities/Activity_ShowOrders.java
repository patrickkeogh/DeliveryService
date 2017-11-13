package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.driver.views.fragments.Fragment_OrderDetails;
import com.programming.kantech.deliveryservice.app.driver.views.fragments.Fragment_OrderList;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-10-11.
 */

public class Activity_ShowOrders extends AppCompatActivity implements
        Fragment_OrderList.OrderClickListener {

    private ActionBar mActionBar;
    private Driver mDriver;
    private Order mSelectedOrder;
    private FragmentManager mFragmentManager;

    private static final String BACKSTACK_NAME= "detailFragment";

    // 12:00 AM of the selected day
    private long mDisplayDateStartTimeInMillis;

    // Track whether to display a two-column or single-column UI
    private boolean mLandscapeView;
    private boolean mIsFirstTimeLoaded = true;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Log.i(Constants.LOG_TAG, "onCreate() in Admin Activity_Main");

        if (savedInstanceState != null) {

            Log.i(Constants.LOG_TAG, "Activity_Details savedInstanceState is not null");
            if (savedInstanceState.containsKey(Constants.STATE_INFO_DRIVER)) {
                Log.i(Constants.LOG_TAG, "we found the driver key in savedInstanceState");
                mDriver = savedInstanceState.getParcelable(Constants.STATE_INFO_DRIVER);
            }

        } else {
            Log.i(Constants.LOG_TAG, "Activity_Details savedInstanceState is null, get data from intent");
            mDriver = getIntent().getParcelableExtra(Constants.EXTRA_DRIVER);
        }

        if (mDriver == null) {
            throw new IllegalArgumentException("Must pass EXTRA_DRIVER");
        }

        ButterKnife.bind(this);

        // Set the support action bar
        setSupportActionBar(mToolbar);

        mFragmentManager = getSupportFragmentManager();

        // Create the display date, today at 00:00 hrs
        Calendar date = new GregorianCalendar();
        mDisplayDateStartTimeInMillis = Utils_General.getStartTimeForDate(date.getTimeInMillis());

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("My Orders");
        }

        // Determine if you're creating a two-pane or single-pane display
        mLandscapeView = (findViewById(R.id.layout_for_two_cols) != null);
        Log.i(Constants.LOG_TAG, "we have 2 cols:" + mLandscapeView);

        // This is the first time the activity has been loaded
        if (savedInstanceState == null && mIsFirstTimeLoaded) {
            // add main fragment to master container on first start
            mIsFirstTimeLoaded = false;
            replaceMasterListFragment();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_orders, menu);

        return true;
    }

    private void clearDetailsContainer() {

        if (mFragmentManager.findFragmentById(R.id.container_details) != null) {

            mFragmentManager.beginTransaction()
                    .remove(mFragmentManager.findFragmentById(R.id.container_details)).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_driver_orders_previous:

                mDisplayDateStartTimeInMillis -= Constants.DAY;

                if (mLandscapeView) {
                    clearDetailsContainer();
                }

                replaceMasterListFragment();

                return true;

            case R.id.action_driver_orders_next:

                mDisplayDateStartTimeInMillis += Constants.DAY;

                if (mLandscapeView) {
                    clearDetailsContainer();
                }

                replaceMasterListFragment();


                return true;

            case R.id.action_driver_orders_select_date:

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
                //Utils_General.showToast(this, "Signout called");
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(Activity_ShowOrders.this, Activity_Splash.class));
                                finish();
                            }
                        });

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(Constants.LOG_TAG, "onSaveInstanceState() called in Activity_ShowOrders");
        outState.putParcelable(Constants.STATE_INFO_DRIVER, mDriver);

        //Save the fragment's instance
        //getSupportFragmentManager().putFragment(outState, "myFragmentName", mContent);
    }

    public void replaceMasterListFragment() {

        Log.i(Constants.LOG_TAG, "AddMasterListFragment called():" + mDisplayDateStartTimeInMillis);

        Fragment_OrderList frag = Fragment_OrderList.newInstance(mDriver, mDisplayDateStartTimeInMillis);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.container_master, frag, Constants.TAG_FRAGMENT_ORDER_LIST);

        if (mActionBar != null) {
            mActionBar.setTitle("My Orders");
        }

        // Commit the transaction
        transaction.commit();

    }

    @Override
    public void onOrderClicked(Order order) {
        Log.i(Constants.LOG_TAG, "On order clicked in Fragment and returned to activity");

        mSelectedOrder = order;

        //replaceDetailsFragment();

        if (mLandscapeView) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            Fragment_OrderDetails frag_details = Fragment_OrderDetails.newInstance(mSelectedOrder);

            mFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left,
                            R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.container_details, frag_details)
                    .addToBackStack(BACKSTACK_NAME)
                    .commit();

            //getActionBar().setDisplayHomeAsUpEnabled(true);

        } else {

            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent intent_details = new Intent(this, Activity_OrderDetails.class);
            intent_details.putExtra(Constants.EXTRA_ORDER, mSelectedOrder);
            startActivity(intent_details);
        }

    }

//    public void replaceDetailsFragment() {
//
//        Log.i(Constants.LOG_TAG, "replaceDetailsFragment called()");
//
//        Fragment_OrderDetails frag = Fragment_OrderDetails.newInstance(mSelectedOrder);
//
//        if(!mLandscapeView) mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//
//        FragmentTransaction transaction = mFragmentManager.beginTransaction();
//
//        if (mLandscapeView) {
//            transaction.replace(R.id.container_details, frag, Constants.TAG_FRAGMENT_ORDER_DETAILS);
//            transaction.setCustomAnimations(R.anim.slide_in_left,
//                    R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right);
//
//            transaction.replace(R.id.container_details, frag, Constants.TAG_FRAGMENT_ORDER_DETAILS);
//        } else {
//            if (mActionBar != null) {
//                mActionBar.setTitle("Order Details");
//            }
//            // Replace whatever is in the fragment_container view with this fragment,
//            // and add the transaction to the back stack so the user can navigate back
//            transaction.replace(R.id.container_master, frag, Constants.TAG_FRAGMENT_ORDER_DETAILS);
//            transaction.addToBackStack(null);
//        }
//
//        // Commit the transaction
//        transaction.commit();
//    }

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

            replaceMasterListFragment();

        }
    };

//    @Override
//    public void onBackPressed() {
//        if (mFragmentManager.getBackStackEntryCount() == 0) {
//            moveTaskToBack(true);
//        }
//        mFragmentManager.popBackStack(BACKSTACK_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        if (mActionBar != null) {
//            mActionBar.setDisplayHomeAsUpEnabled(false);
//            mActionBar.setHomeButtonEnabled(false);
//        }
//    }




}
