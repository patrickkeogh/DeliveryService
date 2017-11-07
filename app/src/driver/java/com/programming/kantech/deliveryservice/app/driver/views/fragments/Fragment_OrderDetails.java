package com.programming.kantech.deliveryservice.app.driver.views.fragments;

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

/**
 * Created by patri on 2017-10-11.
 */

public class Fragment_OrderDetails extends Fragment  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Order mSelectedOrder;
    private GoogleApiClient mClient;

    private TextView tv_order_details_company;
    private TextView tv_order_details_number;
    private TextView tv_order_details_location_pickup;
    private TextView tv_order_details_location_delivery;
    private TextView tv_order_details_status;
    private TextView tv_order_details_date;
    private TextView tv_order_details_distance;




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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_order_details, container, false);

        Bundle args = getArguments();
        mSelectedOrder = args.getParcelable(Constants.EXTRA_ORDER);

        Log.i(Constants.LOG_TAG, "Order in Frag:" + mSelectedOrder.toString());

        tv_order_details_company = rootView.findViewById(R.id.tv_order_details_company);
        tv_order_details_number = rootView.findViewById(R.id.tv_order_details_number);
        tv_order_details_location_pickup = rootView.findViewById(R.id.tv_order_details_location_pickup);
        tv_order_details_location_delivery = rootView.findViewById(R.id.tv_order_details_location_delivery);

        tv_order_details_status = rootView.findViewById(R.id.tv_order_details_status);
//        tv_order_type = rootView.findViewById(R.id.tv_order_type);
        tv_order_details_date = rootView.findViewById(R.id.tv_order_details_date);
        tv_order_details_distance = rootView.findViewById(R.id.tv_order_details_distance);

        if (mSelectedOrder == null) {
            throw new IllegalArgumentException("Must pass a Order Object");
        } else {
            tv_order_details_company.setText(mSelectedOrder.getCustomerName());
            tv_order_details_number.setText(mSelectedOrder.getId());
            tv_order_details_status.setText(mSelectedOrder.getStatus());
//            tv_order_type.setText(mOrder.getType());
            tv_order_details_distance.setText(mSelectedOrder.getDistance_text());
            tv_order_details_date.setText(Utils_General.getFormattedLongDateStringFromLongDate(mSelectedOrder.getPickupDate()));
        }

        return rootView;

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

    private void displayData() {

        tv_order_details_company.setText(mSelectedOrder.getCustomerName());
        tv_order_details_number.setText(mSelectedOrder.getId());
        tv_order_details_location_pickup.setText(mSelectedOrder.getId());

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
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }
}
