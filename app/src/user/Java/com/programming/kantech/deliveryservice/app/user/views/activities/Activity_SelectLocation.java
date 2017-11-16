package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

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
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.user.provider.Contract_DeliveryService;
import com.programming.kantech.deliveryservice.app.user.views.ui.Adapter_PlaceList;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-09-21.
 * An activity used to select a google Place from local storage or
 * from the api
 */

public class Activity_SelectLocation extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Adapter_PlaceList.PlaceListOnClickHandler {

    // Local member variables
    private GoogleApiClient mClient;
    private Adapter_PlaceList mAdapter;
    private AppUser mAppUser;
    private int mTitle;

    @BindView(R.id.rv_locations_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
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

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_USER)) {
                mAppUser = savedInstanceState.getParcelable(Constants.STATE_INFO_USER);
            }
            if (savedInstanceState.containsKey(Constants.STATE_INFO_LOCATION_SELECT_MESSAGE)) {
                mTitle = savedInstanceState.getInt(Constants.STATE_INFO_LOCATION_SELECT_MESSAGE);
            }
        } else {
            mAppUser = getIntent().getParcelableExtra(Constants.EXTRA_USER);
            mTitle = getIntent().getIntExtra(Constants.EXTRA_LOCATION_SELECT_MESSAGE, 0);
        }


        if (mAppUser == null || mTitle == 0) {
            throw new IllegalArgumentException("Must pass EXTRA_USER & EXTRA_LOCATION_SELECT_MESSAGE ");
        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(getResources().getString(mTitle));

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

        mAdapter = new Adapter_PlaceList(this, null, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Save the current state of this activity
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the app bar title in the instance state
        outState.putInt(Constants.STATE_INFO_LOCATION_SELECT_MESSAGE, mTitle);

        // Store the user in the instance state
        outState.putParcelable(Constants.STATE_INFO_USER, mAppUser);
    }

    /***
     * Called when the Place Picker Activity returns back with a selected place (or after canceling)
     *
     * @param requestCode The request code passed when calling startActivityForResult
     * @param resultCode  The result code specified by the second activity
     * @param data        The Intent that carries the result data.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_PLACE_PICKER && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            if (place == null) {
                Log.i(Constants.LOG_TAG, "No place selected");
                return;
            }

            // Extract the place information from the API
            String placeID = place.getId();

            // Insert a new place into DB
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract_DeliveryService.PlaceEntry.COLUMN_PLACE_ID, placeID);
            getContentResolver().insert(Contract_DeliveryService.PlaceEntry.CONTENT_URI, contentValues);

            // Get live data information
            refreshPlacesData();
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
        refreshPlacesData();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

//    private void loadFirebaseAdapter() {
//
//        mFireAdapter = new FirebaseRecyclerAdapter<Location, ViewHolder_Locations>(
//                Location.class,
//                R.layout.item_location_pickup,
//                ViewHolder_Locations.class,
//                mLocationsRef.child(mAppUser.getId())) {
//
//            @Override
//            public void populateViewHolder(final ViewHolder_Locations holder, final Location location, int position) {
//                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + location.getPlaceId());
//
//                final Place[] thisPlace = new Place[1];
//
//                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, location.getPlaceId());
//
//                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
//                    @Override
//                    public void onResult(@NonNull PlaceBuffer places) {
//
//                        thisPlace[0] = places.get(0);
//
//                        Log.i(Constants.LOG_TAG, "PLACE:" + thisPlace[0].getAddress());
//
//                        holder.setName(thisPlace[0].getName().toString());
//                        holder.setAddress(thisPlace[0].getAddress().toString());
//
//
//                    }
//                });
//
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // Send the selected customer back to the main activity
//                        finishTheActivity(location, thisPlace[0]);
//                    }
//                });
//            }
//        };
//
//        mRecyclerView.setAdapter(mFireAdapter);
//    }

    private void finishTheActivity(Place place) {

        Intent resultIntent = new Intent();

        resultIntent.putExtra(Constants.EXTRA_PLACE_ID, place.getId());
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

    public void refreshPlacesData() {
        Uri uri = Contract_DeliveryService.PlaceEntry.CONTENT_URI;
        Cursor data = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);

        if (data == null || data.getCount() == 0) return;
        List<String> guids = new ArrayList<>();
        while (data.moveToNext()) {
            guids.add(data.getString(data.getColumnIndex(Contract_DeliveryService.PlaceEntry.COLUMN_PLACE_ID)));
        }
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient,
                guids.toArray(new String[guids.size()]));

        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                mAdapter.swapPlaces(places);
            }
        });

        data.close();
    }

    @Override
    public void onClick(long id, Place place) {
        Log.i(Constants.LOG_TAG, "onClick called in Activity_SelectLocation");

        // send selected location back to activity
        finishTheActivity(place);

    }
}
