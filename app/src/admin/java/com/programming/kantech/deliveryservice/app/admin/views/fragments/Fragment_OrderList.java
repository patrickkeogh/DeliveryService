package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.programming.kantech.deliveryservice.app.admin.views.activities.Activity_SelectDriver;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Order;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by patrick keogh on 2017-08-27.
 *
 */

public class Fragment_OrderList extends Fragment  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private FirebaseRecyclerAdapter<Order, ViewHolder_Order> mFireAdapter;
    private RecyclerView mOrderList;
    private GoogleApiClient mClient;

    private Driver mSelectedDriver;
    private Order mSelectedOrder;
    private MenuItem mAssignedDriverMenuItem;

    //private String mStatus;

    // Firebase references
    private DatabaseReference mOrdersRef;

    // Define a new interface onCustomerSelected that triggers a callback in the host activity
    OrderClickListener mCallback;

    // onOrderSelected interface, calls a method in the host activity named onOrderSelected
    public interface OrderClickListener {
        void onOrderSelected(Order order);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_assign_driver:
                    Utils_General.showToast(getContext(), "Assign Driver");

                    // Open the Select Driver Activity and return a driver object
                    Intent intent = new Intent(getActivity(), Activity_SelectDriver.class);
                    startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_DRIVER);
                    return true;
            }
            return false;
        }

    };

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

        BottomNavigationView navigation = rootView.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mAssignedDriverMenuItem = navigation.getMenu().findItem(R.id.navigation_assign_driver);
        mAssignedDriverMenuItem.setEnabled(false);

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

        mFireAdapter = new FirebaseRecyclerAdapter<Order, ViewHolder_Order>(
                Order.class,
                R.layout.item_order,
                ViewHolder_Order.class,
                mOrdersRef) {

            @Override
            public void populateViewHolder(final ViewHolder_Order holder, final Order order, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + order.toString());

                //holder.itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));

                holder.setCustomerName(order.getCustomerName());
                holder.setOrderDate(Utils_General.getFormattedLongDateStringFromLongDate(order.getPickupDate()));
                holder.setOrderStatus(order.getStatus());

                //final Place[] pickupLocation = new Place[1];

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, order.getPickupLocationId());

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        holder.setPickupAddress(places.get(0).getAddress().toString());
                    }
                });

                // Get the key for each record
                //final DatabaseReference postRef = getRef(position);
                //final String postKey = postRef.getKey();

                // Set click listener for the whole post view
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectedOrder = order;

                        // Enable the Assign Driver bottom nav menu item
                        mAssignedDriverMenuItem.setEnabled(true);

                        // Notify the activity a driver was clicked
                        mCallback.onOrderSelected(order);
                    }
                });
            }

        };

        mOrderList.setAdapter(mFireAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_SELECT_DRIVER) {
            if (resultCode == RESULT_OK) {
                // Customer was successfully selected
                Utils_General.showToast(getContext(), "Driver Selected");

                // Get the customer from the intent data
                mSelectedDriver = data.getParcelableExtra(Constants.EXTRA_DRIVER);
                assignDriver();

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(getContext(), "Customer Not Selected");
                //finish();
            }
        }
    }

    private void assignDriver() {


        if(mSelectedOrder != null && mSelectedDriver !=  null){

            LayoutInflater inflater = this.getLayoutInflater();
            View dialog_confirm = inflater.inflate(R.layout.alert_confirm_driver, null);

            TextView tv_driver = dialog_confirm.findViewById(R.id.tv_confirm_order_driver);
            tv_driver.setText(mSelectedDriver.getDisplayName());


            new AlertDialog.Builder(getContext())
                    .setTitle("Please confirm a new order for:")
                    .setView(dialog_confirm)
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // Update the driver field and the status
                            mSelectedOrder.setStatus(Constants.ORDER_STATUS_ASSIGNED);
                            mSelectedOrder.setInProgress(true);
                            mSelectedOrder.setDriverId(mSelectedDriver.getUid());
                            mSelectedOrder.setInProgressDriverId("true_" + mSelectedDriver.getUid());

                            // Construct query string for inprogress_date_driver

                            String strQuery = "true_" +
                                    mSelectedOrder.getPickupDate()
                                    + "_" + mSelectedDriver.getUid();

                            mSelectedOrder.setInProgressDateDriverId(strQuery);

                            // Save the order to firebase
                            mOrdersRef.child(mSelectedOrder.getId()).setValue(mSelectedOrder);

                            mSelectedOrder = null;
                            mSelectedDriver = null;
                            mAssignedDriverMenuItem.setEnabled(false);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Utils_General.showToast(getContext(), getString(R.string.order_cancelled));

//                            mLocationDelivery = null;
//                            mLocationPickup = null;
//                            mSelectedCustomer = null;
//
//                            setupForm();

                        }
                    })
                    .show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Constants.LOG_TAG, "onConnected called");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
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
