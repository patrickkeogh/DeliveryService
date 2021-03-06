package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick on 2017-11-10.
 * An activity to show the details of a selected order
 */

public class Activity_OrderDetails extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Order mSelectedOrder;
    private GoogleApiClient mClient;

    // Bind the layout views
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tv_order_details_company)
    TextView tv_order_details_company;

    @BindView(R.id.tv_order_details_date)
    TextView tv_order_details_date;

    @BindView(R.id.tv_order_details_location_pickup)
    TextView tv_order_details_location_pickup;

    @BindView(R.id.tv_order_details_location_delivery)
    TextView tv_order_details_location_delivery;

    @BindView(R.id.tv_order_details_status)
    TextView tv_order_details_status;

    @BindView(R.id.tv_order_details_value)
    TextView tv_order_details_value;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        ButterKnife.bind(this);

        // Set the support action bar
        setSupportActionBar(mToolbar);
        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(R.string.title_order_details);
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_ORDER)) {
                mSelectedOrder = savedInstanceState.getParcelable(Constants.STATE_INFO_ORDER);
            }
        } else {

            mSelectedOrder = getIntent().getParcelableExtra(Constants.EXTRA_ORDER);
        }

        if (mSelectedOrder == null) {
            throw new IllegalArgumentException("Must pass EXTRA_ORDER");
        } else {

            tv_order_details_company.setText(mSelectedOrder.getCustomerName());
            tv_order_details_date.setText(Utils_General.getFormattedLongDateStringFromLongDate(mSelectedOrder.getPickupDate()));
            tv_order_details_status.setText(mSelectedOrder.getStatus());

            DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
            Double km = Double.valueOf(df.format(mSelectedOrder.getDistance() / 1000));

            String distance_text = km + " km " + " @ $2.13/km";

            String display_amount = Utils_General.getCostString(mSelectedOrder.getAmount());

            display_amount += " (" + distance_text + ")";


            tv_order_details_value.setText(display_amount);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.STATE_INFO_ORDER, mSelectedOrder);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Get the location info for the Pickup Address
        PendingResult<PlaceBuffer> pickupResult = Places.GeoDataApi.getPlaceById(mClient, mSelectedOrder.getPickupLocationId());

        pickupResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                tv_order_details_location_pickup.setText(places.get(0).getAddress().toString());
            }
        });

        // Get the location info for the Delivery Address
        PendingResult<PlaceBuffer> deliveryResult = Places.GeoDataApi.getPlaceById(mClient, mSelectedOrder.getDeliveryLocationId());

        deliveryResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                tv_order_details_location_delivery.setText(places.get(0).getAddress().toString());
            }
        });


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient = null;
    }

    private void buildApiClient() {


        if (mClient == null) {
            if(Utils_General.isNetworkAvailable(this)){

                // Build up the LocationServices API client
                // Uses the addApi method to request the LocationServices API
                // Also uses enableAutoManage to automatically know when to connect/suspend the client
                mClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Places.GEO_DATA_API)
                        .build();
            }else{
                Utils_General.showToast(this, getString(R.string.msg_no_network));
            }


        }
    }
}
