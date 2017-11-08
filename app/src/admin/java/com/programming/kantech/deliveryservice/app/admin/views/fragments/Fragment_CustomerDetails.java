package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
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
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Locations;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Customer;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Location;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-08-14.
 *
 */

public class Fragment_CustomerDetails extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private Customer mCustomer;
    private FirebaseRecyclerAdapter<Location, ViewHolder_Locations> mFireAdapter;
    private GoogleApiClient mClient;
    private DatabaseReference mLocationsRef;

    @BindView(R.id.tv_customer_address)
    TextView tv_customer_address;

    @BindView(R.id.tv_customer_company)
    TextView tv_customer_company;

    @BindView(R.id.tv_customer_contact_name)
    TextView tv_customer_contact_name;

    @BindView(R.id.tv_customer_contact_phone)
    TextView tv_customer_contact_phone;

    @BindView(R.id.rv_customer_locations_list)
    RecyclerView rv_customer_locations_list;

    // Define a new interface onCustomerSelected that triggers a callback in the host activity
    LocationAddedListener mCallback;

    public interface LocationAddedListener {
        void onLocationAdded(Customer customer);
        void onFragmentLoaded(String tag);
    }

    /**
     * Static factory method that takes a driver object parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_CustomerDetails newInstance(Customer customer) {
        Fragment_CustomerDetails f = new Fragment_CustomerDetails();
        Bundle args = new Bundle();

        // Add any required arguments for start up - None needed right now
        args.putParcelable(Constants.EXTRA_CUSTOMER, customer);
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
            if (savedInstanceState.containsKey(Constants.STATE_INFO_CUSTOMER)) {
                Log.i(Constants.LOG_TAG, "we found the step key in savedInstanceState");
                mCustomer = savedInstanceState.getParcelable(Constants.STATE_INFO_CUSTOMER);
            }

        } else {

            Bundle args = getArguments();
            mCustomer = args.getParcelable(Constants.EXTRA_CUSTOMER);
            Log.i(Constants.LOG_TAG, "Fragment_Step savedInstanceState is null, get data from intent:" + mCustomer);
        }

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_customer_details, container, false);

        ButterKnife.bind(this, rootView);

        if (mCustomer == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CUSTOMER");
        } else {
            tv_customer_company.setText(mCustomer.getCompany());
            tv_customer_contact_name.setText(mCustomer.getContact_name());
            tv_customer_contact_phone.setText(mCustomer.getContact_number());
        }

        // Get a reference to the drivers table
        mLocationsRef = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_LOCATIONS).child(mCustomer.getId());

        return rootView;

    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_CustomerDetails.LocationAddedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement LocationAddedListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        rv_customer_locations_list.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        rv_customer_locations_list.setHasFixedSize(false);

    }



    /**
     * Save the current state of this fragment
     */
    @Override
    public void onSaveInstanceState(Bundle currentState) {
        currentState.putParcelable(Constants.STATE_INFO_CUSTOMER, mCustomer);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Get the location info for the Main Address
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, mCustomer.getPlaceId());

        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {

                //Log.i(Constants.LOG_TAG, "PLACE:" + places.get(0).getAddress().toString());

                tv_customer_address.setText(places.get(0).getAddress().toString());


            }
        });

        loadFirebaseAdapter();

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

        // Notify the activity this fragment was loaded
        mCallback.onFragmentLoaded(Constants.TAG_FRAGMENT_CUSTOMER_DETAILS);

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
        //Log.i(Constants.LOG_TAG, "buildApiClient() called");

        if(mClient == null){
            //Log.i(Constants.LOG_TAG, "CREATE NEW GOOGLE CLIENT");

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
    private void loadFirebaseAdapter() {

        if(mFireAdapter != null){
            mFireAdapter.cleanup();
        }

        // Create the customer locations list
        mFireAdapter = new FirebaseRecyclerAdapter<Location, ViewHolder_Locations>(
                Location.class,
                R.layout.item_location,
                ViewHolder_Locations.class,
                mLocationsRef) {

            @Override
            public void populateViewHolder(final ViewHolder_Locations holder, Location location, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + location.toString());

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, location.getPlaceId());

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        Log.i(Constants.LOG_TAG, "PLACE:" + places.get(0).getAddress().toString());

                        holder.setName(places.get(0).getName().toString());
                        holder.setAddress(places.get(0).getAddress().toString());


                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            }

        };

        rv_customer_locations_list.setAdapter(mFireAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.i(Constants.LOG_TAG, "onActivityResult in Fragment");
        if (requestCode == Constants.REQUEST_CODE_LOCATION_PICKER && resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(getContext(), data);

            if (place == null) {
                Log.i(Constants.LOG_TAG, "No place selected");
                return;
            }

            addLocation(place);
        }

    }

    public void onGetLocationButtonClicked() {
        //Log.i(Constants.LOG_TAG, "onGetLocationButtonClicked() called");
        try {

            // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
            // when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(getActivity());
            startActivityForResult(i, Constants.REQUEST_CODE_LOCATION_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            Log.e(Constants.LOG_TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(Constants.LOG_TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
        }
    }

    @OnClick(R.id.ib_customer_add_location)
    public void onAddCustLocationClicked() {
        // Get a new location using google places
        onGetLocationButtonClicked();
    }


    private void addLocation(Place place) {
        //Log.i(Constants.LOG_TAG, "addLocation() calledplaceid:" + place.getId());

        final Location location = new Location();
        location.setCustId(mCustomer.getId());
        location.setPlaceId(place.getId());location.setMainAddress(false);

        mLocationsRef
                .push()
                .setValue(location, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        String uniqueKey = databaseReference.getKey();

                        location.setId(uniqueKey);

                        mLocationsRef.child(uniqueKey).setValue(location, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                mCallback.onLocationAdded(mCustomer);
                                //mFireAdapter.notifyDataSetChanged();

                            }
                        });

                    }
                });

    }
}
