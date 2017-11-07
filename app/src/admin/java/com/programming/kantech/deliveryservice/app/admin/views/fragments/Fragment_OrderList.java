package com.programming.kantech.deliveryservice.app.admin.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Order;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Objects;

/**
 * Created by patrick keogh on 2017-08-27.
 *
 */

public class Fragment_OrderList extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private FirebaseRecyclerAdapter<Order, ViewHolder_Order> mFireAdapter;
    private GoogleApiClient mClient;
    private RecyclerView mOrdersList;
    private Order mSelectedOrder;

    // Member variables for Firebase
    private DatabaseReference mOrdersRef;

    // Define a new interface StepNavClickListener that triggers a callback in the host activity
    OrderClickListener mCallback;

    // OrderClickListener interface, calls a method in the host activity
    // depending on the order selected in the master list
    public interface OrderClickListener {
        void onOrderClicked(Order order);
    }

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

        // Get a reference to the RecyclerView in the fragment_order_list xml layout file
        mOrdersList = rootView.findViewById(R.id.rv_orders_list);

        // Get a reference to the orders table
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        return rootView;

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup the recycler view
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mOrdersList.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView.  DO NOT CHANGE IF USING
         * FIREBASE ADAPTOR
         */
        mOrdersList.setHasFixedSize(false);
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_OrderList.OrderClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OrderClickListener");
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
    public void onStop() {
        super.onStop();

        if (mClient != null) {
            mClient.disconnect();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mClient = null;

        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        loadFirebaseAdapter();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void buildApiClient() {
        if (mClient == null) {
            //Log.i(Constants.LOG_TAG, "CREATE NEW GOOGLE CLIENT");

            mClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }

    private Query getDatabaseRef() {
        //Log.i(Constants.LOG_TAG, "CustId:" + mAppUser.getId());

        return mOrdersRef;


    }

    private void loadFirebaseAdapter() {
        Log.i(Constants.LOG_TAG, "loadFirebaseAdapter");

        mFireAdapter = new FirebaseRecyclerAdapter<Order, ViewHolder_Order>(
                Order.class,
                R.layout.item_admin_order,
                ViewHolder_Order.class,
                mOrdersRef) {

            @Override
            public void populateViewHolder(final ViewHolder_Order holder, final Order order, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + order.getCustomerName());

                //mLayout.setBackgroundColor(ContextCompat.getColor(Activity_MyOrders.this, R.color.colorAccent));

//                switch (order.getStatus()){
//                    case Constants.ORDER_STATUS_BOOKED:
//                        holder.setBackgroundColor(Activity_ShowOrders, R.color.colorPrimary);
//                        break;
//                    case Constants.ORDER_STATUS_ASSIGNED:
//                        holder.setBackgroundColor(Activity_MyOrders.this, R.color.colorAccent);
//                        break;
//
//              }

                if (mSelectedOrder != null) {
                    if (Objects.equals(order.getId(), mSelectedOrder.getId())) {
                        holder.setSelectedColor(getContext(), R.color.colorPrimaryLight);
                    } else {
                        holder.setSelectedColor(getContext(), R.color.colorWhite);
                    }
                }


                holder.setCustomerName(order.getCustomerName());
                holder.setOrderDate(Utils_General.getFormattedLongDateStringFromLongDate(order.getPickupDate()));
                holder.setOrderStatus(order.getStatus());

                final Place[] thisPlace = new Place[1];

                PendingResult<PlaceBuffer> result_pickup = Places.GeoDataApi.getPlaceById(mClient, order.getPickupLocationId());

                result_pickup.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        thisPlace[0] = places.get(0);
                        holder.setPickupAddress(thisPlace[0].getAddress().toString());
                    }
                });

                PendingResult<PlaceBuffer> result_delivery = Places.GeoDataApi.getPlaceById(mClient, order.getDeliveryLocationId());

                result_delivery.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        thisPlace[0] = places.get(0);
                        holder.setDeliveryAddress(thisPlace[0].getAddress().toString());
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Send the selected order back to the main activity
                        // and hightlight the selectws order in the list
                        mCallback.onOrderClicked(order);
                        orderSelected(order);
                    }
                });
            }
        };

        mOrdersList.setAdapter(mFireAdapter);
    }

    private void orderSelected(Order order) {
        Log.i(Constants.LOG_TAG, "orderSelected()");

        mSelectedOrder = order;
        mFireAdapter.notifyDataSetChanged();

    }
}
