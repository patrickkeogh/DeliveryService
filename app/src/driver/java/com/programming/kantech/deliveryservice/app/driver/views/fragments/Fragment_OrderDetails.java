package com.programming.kantech.deliveryservice.app.driver.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-10-11.
 * A fragment to show order details
 */

public class Fragment_OrderDetails extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Order mSelectedOrder;
    private GoogleApiClient mClient;
    private Context mContext;

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


    // Mandatory empty constructor
    public Fragment_OrderDetails() {

    }

    /**
     * Static factory method that takes an order parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_OrderDetails newInstance(Order order) {
        Fragment_OrderDetails f = new Fragment_OrderDetails();
        Bundle args = new Bundle();
        args.putParcelable(Constants.EXTRA_ORDER, order);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_order_details, container, false);

        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_ORDER)) {
                mSelectedOrder = savedInstanceState.getParcelable(Constants.STATE_INFO_ORDER);
            }
        } else {

            mSelectedOrder = getArguments().getParcelable(Constants.EXTRA_ORDER);
        }

        if (mSelectedOrder == null) {
            throw new IllegalArgumentException("Must pass EXTRA_ORDER");
        } else {

            tv_order_details_company.setText(mSelectedOrder.getCustomerName());
            tv_order_details_date.setText(Utils_General.getFormattedLongDateStringFromLongDate(mSelectedOrder.getPickupDate()));
            tv_order_details_status.setText(mSelectedOrder.getStatus());

            int scale = 100;
            BigDecimal num1 = new BigDecimal(mSelectedOrder.getDistance());
            BigDecimal num2 = new BigDecimal(1000);
            Log.i(Constants.LOG_TAG, "new method" + num1.divide(num2, scale, RoundingMode.DOWN).toString());

            DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
            Double km = Double.valueOf(df.format(mSelectedOrder.getDistance() / 1000));

            //Log.i(Constants.LOG_TAG, "km:" + km);

            String distance_text = km + " km " + " @ $2.13/km";

            String display_amount = Utils_General.getCostString(mSelectedOrder.getAmount());

            display_amount += " (" + distance_text + ")";


            tv_order_details_value.setText(display_amount);
        }

        return rootView;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.STATE_INFO_ORDER, mSelectedOrder);
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mContext = context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OrderClickListener");
        }
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
    public void onDetach() {
        super.onDetach();
        mClient = null;
    }

    private void buildApiClient() {
        if (mClient == null) {
            if (Utils_General.isNetworkAvailable(mContext)) {
                // Build up the LocationServices API client
                // Uses the addApi method to request the LocationServices API
                // Also uses enableAutoManage to automatically know when to connect/suspend the client
                mClient = new GoogleApiClient.Builder(mContext)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Places.GEO_DATA_API)
                        .build();
            } else {
                Utils_General.showToast(mContext, getString(R.string.msg_no_network));
            }
        }
    }
}
