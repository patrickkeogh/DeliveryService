package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Driver;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Order;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patri on 2017-08-27.
 */

public class Fragment_OrderList extends Fragment  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private FirebaseRecyclerAdapter<Order, ViewHolder_Order> mFireAdapter;
    private RecyclerView mOrderList;
    private GoogleApiClient mClient;

    private String mStatus;

    // Firebase references
    private DatabaseReference mOrdersRef;

    // Mandatory empty constructor
    public Fragment_OrderList() {

    }

    /**
     * Static factory method that
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_OrderList newInstance() {
        return new Fragment_OrderList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_order_list, container, false);

        // Get a reference to the orders table
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        // Get a reference to the RecyclerView in the fragment_order_list xml layout file
        mOrderList = rootView.findViewById(R.id.rv_order_list);

        // Initially show only booked orders
        mStatus = Constants.ORDER_STATUS_BOOKED;

        // Build up the LocationServices API client
        // Uses the addApi method to request the LocationServices API
        // Also uses enableAutoManage to automatically know when to connect/suspend the client
        mClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();

        mClient.connect();

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mOrderList.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mOrderList.setHasFixedSize(false);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Constants.LOG_TAG, "onConnected called");

        setupAdapter();


    }

    private void setupAdapter() {
        mFireAdapter = new FirebaseRecyclerAdapter<Order, ViewHolder_Order>(
                Order.class,
                R.layout.item_order,
                ViewHolder_Order.class,
                mOrdersRef) {

            @Override
            public void populateViewHolder(final ViewHolder_Order holder, Order order, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + order.toString());

                holder.itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));

                holder.setCustomerName(order.getCustomerName());

                //final Place[] pickupLocation = new Place[1];

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, order.getPickupLocationId());

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        //pickupLocation[0] = places.get(0);

                        Log.i(Constants.LOG_TAG, "PLACE:" + places.get(0).getAddress().toString());

                        //holder.setPickupAddress(places.get(0).getAddress().toString());


                    }
                });

                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Notify the activity a driver was clicked
                        //mCallback.onDriverSelected(postKey);
                    }
                });
            }

        };

        mOrderList.setAdapter(mFireAdapter);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
