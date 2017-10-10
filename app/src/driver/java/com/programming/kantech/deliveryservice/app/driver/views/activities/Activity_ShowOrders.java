package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.media.MediaPlayer;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Order;
import com.programming.kantech.deliveryservice.app.driver.views.ui.ViewHolder_Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by patri on 2017-09-13.
 */

public class Activity_ShowOrders extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private FirebaseRecyclerAdapter<Order, ViewHolder_Order> mFireAdapter;
    private GoogleApiClient mClient;

    private ActionBar mActionBar;
    private long mDisplayDateStartTimeInMillis;

    private Driver mDriver;

    private DatabaseReference mOrdersRef;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.rv_orders_list)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Log.i(Constants.LOG_TAG, "onCreate() in Driver Activity_ShowOrders");

        ButterKnife.inject(this);

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        // get today at 00:00 hrs
        Calendar date = new GregorianCalendar();
        mDisplayDateStartTimeInMillis = Utils_General.getStartTimeForDate(date.getTimeInMillis());

        String showDate = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle(showDate);
        }

        // Get a reference to the orders node
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        // Set up the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);

        String strQuery = "true_" + mDisplayDateStartTimeInMillis + "_Vf2soD9jUkXdxjx8ACvOGFOal9E2";

        mFireAdapter = new FirebaseRecyclerAdapter<Order, ViewHolder_Order>(
                Order.class,
                R.layout.item_order,
                ViewHolder_Order.class,
                mOrdersRef.orderByChild("inProgressDateDriverId").equalTo(strQuery)) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                Utils_General.showToast(getApplicationContext(), "Firebase data changed");

                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.bubble_pop);
                mp.start();
            }

            @Override
            public void populateViewHolder(final ViewHolder_Order holder, final Order order, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + order.getCustomerName());

                holder.setCustomerName(order.getCustomerName());
                //holder.setOrderDate(Utils_General.getFormattedLongDateStringFromLongDate(order.getPickupDate()));
                holder.setOrderStatus(order.getStatus());

                PendingResult<PlaceBuffer> result_pickup = Places.GeoDataApi.getPlaceById(mClient, order.getPickupLocationId());

                result_pickup.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        holder.setPickupAddress(places.get(0).getAddress().toString());
                    }
                });

                PendingResult<PlaceBuffer> result_delivery = Places.GeoDataApi.getPlaceById(mClient, order.getDeliveryLocationId());

                result_delivery.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        holder.setDeliveryAddress(places.get(0).getAddress().toString());
                    }
                });

                // add click listener to the recyclerview item
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Send the selected customer back to the main activity
                        //finishTheActivity(customer);
                    }
                });

            }

        };

        mRecyclerView.setAdapter(mFireAdapter);

    }

    private void buildApiClient() {
        Log.i(Constants.LOG_TAG, "buildApiClient() called");

        if(mClient == null){
            Log.i(Constants.LOG_TAG, "CREATE NEW GOOGLE CLIENT");

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
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
    protected void onStop() {
        super.onStop();

        if(mClient != null){
            mClient.disconnect();
        }

    }


}
