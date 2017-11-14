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
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.user.views.ui.ViewHolder_Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick keogh on 2017-10-04.
 * An activity to display a list of orders for the logged in user
 */

public class Activity_MyOrders extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private ActionBar mActionBar;
    private AppUser mAppUser;
    private RecyclerView.AdapterDataObserver mObserver;
    private FirebaseRecyclerAdapter<Order, ViewHolder_Order> mFireAdapter;
    private GoogleApiClient mClient;
    //private Order mSelectedOrder;
    private String mStatusFilter = Constants.ORDER_STATUS_OPEN;

    private DatabaseReference mOrdersRef;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.rv_user_orders)
    RecyclerView mRecyclerView;

    @BindView(R.id.tv_empty_view)
    TextView tv_empty_view;

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
                //Log.i(Constants.LOG_TAG, "Sign out clicked:");
                AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Intent intent = new Intent(Activity_MyOrders.this, Activity_Splash.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                });
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
                        holder.setBackgroundColor(Activity_MyOrders.this, R.color.colorGreen);
                        holder.btn_order_show_map.setEnabled(false);
                        holder.btn_order_show_map.setTextColor(ContextCompat.getColor(Activity_MyOrders.this, R.color.colorTextGrey));
                        break;
                    case Constants.ORDER_STATUS_PICKUP_COMPLETE:
                        holder.setBackgroundColor(Activity_MyOrders.this, R.color.colorGreen);
                        holder.btn_order_show_map.setEnabled(true);
                        holder.btn_order_show_map.setTextColor(ContextCompat.getColor(Activity_MyOrders.this, R.color.colorGreen));
                        break;
                    case Constants.ORDER_STATUS_COMPLETE:
                        holder.setBackgroundColor(Activity_MyOrders.this, R.color.colorAccent);
                        holder.btn_order_show_map.setEnabled(false);
                        holder.btn_order_show_map.setTextColor(ContextCompat.getColor(Activity_MyOrders.this, R.color.colorTextGrey));
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

                        //Log.i(Constants.LOG_TAG, "Show MAp clicked for: " + order.getCustomerName());

                        Intent intent = new Intent(Activity_MyOrders.this, Activity_MyMap.class);
                        intent.putExtra(Constants.EXTRA_ORDER, order);
                        startActivity(intent);

                    }
                });

                holder.btn_order_show_details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //Log.i(Constants.LOG_TAG, "Show Details clicked for: " + order.getCustomerName());

                        Intent intent = new Intent(Activity_MyOrders.this, Activity_OrderDetails.class);
                        intent.putExtra(Constants.EXTRA_ORDER, order);
                        startActivity(intent);
                    }
                });



            }

        };

        mRecyclerView.setAdapter(mFireAdapter);

        Query query = getDatabaseRef();

        // Hide or show the list depending on if there are records
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Perform initial setup, this will only be called once
                if(dataSnapshot.hasChildren()){
                    showList(true);
                }else{
                    showList(false);
                }

                // Create an observer to check if the list changes
                mObserver = new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {

                        int count = mFireAdapter.getItemCount();
                        if(count == 0){
                            showList(false);
                        }else{
                            showList(true);
                        }
                    }

                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        int count = mFireAdapter.getItemCount();
                        if(count == 0){
                            showList(false);
                        }else{
                            showList(true);
                        }
                    }
                };
                mFireAdapter.registerAdapterDataObserver(mObserver);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void showList(boolean bShowList) {

        if(bShowList){
            mRecyclerView.setVisibility(View.VISIBLE);
            tv_empty_view.setVisibility(View.GONE);
        }else{
            mRecyclerView.setVisibility(View.GONE);
            tv_empty_view.setVisibility(View.VISIBLE);
        }
    }
}
