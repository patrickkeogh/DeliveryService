package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.user.views.ui.ViewHolder_Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-10-04.
 */

public class Activity_MyOrders extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private ActionBar mActionBar;
    private AppUser mAppUser;
    private FirebaseRecyclerAdapter<Order, ViewHolder_Order> mFireAdapter;
    private GoogleApiClient mClient;
    private Order mSelectedOrder;
    private String mStatusFilter = Constants.ORDER_STATUS_OPEN;

    private DatabaseReference mOrdersRef;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.rv_user_orders)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_orders);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {

            Log.i(Constants.LOG_TAG, "Activity_Details savedInstanceState is not null");
            if (savedInstanceState.containsKey(Constants.STATE_INFO_USER)) {
                mAppUser = savedInstanceState.getParcelable(Constants.STATE_INFO_USER);
            }

        } else {
            Log.i(Constants.LOG_TAG, "Activity_Details savedInstanceState is null, get data from intent: ");
            mAppUser = getIntent().getParcelableExtra(Constants.EXTRA_USER);
        }


        if (mAppUser == null) {
            throw new IllegalArgumentException("Must pass EXTRA_USER");
        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("My Orders");
        }

        // Get a reference to the orders table
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        // Set up the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(Constants.STATE_INFO_USER, mAppUser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_orders, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG, "onOptionsItemSelected:" + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_user_orders_show_completed:
                return true;
            case R.id.action_user_orders_show_open:
                mStatusFilter = Constants.ORDER_STATUS_OPEN;
                loadFirebaseAdapter();
                return true;
            case R.id.action_sign_out:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void buildApiClient() {
        if (mClient == null) {
            Log.i(Constants.LOG_TAG, "CREATE NEW GOOGLE CLIENT");

            mClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Places.GEO_DATA_API)
                    .build();
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

    private Query getDatabaseRef(){
        Log.i(Constants.LOG_TAG, "CustId:" + mAppUser.getId());

        return mOrdersRef
                .orderByChild(Constants.FIREBASE_CHILD_ORDER_CUST_ID)
                .equalTo(mAppUser.getId());


    }

    private void loadFirebaseAdapter() {

        mFireAdapter = new FirebaseRecyclerAdapter<Order, ViewHolder_Order>(
                Order.class,
                R.layout.item_order,
                ViewHolder_Order.class,
                getDatabaseRef()) {

            @Override
            public void populateViewHolder(final ViewHolder_Order holder, final Order order, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + order.getCustomerName());

                //mLayout.setBackgroundColor(ContextCompat.getColor(Activity_MyOrders.this, R.color.colorAccent));

                switch (order.getStatus()){
                    case Constants.ORDER_STATUS_BOOKED:
                        holder.setBackgroundColor(Activity_MyOrders.this, R.color.colorPrimary);
                        holder.btn_order_show_map.setEnabled(false);
                        holder.btn_order_show_map.setTextColor(ContextCompat.getColor(Activity_MyOrders.this, R.color.colorTextGrey));
                        break;
                    case Constants.ORDER_STATUS_ASSIGNED:
                        holder.setBackgroundColor(Activity_MyOrders.this, R.color.colorAccent);
                        holder.btn_order_show_map.setEnabled(true);
                        holder.btn_order_show_map.setTextColor(ContextCompat.getColor(Activity_MyOrders.this, R.color.colorGreen));
                        break;

                }

                holder.setOrderDate(Utils_General.getFormattedLongDateStringFromLongDate(order.getPickupDate()));
                holder.setOrderStatus(order.getStatus());

                final Place[] thisPlace = new Place[1];

                PendingResult<PlaceBuffer> result_pickup = Places.GeoDataApi.getPlaceById(mClient, order.getPickupLocationId());

                result_pickup.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        thisPlace[0] = places.get(0);
                        holder.setPickupAddress(thisPlace[0].getAddress().toString());
                    }
                });

                PendingResult<PlaceBuffer> result_delivery = Places.GeoDataApi.getPlaceById(mClient, order.getDeliveryLocationId());

                result_delivery.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        thisPlace[0] = places.get(0);
                        holder.setDeliveryAddress(thisPlace[0].getAddress().toString());
                    }
                });

                holder.btn_order_show_map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Log.i(Constants.LOG_TAG, "Show MAp clicked for: " + order.getCustomerName());

                        Intent intent = new Intent(Activity_MyOrders.this, Activity_MyMap.class);
                        startActivity(intent);

                    }
                });

                holder.btn_order_show_details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Log.i(Constants.LOG_TAG, "Show Details clicked for: " + order.getCustomerName());

                        Intent intent = new Intent(Activity_MyOrders.this, Activity_OrderDetails.class);
                        intent.putExtra(Constants.EXTRA_ORDER, order);
                        startActivity(intent);
                    }
                });



            }

        };

        mRecyclerView.setAdapter(mFireAdapter);


    }
}
