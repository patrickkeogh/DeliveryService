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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Order;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-08-27.
 * Uses FirebaseDatabase-UI, ButterKnife
 */

public class Fragment_OrderList extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Local member variables
    private FirebaseRecyclerAdapter<Order, ViewHolder_Order> mFireAdapter;
    private RecyclerView.AdapterDataObserver mObserver;
    private GoogleApiClient mClient;
    private Order mSelectedOrder;

    private Context mContext;

    // 12:00 AM of the selected day
    private long mDisplayDateStartTimeInMillis;


    // Member variables for Firebase
    private DatabaseReference mOrdersRef;

    // Views
    @BindView(R.id.rv_orders_list)
    RecyclerView rv_orders_list;

    @BindView(R.id.tv_empty_view)
    TextView tv_empty_view;

    // Define a new interface StepNavClickListener that triggers a callback in the host activity
    OrderListFragmentListener mCallback;

    // OrderListFragmentListener interface,
    // calls a methods in the host activity
    public interface OrderListFragmentListener {

        // Notify activity when an order is selected
        void onOrderClicked(Order order);

        // Notify the activity to load the new order form
        void onAddOrderClicked();

        // Notify the activity that this fragment has be loaded,
        // may be from activity, orientation change, or back button
        void onFragmentLoaded(String tag);
    }

    // Mandatory empty constructor
    public Fragment_OrderList() {

    }

    /**
     * Static factory method that takes a datetime object parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_OrderList newInstance(long date_in) {
        Fragment_OrderList f = new Fragment_OrderList();
        Bundle args = new Bundle();

        // Add any required arguments for start up - None needed right now
        args.putLong(Constants.EXTRA_DATE_FILTER, date_in);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_DATE_FILTER)) {
                mDisplayDateStartTimeInMillis = savedInstanceState.getLong(Constants.STATE_INFO_DATE_FILTER);
            }
        } else {
            mDisplayDateStartTimeInMillis = getArguments().getLong(Constants.EXTRA_DATE_FILTER);
        }

        if (mDisplayDateStartTimeInMillis == 0) {
            throw new IllegalArgumentException("Must pass EXTRA_DATE_FILTER");
        }

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_order_list, container, false);

        ButterKnife.bind(this, rootView);

        // Get a reference to the orders table
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        return rootView;

    }

    /**
     * Save the current state of this fragment
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the driver in the instance state
        outState.putLong(Constants.STATE_INFO_DATE_FILTER, mDisplayDateStartTimeInMillis);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup the recycler view
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        rv_orders_list.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView.  DO NOT CHANGE IF USING
         * FIREBASE ADAPTOR
         */
        rv_orders_list.setHasFixedSize(false);


    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_OrderList.OrderListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OrderListFragmentListener");
        }
    }

    @OnClick(R.id.fab_add_order)
    public void onFabAddOrderClicked() {
        mCallback.onAddOrderClicked();
    }

    @Override
    public void onStart() {
        super.onStart();
        mCallback.onFragmentLoaded(Constants.TAG_FRAGMENT_ORDER_LIST);

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
            if (Utils_General.isNetworkAvailable(mContext)) {

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

    private Query getDatabaseQuery() {

        return mOrdersRef.orderByChild(Constants.FIREBASE_CHILD_PICKUP_DATE).equalTo(mDisplayDateStartTimeInMillis);
    }

    private void loadFirebaseAdapter() {
        //Log.i(Constants.LOG_TAG, "loadFirebaseAdapter");

        mFireAdapter = new FirebaseRecyclerAdapter<Order, ViewHolder_Order>(
                Order.class,
                R.layout.item_admin_order,
                ViewHolder_Order.class,
                getDatabaseQuery()) {

            @Override
            public void populateViewHolder(final ViewHolder_Order holder, final Order order, int position) {

                // change the background color of the list item depending on the status
                switch (order.getStatus()) {
                    case Constants.ORDER_STATUS_BOOKED:
                        holder.setBackgroundColor(getContext(), R.color.colorPrimary);
                        break;
                    case Constants.ORDER_STATUS_ASSIGNED:
                    case Constants.ORDER_STATUS_PICKUP_COMPLETE:
                        holder.setBackgroundColor(getContext(), R.color.colorGreen);
                        break;
                    case Constants.ORDER_STATUS_COMPLETE:
                        holder.setBackgroundColor(getContext(), R.color.colorAccent);
                        break;

                }

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

        rv_orders_list.setAdapter(mFireAdapter);

        Query countRef = getDatabaseQuery();

        // Hide or show the list depending on if there are records
        countRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //remove loading indicator

                // Perform initial setup, this will only be called once
                if (dataSnapshot.hasChildren()) {
                    Log.i(Constants.LOG_TAG, "we have records");
                    showList(true);
                } else {
                    showList(false);
                }

                // Create an observer to check if the list changes
                mObserver = new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {

                        int count = mFireAdapter.getItemCount();
                        if (count == 0) {
                            showList(false);
                        } else {
                            showList(true);
                        }
                    }

                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        int count = mFireAdapter.getItemCount();
                        if (count == 0) {
                            showList(false);
                        } else {
                            showList(true);
                        }
                    }
                };
                mFireAdapter.registerAdapterDataObserver(mObserver);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showList(boolean bShowList) {

        if (bShowList) {
            rv_orders_list.setVisibility(View.VISIBLE);
            tv_empty_view.setVisibility(View.GONE);
        } else {
            rv_orders_list.setVisibility(View.GONE);
            tv_empty_view.setVisibility(View.VISIBLE);
        }
    }

    private void orderSelected(Order order) {
        mSelectedOrder = order;
        mFireAdapter.notifyDataSetChanged();

    }
}
