package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.driver.views.fragments.Fragment_OrderDetails;
import com.programming.kantech.deliveryservice.app.driver.views.fragments.Fragment_OrderList;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-10-11.
 */

public class Activity_ShowOrders extends AppCompatActivity implements Fragment_OrderList.OrderClickListener {

    private ActionBar mActionBar;
    private Driver mDriver;
    private Order mSelectedOrder;




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

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle("Works");
        }

        // Determine if you're creating a two-pane or single-pane display
        mLandscapeView = (findViewById(R.id.layout_for_two_cols) != null);
        Log.i(Constants.LOG_TAG, "we have 2 cols:" + mLandscapeView);

        // This is the first time the activity has been loaded
        if (savedInstanceState == null && mIsFirstTimeLoaded) {
            // add main fragment to master container on first start
            mIsFirstTimeLoaded = false;
            addMasterListFragment();

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

    public void addMasterListFragment() {

        Log.i(Constants.LOG_TAG, "AddMasterListFragment called()");

        Fragment_OrderList frag = Fragment_OrderList.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.container_master, frag, Constants.TAG_FRAGMENT_ORDER_LIST);
        //transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

    }

    @Override
    public void onOrderClicked(Order order) {
        Log.i(Constants.LOG_TAG, "On order clicked in Fragment and returned to activity");

        mSelectedOrder = order;

        replaceDetailsFragment();

    }

    public void replaceDetailsFragment() {

        Log.i(Constants.LOG_TAG, "replaceDetailsFragment called()");

        Fragment_OrderDetails frag = Fragment_OrderDetails.newInstance(mSelectedOrder);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (mLandscapeView) {
            transaction.replace(R.id.container_details, frag, Constants.TAG_FRAGMENT_ORDER_DETAILS);
        } else {
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.container_master, frag, Constants.TAG_FRAGMENT_ORDER_DETAILS);
            //transaction.addToBackStack(null);
        }

        // Commit the transaction
        transaction.commit();
    }


}
