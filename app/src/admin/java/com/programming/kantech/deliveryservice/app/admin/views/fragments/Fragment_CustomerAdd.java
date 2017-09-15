package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Customer;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Location;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-18.
 *
 */

public class Fragment_CustomerAdd extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private EditText et_fullname;
    private EditText et_email;
    private EditText et_company;
    private TextView tv_address;

    private static final int PLACE_PICKER_REQUEST = 1;

    private DatabaseReference mCustomerRef;
    private DatabaseReference mLocationsRef;

    private Customer mCustomer;
    private Location mLocation;
    private Place mPlace;

    /**
     * Static factory method that takes a driver object parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_CustomerAdd newInstance() {
        return new Fragment_CustomerAdd();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_customer_add, container, false);

        et_fullname = rootView.findViewById(R.id.et_customer_add_fullname);
        et_email = rootView.findViewById(R.id.et_customer_add_email);
        et_company = rootView.findViewById(R.id.et_customer_add_company);

        tv_address = rootView.findViewById(R.id.et_customer_add_address);

        mCustomer = new Customer();
        mLocation = new Location();

        Button btn_select_location = rootView.findViewById(R.id.btn_select_location);

        btn_select_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGetLocationButtonClicked();
            }
        });

        Button btn_add_customer = rootView.findViewById(R.id.btn_customer_add);

        btn_add_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddCustomerClicked();
            }
        });

        mCustomerRef = FirebaseDatabase.getInstance().getReference().child("customers");
        mLocationsRef = FirebaseDatabase.getInstance().getReference().child("locations");

        return rootView;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(Constants.LOG_TAG, "onActivityResult in Frag,ment");
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(getContext(), data);
            if (place == null) {
                Log.i(Constants.LOG_TAG, "No place selected");
                return;
            }

            // Extract the place information from the API
//            String placeName = place.getName().toString();
//            String placeAddress = place.getAddress().toString();
//            String placeID = place.getId();
//
//            LatLng latlng = place.getLatLng();
//
//            Double lat = latlng.latitude;
//            Double lng = latlng.longitude;

            mPlace = place;

            tv_address.setText(place.getAddress().toString());
        }
    }



    /***
     * Called when the Google API Client is successfully connected
     *
     * @param connectionHint Bundle of data provided to clients by Google Play services
     */
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        //refreshPlacesData();
        Log.i(Constants.LOG_TAG, "API Client Connection Successful!");
    }

    /***
     * Called when the Google API Client is suspended
     *
     * @param cause cause The reason for the disconnection. Defined by constants CAUSE_*.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(Constants.LOG_TAG, "API Client Connection Suspended!");
    }

    /***
     * Called when the Google API Client failed to connect to Google Play Services
     *
     * @param result A ConnectionResult that can be used for resolving the error
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(Constants.LOG_TAG, "API Client Connection Failed!");
    }

    /***
     * Button Click event handler to handle clicking the "select Location" Button
     *
     */
    public void onGetLocationButtonClicked() {
        Log.i(Constants.LOG_TAG, "onGetLocationButtonClicked() called");
        try {

            // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
            // when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(getActivity());
            startActivityForResult(i, PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(Constants.LOG_TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
        }
    }

    /***
     * Button Click event handler to handle clicking the "Add Customer" Button
     *
     */
    public void onAddCustomerClicked() {
        Log.i(Constants.LOG_TAG, "onGetLocationButtonClicked() called");

        // Validate the custer info

        // Add the customer and display message
        mCustomer.setCompany(et_company.getText().toString());
        mCustomer.setEmail(et_email.getText().toString());
        mCustomer.setContact_name(et_fullname.getText().toString());
        mCustomer.setPlaceId(mPlace.getId());

        mLocation.setPlaceId(mPlace.getId());
        mLocation.setMainAddress(true);

//        LatLng latlng = mPlace.getLatLng();
//
//        Double lat = latlng.latitude;
//        Double lng = latlng.longitude;
//
//        mCustomer.setLat(lat);
//        mCustomer.setLng(lng);

        //mCustomer.setPlaceId(mPlace.getId());

        //mCustomerRef.push().setValue(mCustomer);
        //m/Customer.setUid(mCustomerRef.getKey());
        //Log.i(Constants.LOG_TAG, "key:" + mCustomerRef.child());
        //mCustomerRef.child(mGroupId).setValue(mCustomer);
        Log.i(Constants.LOG_TAG, "Customer:" + mCustomer.toString());

        mCustomerRef
                .push()
                .setValue(mCustomer, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        String uniqueKey = databaseReference.getKey();

                        mCustomer.setId(uniqueKey);

                        mCustomerRef.child(uniqueKey).setValue(mCustomer);

                        mLocation.setCustId(uniqueKey);



                        addLocation();

                        Log.i(Constants.LOG_TAG, "key:" + uniqueKey);
                    }
                });


    }

    private void addLocation() {

        mLocationsRef
                .child(mCustomer.getId())
                .push()
                .setValue(mLocation, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        String uniqueKey = databaseReference.getKey();

                        mLocation.setId(uniqueKey);

                        mLocationsRef.child(mCustomer.getId()).child(uniqueKey).setValue(mLocation);

                        Log.i(Constants.LOG_TAG, "key:" + uniqueKey);
                    }
                });

    }
}
