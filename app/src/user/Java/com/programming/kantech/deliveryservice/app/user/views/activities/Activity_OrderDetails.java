package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-11-09.
 * An activity to show order details
 */

public class Activity_OrderDetails extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mClient;
    private Order mOrder;
    private AppUser mAppUser;

    // View to bind
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tv_order_date)
    TextView tv_order_date;

    @BindView(R.id.tv_order_amount)
    TextView tv_order_amount;

    @BindView(R.id.tv_order_status)
    TextView tv_order_status;

    @BindView(R.id.tv_order_driver)
    TextView tv_order_driver;

    @BindView(R.id.tv_order_location_pickup)
    TextView tv_order_location_pickup;

    @BindView(R.id.tv_order_location_delivery)
    TextView tv_order_location_delivery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_details);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_ORDER)) {
                mOrder = savedInstanceState.getParcelable(Constants.STATE_INFO_ORDER);
            }
            if (savedInstanceState.containsKey(Constants.STATE_INFO_USER)) {
                mAppUser = savedInstanceState.getParcelable(Constants.STATE_INFO_USER);
            }
        } else {
            mOrder = getIntent().getParcelableExtra(Constants.EXTRA_ORDER);
            mAppUser = getIntent().getParcelableExtra(Constants.EXTRA_USER);
        }


        if (mOrder == null || mAppUser == null) {
            throw new IllegalArgumentException("Must pass EXTRA_ORDER & EXTRA_USER");
        } else {
            parseOrderFields();
        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("Order Details");
        }

    }

    /**
     * Save the current state of this activity
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the user in the instance state
        outState.putParcelable(Constants.STATE_INFO_USER, mAppUser);
        // Store the order in the instance state
        outState.putParcelable(Constants.STATE_INFO_ORDER, mOrder);
    }

    private void parseOrderFields() {

//        tv_order_number.setText(mOrder.getId());
        tv_order_status.setText(mOrder.getStatus());
//        tv_order_type.setText(mOrder.getType());
//        tv_order_distance.setText(mOrder.getDistance_text());

        int scale = 100;
        BigDecimal num1 = new BigDecimal(mOrder.getDistance());
        BigDecimal num2 = new BigDecimal(1000);
        Log.i(Constants.LOG_TAG, "new method" + num1.divide(num2, scale, RoundingMode.DOWN).toString());

        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
        Double km = Double.valueOf(df.format(mOrder.getDistance() / 1000));

        //Double km = (double) (mOrder.getDistance() / 1000);
        Log.i(Constants.LOG_TAG, "km:" + km);

        String distance_text = km + " km " + " @ $2.13/km";

        String display_amount = Utils_General.getCostString(mOrder.getAmount());

        display_amount += " (" + distance_text + ")";


        tv_order_amount.setText(display_amount);
        tv_order_date.setText(Utils_General.getFormattedLongDateStringFromLongDate(mOrder.getPickupDate()));

        tv_order_driver.setText(mOrder.getDriverName());

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Get the location info for the Pickup Address
        PendingResult<PlaceBuffer> pickupResult = Places.GeoDataApi.getPlaceById(mClient, mOrder.getPickupLocationId());

        pickupResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                tv_order_location_pickup.setText(places.get(0).getAddress().toString());
            }
        });

        // Get the location info for the Delivery Address
        PendingResult<PlaceBuffer> deliveryResult = Places.GeoDataApi.getPlaceById(mClient, mOrder.getDeliveryLocationId());

        deliveryResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                tv_order_location_delivery.setText(places.get(0).getAddress().toString());
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void buildApiClient() {
        //Log.i(Constants.LOG_TAG, "buildApiClient() called");

        if (mClient == null) {
            //Log.i(Constants.LOG_TAG, "CREATE NEW GOOGLE CLIENT");

            // Build up the LocationServices API client
            // Uses the addApi method to request the LocationServices API
            // Also uses enableAutoManage to automatically know when to connect/suspend the client
            mClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mClient == null) {
            buildApiClient();
            mClient.connect();
        } else {
            if (!mClient.isConnected()) {
                mClient.connect();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mClient != null) {
            mClient.disconnect();
        }
    }


}
