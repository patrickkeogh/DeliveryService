package com.programming.kantech.deliveryservice.app.driver.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.driver.views.ui.ViewHolder_OrderHistory;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-10-11.
 * A fragment to show a list of orders
 */

public class Fragment_OrderList extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Local member variables
    private FirebaseRecyclerAdapter<Order, ViewHolder_OrderHistory> mFireAdapter;
    private GoogleApiClient mClient;
    private RecyclerView.AdapterDataObserver mObserver;
    private Order mSelectedOrder;
    private Driver mDriver;

    @BindView(R.id.rv_orders_list)
    RecyclerView rv_orders_list;

    @BindView(R.id.tv_empty_view)
    TextView tv_empty_view;

    @BindView(R.id.tv_orders_filter_date)
    TextView tv_orders_filter_date;

    // 12:00 AM of the selected day
    private long mDisplayDateStartTimeInMillis;

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
    public static Fragment_OrderList newInstance(Driver driver, long date_in) {

        Fragment_OrderList f = new Fragment_OrderList();
        Bundle args = new Bundle();
        args.putParcelable(Constants.EXTRA_DRIVER, driver);
        args.putLong(Constants.EXTRA_DATE_FILTER, date_in);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_order_list, container, false);

        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_DRIVER)) {
                mDriver = savedInstanceState.getParcelable(Constants.STATE_INFO_DRIVER);
            }
            if (savedInstanceState.containsKey(Constants.STATE_INFO_DATE_FILTER)) {
                mDisplayDateStartTimeInMillis = savedInstanceState.getLong(Constants.STATE_INFO_DATE_FILTER);
            }
        } else {

            mDriver = getArguments().getParcelable(Constants.EXTRA_DRIVER);
            mDisplayDateStartTimeInMillis = getArguments().getLong(Constants.EXTRA_DATE_FILTER);
        }

        if (mDriver == null || mDisplayDateStartTimeInMillis == 0) {
            throw new IllegalArgumentException("Must pass EXTRA_DRIVER AND EXTRA_DATE_FILTER");
        }else{

            String showDate = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);
            tv_orders_filter_date.setText(showDate);


        }

        // Get a reference to the orders table
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        return rootView;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.STATE_INFO_DRIVER, mDriver);
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
        //Log.i(Constants.LOG_TAG, "onConnected");


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

    private Query getDatabaseQuery() {
        //Log.i(Constants.LOG_TAG, "CustId:" + mAppUser.getId());

        String strQueryStart = mDisplayDateStartTimeInMillis + "_" + mDriver.getUid() + Constants.FIREBASE_STATUS_SORT_PICKUP_COMPLETE;
        String strQueryEnd = mDisplayDateStartTimeInMillis + "_" + mDriver.getUid() + Constants.FIREBASE_STATUS_SORT_COMPLETE;
        return mOrdersRef.orderByChild("queryDateDriverId").startAt(strQueryStart).endAt(strQueryEnd);

    }

    private void loadFirebaseAdapter() {
        //Log.i(Constants.LOG_TAG, "loadFirebaseAdapter");

        mFireAdapter = new FirebaseRecyclerAdapter<Order, ViewHolder_OrderHistory>(
                Order.class,
                R.layout.item_order_history,
                ViewHolder_OrderHistory.class,
                getDatabaseQuery()) {

            @Override
            public void populateViewHolder(final ViewHolder_OrderHistory holder, final Order order, int position) {
                //Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + order.getCustomerName());

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

                // Highlight the selected item
                if (mSelectedOrder != null) {
                    if (Objects.equals(order.getId(), mSelectedOrder.getId())) {
                        holder.setSelectedColor(getContext(), R.color.colorPrimaryLight);
                    } else {
                        holder.setSelectedColor(getContext(), R.color.colorWhite);
                    }
                }

                holder.setCustomerName(order.getCustomerName());

                //holder.setCustomerName(order.getCustomerName());
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
                    //Log.i(Constants.LOG_TAG, "we have records");
                    showList(true);
                } else {
                    showList(false);
                }

                // Create an observer to check if the list changes in the future
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
        //Log.i(Constants.LOG_TAG, "orderSelected()");

        mSelectedOrder = order;
        mFireAdapter.notifyDataSetChanged();

    }
}
