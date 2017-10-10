package com.programming.kantech.deliveryservice.app.admin.views.fragments;

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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

/**
 * Created by patrick keogh on 2017-08-14.
 *
 */

public class Fragment_OrderDetails extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private Order mOrder;
    private GoogleApiClient mClient;
    private DatabaseReference mLocationsRef;

    private TextView tv_order_company;
    private TextView tv_order_number;
    private TextView tv_order_location_pickup;
    private TextView tv_order_location_delivery;
    private TextView tv_order_status;
    private TextView tv_order_date;
    private TextView tv_order_type;
    private TextView tv_order_distance;

    /**
     * Static factory method that takes a driver object parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_OrderDetails newInstance(Order order) {
        Fragment_OrderDetails f = new Fragment_OrderDetails();
        Bundle args = new Bundle();

        // Add any required arguments for start up - None needed right now
        args.putParcelable(Constants.EXTRA_ORDER, order);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Load the saved state if there is one
        if (savedInstanceState != null) {
            Log.i(Constants.LOG_TAG, "Fragment_Step savedInstanceState is not null");
            if (savedInstanceState.containsKey(Constants.STATE_INFO_ORDER)) {
                Log.i(Constants.LOG_TAG, "we found the step key in savedInstanceState");
                mOrder = savedInstanceState.getParcelable(Constants.STATE_INFO_ORDER);
            }
        } else {
            Bundle args = getArguments();
            mOrder = args.getParcelable(Constants.EXTRA_ORDER);
        }

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_order_details, container, false);

        tv_order_company = rootView.findViewById(R.id.tv_order_company);
        tv_order_number = rootView.findViewById(R.id.tv_order_number);
        tv_order_location_pickup = rootView.findViewById(R.id.tv_order_location_pickup);
        tv_order_location_delivery = rootView.findViewById(R.id.tv_order_location_delivery);
        tv_order_status = rootView.findViewById(R.id.tv_order_status);
        tv_order_type = rootView.findViewById(R.id.tv_order_type);
        tv_order_date = rootView.findViewById(R.id.tv_order_date);
        tv_order_distance = rootView.findViewById(R.id.tv_order_distance);

        if (mOrder == null) {
            throw new IllegalArgumentException("Must pass a Order Object");
        } else {
            tv_order_company.setText(mOrder.getCustomerName());
            tv_order_number.setText(mOrder.getId());
            tv_order_status.setText(mOrder.getStatus());
            tv_order_type.setText(mOrder.getType());
            tv_order_distance.setText(mOrder.getDistance_text());
            tv_order_date.setText(Utils_General.getFormattedLongDateStringFromLongDate(mOrder.getPickupDate()));
        }

        // Get a reference to the drivers table
        mLocationsRef = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_ORDERS).child(mOrder.getId());

        return rootView;

    }



    /**
     * Save the current state of this fragment
     */
    @Override
    public void onSaveInstanceState(Bundle currentState) {
        currentState.putParcelable(Constants.STATE_INFO_ORDER, mOrder);
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

    @Override
    public void onStart() {
        super.onStart();
        if(mClient == null){
            buildApiClient();
            mClient.connect();
        }else{
            if(!mClient.isConnected() ){
                mClient.connect();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mClient != null){
            mClient.disconnect();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClient = null;
    }

    private void buildApiClient() {
        Log.i(Constants.LOG_TAG, "buildApiClient() called");

        if(mClient == null){
            Log.i(Constants.LOG_TAG, "CREATE NEW GOOGLE CLIENT");

            // Build up the LocationServices API client
            // Uses the addApi method to request the LocationServices API
            // Also uses enableAutoManage to automatically know when to connect/suspend the client
            mClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }
}
