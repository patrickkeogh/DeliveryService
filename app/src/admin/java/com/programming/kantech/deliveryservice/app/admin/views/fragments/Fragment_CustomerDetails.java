package com.programming.kantech.deliveryservice.app.admin.views.fragments;

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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Locations;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Customer;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Location;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-14.
 *
 */

public class Fragment_CustomerDetails extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private Customer mCustomer;
    private FirebaseRecyclerAdapter<Location, ViewHolder_Locations> mFireAdapter;
    private RecyclerView mLocationRecyclerView;
    private GoogleApiClient mClient;
    private DatabaseReference mLocationsRef;

    private TextView tv_customer_address;

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

        TextView tv_customer_company = rootView.findViewById(R.id.tv_customer_company);
        tv_customer_address = rootView.findViewById(R.id.tv_customer_address);
        TextView tv_customer_contact_name = rootView.findViewById(R.id.tv_customer_contact_name);

        if (mCustomer == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        } else {
            tv_customer_company.setText(mCustomer.getCompany());
            tv_customer_contact_name.setText(mCustomer.getContact_name());
        }

        // Get a reference to the RecyclerView in the fragment_order_list xml layout file
        mLocationRecyclerView = rootView.findViewById(R.id.rv_customer_locations_list);

        // Get a reference to the drivers table
        mLocationsRef = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_LOCATIONS).child(mCustomer.getId());

        //buildApiClient();

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mLocationRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mLocationRecyclerView.setHasFixedSize(false);

        mFireAdapter = new FirebaseRecyclerAdapter<Location, ViewHolder_Locations>(
                Location.class,
                R.layout.item_location,
                ViewHolder_Locations.class,
                mLocationsRef) {

            @Override
            public void populateViewHolder(final ViewHolder_Locations holder, Location location, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + location.toString());

                //holder.itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));

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

        mLocationRecyclerView.setAdapter(mFireAdapter);
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

                Log.i(Constants.LOG_TAG, "PLACE:" + places.get(0).getAddress().toString());

                tv_customer_address.setText(places.get(0).getAddress().toString());


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
