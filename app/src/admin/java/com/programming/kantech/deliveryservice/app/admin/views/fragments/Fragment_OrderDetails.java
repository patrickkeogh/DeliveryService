package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.programming.kantech.deliveryservice.app.admin.views.activities.Activity_SelectDriver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by patrick keogh on 2017-08-14.
 *
 *
 */

public class Fragment_OrderDetails extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private Order mOrder;
    private Driver mSelectedDriver;
    private GoogleApiClient mClient;

    // Firebase references
    private DatabaseReference mOrdersRef;

    @BindView(R.id.tv_order_details_company)
    TextView tv_order_details_company;

    @BindView(R.id.tv_order_details_contact)
    TextView tv_order_details_contact;

    @BindView(R.id.tv_order_details_phone)
    TextView tv_order_details_phone;

    @BindView(R.id.tv_order_number)
    TextView tv_order_number;

    @BindView(R.id.tv_order_location_pickup)
    TextView tv_order_location_pickup;

    @BindView(R.id.tv_order_location_delivery)
    TextView tv_order_location_delivery;

    @BindView(R.id.tv_order_status)
    TextView tv_order_status;

    @BindView(R.id.tv_order_date)
    TextView tv_order_date;

    @BindView(R.id.tv_order_type)
    TextView tv_order_type;

    @BindView(R.id.tv_order_distance)
    TextView tv_order_distance;

    @BindView(R.id.tv_order_amount)
    TextView tv_order_amount;

    @BindView(R.id.tv_order_driver)
    TextView tv_order_driver;

    @BindView(R.id.fab_assign_driver)
    FloatingActionButton fab_assign_driver;

    // Define a new interface OrderDetailFragmentListener that triggers a callback in the host activity
    OrderDetailFragmentListener mCallback;

    // OrderDetailFragmentListener interface, calls a method in the host activity
    // depending on the order selected in the master list
    public interface OrderDetailFragmentListener {
        void onFragmentLoaded(String tag);
    }

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

        ButterKnife.bind(this, rootView);

        if (mOrder == null) {
            throw new IllegalArgumentException("Must pass a Order Object");
        } else {

            parseDriverFields();



        }

        // Get a reference to the orders table
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        return rootView;

    }

    private void parseDriverFields() {

        tv_order_details_company.setText(mOrder.getCustomerName());


        tv_order_details_contact.setText(mOrder.getCustomerContact());
        tv_order_details_phone.setText(mOrder.getCustomerPhone());


        tv_order_number.setText(mOrder.getId());
        tv_order_status.setText(mOrder.getStatus());
        tv_order_type.setText(mOrder.getType());
        tv_order_distance.setText(mOrder.getDistance_text());
        tv_order_amount.setText(Utils_General.getCostString(getContext(), mOrder.getAmount()));
        tv_order_date.setText(Utils_General.getFormattedLongDateStringFromLongDate(mOrder.getPickupDate()));

        tv_order_driver.setText(mOrder.getDriverName());

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

        // Notify the activity this fragment was loaded
        mCallback.onFragmentLoaded(Constants.TAG_FRAGMENT_ORDER_DETAILS);

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
            mCallback = (Fragment_OrderDetails.OrderDetailFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OrderDetailFragmentListener");
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

    @OnClick(R.id.fab_assign_driver)
    public void onAssignDriverClicked() {
        Intent intent = new Intent(getActivity(), Activity_SelectDriver.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_DRIVER);
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

                confirmAndSaveDriver();


                //setupForm();

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(getContext(), "Driver Not Selected");
                //finish();
            }
        }
    }

    private void confirmAndSaveDriver() {

        if(mOrder != null && mSelectedDriver !=  null) {

            final ViewGroup nullParent = null;

            LayoutInflater inflater = this.getLayoutInflater();
            View dialog_confirm = inflater.inflate(R.layout.alert_confirm_driver, nullParent);

            TextView tv_driver = dialog_confirm.findViewById(R.id.tv_confirm_order_driver);
            tv_driver.setText(mSelectedDriver.getDisplayName());

            new AlertDialog.Builder(getContext())
                    .setTitle("Assign the following driver?")
                    .setView(dialog_confirm)
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // Update the driver field and the status
                            mOrder.setStatus(Constants.ORDER_STATUS_ASSIGNED);
                            mOrder.setInProgress(true);
                            mOrder.setDriverId(mSelectedDriver.getUid());
                            mOrder.setDriverName(mSelectedDriver.getDisplayName());
                            mOrder.setInProgressDriverId("true_" + mSelectedDriver.getUid());

                            // Construct query string for inprogress_date_driver
                            String strQuery = "true_" +
                                    mOrder.getPickupDate()
                                    + "_" + mSelectedDriver.getUid();

                            mOrder.setInProgressDateDriverId(strQuery);

                            // Save the order to firebase
                            mOrdersRef.child(mOrder.getId()).setValue(mOrder);


                            fab_assign_driver.setEnabled(false);
                            parseDriverFields();

                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Utils_General.showToast(getContext(), getString(R.string.assign_driver_cancelled));

                        }
                    })
                    .show();


        }

    }
}
