package com.programming.kantech.deliveryservice.app.admin.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick keogh on 2017-08-23.
 * Displays a list of locations for the selected customer.  Alows the user
 * to select one before returning to the previous screen.
 */

public class Activity_SelectLocation extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private DatabaseReference mLocationsRef;

    // Local member variables
    private FirebaseRecyclerAdapter<Location, ViewHolder_Locations> mFireAdapter;
    private GoogleApiClient mClient;
    private Customer mCustomer;

    @BindView(R.id.rv_locations_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.btn_customer_select_pickup_location)
    Button mButton_PickupLocation;

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location_pickup);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_CUSTOMER)) {
                mCustomer = savedInstanceState.getParcelable(Constants.STATE_INFO_CUSTOMER);
            }
        } else {
            mCustomer = getIntent().getParcelableExtra(Constants.EXTRA_CUSTOMER);
        }

        if (mCustomer == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CUSTOMER");
        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("Locations for: " + mCustomer.getCompany());
        }

        // Get a reference to the locations table
        mLocationsRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_LOCATIONS);

        // Set up the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(false);

        mButton_PickupLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGetLocationButtonClicked();
            }
        });

    }

    private void finishTheActivity(Location location, Place place) {

        Intent resultIntent = new Intent();

        resultIntent.putExtra(Constants.EXTRA_LOCATION, location);
        resultIntent.putExtra(Constants.EXTRA_LOCATION_NAME, place.getName());
        resultIntent.putExtra(Constants.EXTRA_LOCATION_ADDRESS, place.getAddress());

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_LOCATION_PICKER && resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);

            if (place == null) {
                Log.i(Constants.LOG_TAG, "No place selected");
                return;
            }

            addLocation(place);
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

    /***
     * Button Click event handler to handle clicking the "select Location" Button
     *
     */
    public void onGetLocationButtonClicked() {
        //Log.i(Constants.LOG_TAG, "onGetLocationButtonClicked() called");
        try {

            // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
            // when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(this);
            startActivityForResult(i, Constants.REQUEST_CODE_LOCATION_PICKER);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(Constants.LOG_TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
        }
    }

    private void addLocation(Place place) {
        //Log.i(Constants.LOG_TAG, "addLocation() calledplaceid:" + place.getId());

        final Location location = new Location();
        location.setCustId(mCustomer.getId());
        location.setPlaceId(place.getId());

        mLocationsRef
                .child(mCustomer.getId())
                .push()
                .setValue(location, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        String uniqueKey = databaseReference.getKey();

                        location.setId(uniqueKey);

                        mLocationsRef.child(mCustomer.getId()).child(uniqueKey).setValue(location);

                        Log.i(Constants.LOG_TAG, "key:" + uniqueKey);
                    }
                });

    }

    private void buildApiClient() {
        if (mClient == null) {
            if(Utils_General.isNetworkAvailable(this)){
                mClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .addApi(Places.GEO_DATA_API)
                        .build();
            }else{
                Utils_General.showToast(this, getString(R.string.msg_no_network));
            }
        }
    }

    private void loadFirebaseAdapter() {

        mFireAdapter = new FirebaseRecyclerAdapter<Location, ViewHolder_Locations>(
                Location.class,
                R.layout.item_location,
                ViewHolder_Locations.class,
                mLocationsRef.child(mCustomer.getId())) {

            @Override
            public void populateViewHolder(final ViewHolder_Locations holder, final Location location, int position) {
                //Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + location.getPlaceId());

                final Place[] thisPlace = new Place[1];

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, location.getPlaceId());

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        thisPlace[0] = places.get(0);

                        //Log.i(Constants.LOG_TAG, "PLACE:" + thisPlace[0].getAddress());

                        holder.setName(thisPlace[0].getName().toString());
                        holder.setAddress(thisPlace[0].getAddress().toString());

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Send the selected location back to the main activity
                        finishTheActivity(location, thisPlace[0]);


                    }
                });

            }

        };

        mRecyclerView.setAdapter(mFireAdapter);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mClient != null) {
            mClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient = null;

        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mClient == null) {
            buildApiClient();
            mClient.connect();
        } else {
            if (!mClient.isConnected()) {
                mClient.connect();
            }
        }
    }
}
