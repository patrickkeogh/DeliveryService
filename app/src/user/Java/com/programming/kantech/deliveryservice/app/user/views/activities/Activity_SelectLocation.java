package com.programming.kantech.deliveryservice.app.user.views.activities;

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
import com.programming.kantech.deliveryservice.app.data.model.pojo.AppUser;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Location;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.views.ui.ViewHolder_Locations;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by patri on 2017-09-21.
 */

public class Activity_SelectLocation extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private FirebaseRecyclerAdapter<Location, ViewHolder_Locations> mFireAdapter;
    private GoogleApiClient mClient;
    private AppUser mAppUser;
    private int mTitle;

    // Firebase variables
    private DatabaseReference mLocationsRef;

    @InjectView(R.id.rv_locations_list)
    RecyclerView mRecyclerView;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        ButterKnife.inject(this);

        if (savedInstanceState != null) {

            Log.i(Constants.LOG_TAG, "Activity_Details savedInstanceState is not null");
            if (savedInstanceState.containsKey(Constants.STATE_INFO_USER)) {
                mAppUser = savedInstanceState.getParcelable(Constants.STATE_INFO_USER);
            }

            if (savedInstanceState.containsKey(Constants.STATE_INFO_LOCATION_SELECT_MESSAGE)) {
                mTitle = savedInstanceState.getInt(Constants.STATE_INFO_LOCATION_SELECT_MESSAGE);
            }

        } else {
            Log.i(Constants.LOG_TAG, "Activity_Details savedInstanceState is null, get data from intent: ");
            mAppUser = getIntent().getParcelableExtra(Constants.EXTRA_USER);
            mTitle = getIntent().getIntExtra(Constants.EXTRA_LOCATION_SELECT_MESSAGE, 0);
        }


        if (mAppUser == null) {
            throw new IllegalArgumentException("Must pass EXTRA_USER");
        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(getResources().getString(R.string.select_pickup_location_title));

        }

        // Get a reference to the locations table
        //mLocationsRef = FirebaseDatabase.getInstance().getReference().child("locations");

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

        // Get a reference to the locations table
        mLocationsRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_LOCATIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_PLACE_PICKER && resultCode == Activity.RESULT_OK) {

            Place place = PlacePicker.getPlace(this, data);

            if (place == null) {
                Log.i(Constants.LOG_TAG, "No place selected");
                return;
            }

            addLocationToFirebase(place);
        }
    }

    @OnClick(R.id.btn_user_add_location)
    public void addLocation() {

        onGetLocationButtonClicked();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mClient != null){
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
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Constants.LOG_TAG, "onConnected called");
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
            Log.i(Constants.LOG_TAG, "CREATE NEW GOOGLE CLIENT");

            mClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }

    private void loadFirebaseAdapter() {

        mFireAdapter = new FirebaseRecyclerAdapter<Location, ViewHolder_Locations>(
                Location.class,
                R.layout.item_location_pickup,
                ViewHolder_Locations.class,
                mLocationsRef.child(mAppUser.getId())) {

            @Override
            public void populateViewHolder(final ViewHolder_Locations holder, final Location location, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + location.getPlaceId());

                final Place[] thisPlace = new Place[1];

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, location.getPlaceId());

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        thisPlace[0] = places.get(0);

                        Log.i(Constants.LOG_TAG, "PLACE:" + thisPlace[0].getAddress());

                        holder.setName(thisPlace[0].getName().toString());
                        holder.setAddress(thisPlace[0].getAddress().toString());


                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Send the selected customer back to the main activity
                        finishTheActivity(location, thisPlace[0]);
                    }
                });
            }
        };

        mRecyclerView.setAdapter(mFireAdapter);
    }

    private void finishTheActivity(Location location, Place place) {

        Intent resultIntent = new Intent();

        resultIntent.putExtra(Constants.EXTRA_LOCATION, location);
        resultIntent.putExtra(Constants.EXTRA_LOCATION_NAME, place.getName());
        resultIntent.putExtra(Constants.EXTRA_LOCATION_ADDRESS, place.getAddress());

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
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
            Intent i = builder.build(this);
            startActivityForResult(i, Constants.REQUEST_CODE_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(Constants.LOG_TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
        }
    }

    private void addLocationToFirebase(Place place) {
        Log.i(Constants.LOG_TAG, "addLocation() calledplaceid:" + place.getId());

        final Location location = new Location();
        location.setCustId(mAppUser.getId());
        location.setPlaceId(place.getId());


        mLocationsRef
                .child(mAppUser.getId())
                .push()
                .setValue(location, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        String uniqueKey = databaseReference.getKey();

                        location.setId(uniqueKey);

                        mLocationsRef.child(mAppUser.getId()).child(uniqueKey).setValue(location);

                        Log.i(Constants.LOG_TAG, "key:" + uniqueKey);
                    }
                });

    }
}
