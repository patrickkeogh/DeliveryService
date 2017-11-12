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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Customers;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Customer;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick keogh on 2017-08-23.
 *
 */

public class Activity_SelectCustomer extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Firebase member variables
    private DatabaseReference mCustomersRef;
    private DatabaseReference mMainOfficeRef;

    // Local member variables
    private FirebaseRecyclerAdapter<Customer, ViewHolder_Customers> mFireAdapter;
    private GoogleApiClient mClient;

    // Views to bind too
    @BindView(R.id.rv_customers_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.btn_customer_add)
    Button mButtonCustomerAdd;

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_customer);

        ButterKnife.bind(this);

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("Select A Customer");
        }

        // Get a reference to the locations table
        mCustomersRef = FirebaseDatabase.getInstance().getReference().child("customers");
        mMainOfficeRef = FirebaseDatabase.getInstance().getReference().child("locations");

        // Set up the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView.
         */
        mRecyclerView.setHasFixedSize(false);


    }

    /**
     * Called before closing to return a value to the calling activity
     *
     * @param customer The customer that will be returned to the calling activity
     */
    private void finishTheActivity(Customer customer) {

        Intent resultIntent = new Intent();

        resultIntent.putExtra(Constants.EXTRA_CUSTOMER, customer);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        loadFireAdapter();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

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
    public void onPause() {
        super.onPause();

        if (mClient != null) {
            mClient.disconnect();
        }
    }

    private void buildApiClient() {
        if (mClient == null) {
            // Build up the LocationServices API client
            // Uses the addApi method to request the LocationServices API
            // Also uses enableAutoManage to automatically know when to connect/suspend the client
            mClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }

    private void loadFireAdapter() {
        mFireAdapter = new FirebaseRecyclerAdapter<Customer, ViewHolder_Customers>(
                Customer.class,
                R.layout.item_admin_customer,
                ViewHolder_Customers.class,
                mCustomersRef) {

            @Override
            public void populateViewHolder(final ViewHolder_Customers holder, final Customer customer, int position) {
                //Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + customer.getCompany());

                holder.setName(customer.getCompany());

                // Get head office from locations for this customer
                mMainOfficeRef.child(customer.getId()).orderByChild(Constants.FIREBASE_CHILD_MAIN_ADDRESS).equalTo(true)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Log.i(Constants.LOG_TAG, "onDataChange() called:" + dataSnapshot.toString());

                                String placeId = "";

                                for (DataSnapshot locations : dataSnapshot.getChildren()) {

                                    // Should only be 1 main address
                                    placeId = (String) locations.child(Constants.FIREBASE_CHILD_PLACE_ID).getValue();
                                }

                                //Log.i(Constants.LOG_TAG, "Main Location:" + placeId);

                                if (!Objects.equals(placeId, "")) {
                                    PendingResult<PlaceBuffer> placeResult;

                                    placeResult = Places.GeoDataApi.getPlaceById(mClient, placeId);

                                    placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                                        @Override
                                        public void onResult(@NonNull PlaceBuffer places) {

                                            Place myPlace = places.get(0);

                                            //Log.i(Constants.LOG_TAG, "PLACE:" + myPlace.getAddress());

                                            holder.setAddress(myPlace.getAddress().toString());


                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(Constants.LOG_TAG, databaseError.toString());

                            }
                        });

                // add click listener to the recyclerview item
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Send the selected customer back to the calling activity
                        finishTheActivity(customer);
                    }
                });

            }

        };

        mRecyclerView.setAdapter(mFireAdapter);
    }
}
