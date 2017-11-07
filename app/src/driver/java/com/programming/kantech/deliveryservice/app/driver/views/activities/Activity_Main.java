package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.data.model.pojo.directions.Distance;
import com.programming.kantech.deliveryservice.app.data.model.pojo.directions.Leg;
import com.programming.kantech.deliveryservice.app.data.model.pojo.directions.Results;
import com.programming.kantech.deliveryservice.app.data.model.pojo.directions.Route;
import com.programming.kantech.deliveryservice.app.data.retrofit.ApiClient;
import com.programming.kantech.deliveryservice.app.data.retrofit.ApiInterface;
import com.programming.kantech.deliveryservice.app.driver.views.ui.ViewHolder_Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;
import com.programming.kantech.deliveryservice.app.utils.Utils_Preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by patri on 2017-10-04.
 */

public class Activity_Main extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private ActionBar mActionBar;
    private String mUsername;
    private Driver mDriver;
    private GoogleApiClient mClient;
    private GoogleMap mGoogleMap;
    private FirebaseRecyclerAdapter<Order, ViewHolder_Order> mFireAdapter;
    private Order mSelectedOrder;
    private boolean mShowFilter = true;


    private boolean mShowOpen = true;
    private boolean mShowComplete = true;
    private boolean mShowDeliveryLocations = true;
    private boolean mShowPickupLocations = true;

    // The displayed Date in millisecs
    private long mDisplayDateStartTimeInMillis;

    private DatabaseReference mDriverRef;
    private DatabaseReference mOrdersRef;
    private DatabaseReference mDriverLocationRef;

    private ValueEventListener mOrdersEventListener;
    private Query mOrdersQuery;


    // Location member variables
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private LatLngBounds.Builder builder;

    // Marker for the driver
    private Marker mDriverMarker;

    // List of Markers for all order destinations
    private ArrayList<Marker> mLocationMarkers = new ArrayList<>();

    // The orders for the selected day
    private ArrayList<Order> mOrdersList = new ArrayList<>();

    private ApiInterface apiService;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.mapView)
    MapView mMapView;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;

    @BindView(R.id.rv_driver_orders)
    RecyclerView mRecyclerView;

    @BindView(R.id.tv_order_filter_date)
    TextView tv_order_filter_date;

    @BindView(R.id.nav_view)
    NavigationView mNavView;

    @BindView(R.id.btn_driver_order_previous_day)
    AppCompatButton btn_driver_order_previous_day;

    @BindView(R.id.btn_driver_order_next_day)
    AppCompatButton btn_driver_order_next_day;

    @BindView(R.id.btn_driver_pickup_complete)
    AppCompatButton btn_driver_pickup_complete;

    @BindView(R.id.btn_driver_delivery_complete)
    AppCompatButton btn_driver_delivery_complete;

    @BindView(R.id.layout_map_filter)
    LinearLayout layout_map_filter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(Constants.LOG_TAG, "onCreate in Driver Main called");

        // load layout
        setContentView(R.layout.activity_main);

        apiService = ApiClient.getClient().create(ApiInterface.class);

        // Load the saved state if there is one
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_ORDERS_LIST)) {
                Log.i(Constants.LOG_TAG, "we found the orders list in savedInstanceState");
                mOrdersList = savedInstanceState.getParcelableArrayList(Constants.STATE_INFO_ORDERS_LIST);
            }
        }

        // use ButterKnife to inject the layout views
        ButterKnife.bind(this);

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        // Create the display date, today at 00:00 hrs
        Calendar date = new GregorianCalendar();
        mDisplayDateStartTimeInMillis = Utils_General.getStartTimeForDate(date.getTimeInMillis());

        //Log.i(Constants.LOG_TAG, "Start Time for today in Millis:" + mDisplayDateStartTimeInMillis);

        String showDate = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);

        tv_order_filter_date.setText(showDate);


        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle(showDate);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // initialize the map
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // this will call onMapReady() when finished
        mMapView.getMapAsync(this);

        // Get a root reference to the Drivers Node
        mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);

        // Get a root reference to the orders table
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        // get a root reference to the drivers location node
        mDriverLocationRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_NODE_DRIVER_LOCATIONS);


        // Set up the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG, "onOptionsItemSelected:" + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_driver_filter:

                mShowFilter = !mShowFilter;

                Animation fadeInAnimation;

                if (mShowFilter) {
                    fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.view_fade_in);
                    fadeInAnimation.setFillAfter(true);
                    // Now Set your animation
                    layout_map_filter.startAnimation(fadeInAnimation);

                    layout_map_filter.setVisibility(View.VISIBLE);

                } else {
                    fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.view_fade_out);
                    fadeInAnimation.setFillAfter(false);
                    // Now Set your animation
                    layout_map_filter.startAnimation(fadeInAnimation);

                    layout_map_filter.setVisibility(View.INVISIBLE);
                }

                return true;
            case R.id.action_driver_orders:
                Intent intent_orders = new Intent(this, Activity_ShowOrders.class);
                intent_orders.putExtra(Constants.EXTRA_DRIVER, mDriver);
                startActivity(intent_orders);
                return true;
            case R.id.action_driver_photo:
                Intent intent_photo = new Intent(this, Activity_Photo.class);

                // Add the driver to the intent
                Log.i(Constants.LOG_TAG, "Driver in Main:" + mDriver.toString());
                intent_photo.putExtra(Constants.EXTRA_DRIVER, mDriver);
                startActivity(intent_photo);

                return true;
            case R.id.action_sign_out:
                Utils_General.showToast(this, "Signout called");
                removeDriverFromActive();
                //FirebaseAuth.getInstance().signOut();
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(Activity_Main.this, Activity_Splash.class));
                                finish();
                            }
                        });

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Add the current orders list to the state
        outState.putParcelableArrayList(Constants.STATE_INFO_ORDERS_LIST, mOrdersList);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_LOCATION_PERMISSION);
        }

        mGoogleMap = googleMap;
        mGoogleMap.setPadding(100, 100, 100, 100);

        //mGoogleMap.setMyLocationEnabled(true);

        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

    }



    private void onSignedOutCleanup() {

        // Remove the driver from the active driver node
    }

    protected synchronized void buildGoogleApiClients() {
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Log.i(Constants.LOG_TAG, "onConnected()");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Activity_Main.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_LOCATION_PERMISSION);
        }

        // Get the last known location for the driver and force a location changed call

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Set an initial location
                            onLocationChanged(location);
                        }
                    }
                });

        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mLocationRequest, this);

        paintDriverLocationToMap();


        attachOrderReadListener();

        loadFirebaseAdapter();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void loadFirebaseAdapter() {

        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }

        mFireAdapter = new FirebaseRecyclerAdapter<Order, ViewHolder_Order>(
                Order.class,
                R.layout.item_driver_order,
                ViewHolder_Order.class,
                getDatabaseQuery()) {
            @Override
            public ViewHolder_Order onCreateViewHolder(ViewGroup parent, int viewType) {
                return super.onCreateViewHolder(parent, viewType);
            }

            @Override
            public void populateViewHolder(final ViewHolder_Order holder, final Order order, int position) {
                //Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + order.getCustomerName());

                if (mLastLocation != null) {


                    String origin = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                    String dest = "place_id:" + order.getPickupLocationId();

                    Call<Results> call = apiService.getDistance(origin, dest);

                    call.enqueue(new Callback<Results>() {
                        @Override
                        public void onResponse(@NonNull Call<Results> call, @NonNull Response<Results> response) {

                            //Log.i(Constants.LOG_TAG, "Response:" + response);

                            if (response.isSuccessful()) {
                                // Code 200, 201
                                Results result = response.body();

                                List<Route> routes;

                                if (result != null) {
                                    routes = result.getRoutes();

                                    if (routes != null && routes.size() != 0) {
                                        Log.i(Constants.LOG_TAG, "Routes:" + routes.toString());
                                        Route route = routes.get(0);

                                        List<Leg> legs = route.getLegs();

                                        Leg trip = legs.get(0);

                                        Distance distance = trip.getDistance();

                                        //Log.i(Constants.LOG_TAG, "Response Distance:" + distance.getText());

                                        if (distance.getValue() < 500) {
                                            holder.setDistance("You have arrived!");

                                        } else {
                                            holder.setDistance(distance.getText());
                                        }

                                    }


                                }


                            } else {
                                //movies_failed(response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Results> call, @NonNull Throwable t) {
                            // Log error here since request failed
                            Log.e(Constants.LOG_TAG, t.toString());
                            //movies_failed(t.toString());
                        }
                    });
                }

                holder.setCustomerName(order.getCustomerName());
                holder.setOrderStatus(order.getStatus());


                switch (order.getStatus()) {
                    case Constants.ORDER_STATUS_BOOKED:
                        holder.setBackgroundColor(Activity_Main.this, R.color.colorPrimary);
                        break;
                    case Constants.ORDER_STATUS_ASSIGNED:
                        holder.setBackgroundColor(Activity_Main.this, R.color.colorAccent);
                        break;

                }
                if (mSelectedOrder != null) {
                    if (Objects.equals(order.getId(), mSelectedOrder.getId())) {
                        holder.setSelectedColor(Activity_Main.this, R.color.colorPrimaryLight);
                    } else {
                        holder.setSelectedColor(Activity_Main.this, R.color.colorWhite);
                    }
                }


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

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Send the selected customer back to the main activity
                        //finishTheActivity(location, thisPlace[0]);

                        Log.i(Constants.LOG_TAG, "onClick called");
                        orderSelected(order);


                    }
                });

            }


        };

        mRecyclerView.setAdapter(mFireAdapter);


    }

    private void orderSelected(Order order) {
        Log.i(Constants.LOG_TAG, "orderSelected()");

        mSelectedOrder = order;
        setDrawerButtons();
        mFireAdapter.notifyDataSetChanged();

    }

    private Query getDatabaseQuery() {
        String strQuery = "true_" + mDisplayDateStartTimeInMillis + "_" + mDriver.getUid();
        return mOrdersRef.orderByChild("inProgressDateDriverId").equalTo(strQuery);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_SIGN_IN) {

            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Utils_General.showToast(this, "Signed In!");

            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Utils_General.showToast(this, "Signed In Cancelled!");
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();

        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
        removeDriverFromActive();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeDriverFromActive();
    }

    @OnClick(R.id.btn_driver_order_previous_day)
    public void getPreviousDay() {

        mSelectedOrder = null;
        setDrawerButtons();

        mDisplayDateStartTimeInMillis -= Constants.DAY;

        String yesterday = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);

        if (mActionBar != null) {
            mActionBar.setTitle(yesterday);
        }

        tv_order_filter_date.setText(yesterday);

        loadFirebaseAdapter();

        detachFirebaseOrderListeners();
        attachOrderReadListener();

    }

    @OnClick(R.id.btn_driver_order_next_day)
    public void getNextDay() {

        mSelectedOrder = null;
        setDrawerButtons();

        mDisplayDateStartTimeInMillis += Constants.DAY;

        String tomorrow = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);

        if (mActionBar != null) {
            mActionBar.setTitle(tomorrow);
        }

        tv_order_filter_date.setText(tomorrow);

        // Prepare the menu
        //invalidateOptionsMenu();

        loadFirebaseAdapter();

        //mFireAdapter.notifyDataSetChanged();

        detachFirebaseOrderListeners();
        attachOrderReadListener();
    }

    @OnClick(R.id.btn_driver_pickup_complete)
    public void completePickup() {

        final Place[] thisPlace = new Place[1];

        PendingResult<PlaceBuffer> result_pickup = Places.GeoDataApi.getPlaceById(mClient, mSelectedOrder.getPickupLocationId());

        result_pickup.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                thisPlace[0] = places.get(0);


                showAlertMessage("Confirm pickup has been completed for:",
                        mSelectedOrder.getCustomerName(),
                        thisPlace[0].getAddress().toString(),
                        "Pickup completion has been canceled",
                        Constants.ORDER_STATUS_PICKUP_COMPLETE);

            }
        });

    }

    @OnClick(R.id.btn_driver_delivery_complete)
    public void completeDelivery() {

        final Place[] thisPlace = new Place[1];

        PendingResult<PlaceBuffer> result_delivery = Places.GeoDataApi.getPlaceById(mClient, mSelectedOrder.getDeliveryLocationId());

        result_delivery.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                thisPlace[0] = places.get(0);


                showAlertMessage("Confirm delivery has been completed for:",
                        mSelectedOrder.getCustomerName(),
                        thisPlace[0].getAddress().toString(),
                        "Delivery completion has been canceled",
                        Constants.ORDER_STATUS_COMPLETE);

            }
        });


    }

    @OnCheckedChanged(R.id.cb_driver_pickup_locations)
    public void onPickupLocationsSelected(CompoundButton button, boolean checked){
        mShowPickupLocations = checked;
        paintMarkersToMap();
    }

    @OnCheckedChanged(R.id.cb_driver_delivery_locations)
    public void onDeliveryLocationsSelected(CompoundButton button, boolean checked){
        mShowDeliveryLocations = checked;
        paintMarkersToMap();
    }

    @OnCheckedChanged({R.id.rb_driver_all, R.id.rb_driver_open, R.id.rb_driver_complete})
    public void onRadioButtonCheckChanged(CompoundButton button, boolean checked) {
        if (checked) {
            switch (button.getId()) {
                case R.id.rb_driver_all:
                    mShowOpen = true;
                    mShowComplete = true;
                    break;
                case R.id.rb_driver_open:
                    mShowOpen = true;
                    mShowComplete = false;
                    break;
                case R.id.rb_driver_complete:
                    mShowOpen = true;
                    mShowComplete = false;
                    break;
            }
            paintMarkersToMap();
        }
    }


    private void showAlertMessage(String title, String company, String address, final String cancelText, final String status) {
        Log.i(Constants.LOG_TAG, "Alert Message called");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialog_confirm = inflater.inflate(R.layout.alert_confirm_location_complete, null);

        TextView tv_location_pickup_name = dialog_confirm.findViewById(R.id.tv_location_pickup_name);
        tv_location_pickup_name.setText(company);

        TextView tv_location_pickup_address = dialog_confirm.findViewById(R.id.tv_location_pickup_address);
        tv_location_pickup_address.setText(address);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialog_confirm)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mOrdersRef.child(mSelectedOrder.getId())
                                .child(Constants.FIREBASE_CHILD_ORDER_STATUS)
                                .setValue(status);

                        mSelectedOrder.setStatus(status);

                        paintMarkersToMap();
                        setDrawerButtons();

                        // TODO: let the user know the order has been picked up

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utils_General.showToast(Activity_Main.this, cancelText);
                    }
                })
                .show();

    }

    private void attachOrderReadListener() {

        mOrdersQuery = getDatabaseQuery();

        if (mOrdersEventListener == null) {

            mOrdersEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Log.i(Constants.LOG_TAG, "onDataChange in attachOrderListener");

                    ArrayList<Order> newOrderList = new ArrayList<>();

                    if (dataSnapshot != null) {
                        //Log.i(Constants.LOG_TAG, "we have an order snapshot");

                        // Get each order that is in progress
                        for (DataSnapshot orders : dataSnapshot.getChildren()) {

                            Order order = orders.getValue(Order.class);
                            //Log.i(Constants.LOG_TAG, "ORDERS:" + order.getCustomerName());

                            newOrderList.add(order);
                        }
                    }

                    if (newOrderList.size() > mOrdersList.size()) {
                        // Should be a new order, make a sound
                        final MediaPlayer mp = MediaPlayer.create(Activity_Main.this, R.raw.alarm_rooster);
                        //mp.start();
                    }

                    mOrdersList = newOrderList;

                    paintMarkersToMap();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(Constants.LOG_TAG, databaseError.getMessage());
                }
            };

            mOrdersQuery.addValueEventListener(mOrdersEventListener);

        }
    }

    private void detachFirebaseOrderListeners() {

        if (mOrdersEventListener != null) {
            mOrdersQuery.removeEventListener(mOrdersEventListener);
            mOrdersEventListener = null;
        }
    }

    private void setDrawerButtons() {
        Log.i(Constants.LOG_TAG, "setDrawerButtons called");

        Drawable top;

        if (mSelectedOrder == null) {
            top = ContextCompat.getDrawable(this, R.drawable.ic_info_outline_100dp);
            btn_driver_pickup_complete.setEnabled(false);
            btn_driver_pickup_complete.setText("Select Order");
            btn_driver_pickup_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

            btn_driver_delivery_complete.setEnabled(false);
            btn_driver_delivery_complete.setText("Select Order");
            btn_driver_delivery_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
        } else {
            Log.i(Constants.LOG_TAG, "Order is not null");


            btn_driver_pickup_complete.setText("Pickup Complete");
            btn_driver_delivery_complete.setText("Delivery Complete");

            Log.i(Constants.LOG_TAG, "Status:" + mSelectedOrder.getStatus());

            switch (mSelectedOrder.getStatus()) {
                case Constants.ORDER_STATUS_ASSIGNED:
                    Log.i(Constants.LOG_TAG, "Status is assigned");
                    btn_driver_pickup_complete.setEnabled(true);
                    btn_driver_delivery_complete.setEnabled(false);


                    top = ContextCompat.getDrawable(this, R.drawable.ic_not_complete_100dp);
                    btn_driver_pickup_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

                    top = ContextCompat.getDrawable(this, R.drawable.ic_not_complete_100dp);
                    btn_driver_delivery_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

                    break;

                case Constants.ORDER_STATUS_PICKUP_COMPLETE:
                    btn_driver_pickup_complete.setEnabled(false);
                    btn_driver_delivery_complete.setEnabled(true);


                    top = ContextCompat.getDrawable(this, R.drawable.ic_complete_100dp);
                    btn_driver_pickup_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

                    top = ContextCompat.getDrawable(this, R.drawable.ic_not_complete_100dp);
                    btn_driver_delivery_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

                    break;

                case Constants.ORDER_STATUS_COMPLETE:
                    btn_driver_pickup_complete.setEnabled(false);
                    btn_driver_delivery_complete.setEnabled(false);


                    top = ContextCompat.getDrawable(this, R.drawable.ic_complete_100dp);
                    btn_driver_pickup_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

                    top = ContextCompat.getDrawable(this, R.drawable.ic_complete_100dp);
                    btn_driver_delivery_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

                    break;
                default:
                    Log.i(Constants.LOG_TAG, "Status is default");

            }


        }


    }

    @Override
    public void onLocationChanged(Location location) {

        // Triggered when the driver's location changes
        //Log.i(Constants.LOG_TAG, "Location Changed");

        if (getApplicationContext() != null) {

            mLastLocation = location;

            // get the latitude and longitude of the drivers current location
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            // use the db ref to create a geofire reference to the node
            GeoFire geoFireDriver = new GeoFire(mDriverLocationRef);

            Log.i(Constants.LOG_TAG, "Write geo location for firebase");
            // Write the drivers location to the geofire db
            geoFireDriver.setLocation(mDriver.getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));

            paintDriverLocationToMap();

            mFireAdapter.notifyDataSetChanged();

        }

    }


    private void removeDriverFromActive() {

        if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            GeoFire geoFire = new GeoFire(mDriverLocationRef);
            geoFire.removeLocation(userId);
        }


    }

    private void paintMarkersToMap() {
        //Log.i(Constants.LOG_TAG, "paintMarkersToMap");

        // Clear any existing markers
        mGoogleMap.clear();
        mLocationMarkers = new ArrayList<>();

        paintDriverLocationToMap();

        // Add Markers for pickup and delivery locations for this driver
        if (mOrdersList != null) {
            //Log.i(Constants.LOG_TAG, "we have orders");
            for (int i = 0; i < mOrdersList.size(); i++) {

                final Order order = mOrdersList.get(i);

                if (mShowPickupLocations) {

                    final int pickupIconColor;

                    switch (order.getStatus()) {
                        case Constants.ORDER_STATUS_ASSIGNED:
                            pickupIconColor = R.color.colorAccent;
                            break;
                        default:
                            pickupIconColor = R.color.colorGreen;
                            break;
                    }

                    final PendingResult<PlaceBuffer> pickupResult =
                            Places.GeoDataApi.getPlaceById(mClient, order.getPickupLocationId());

                    pickupResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceBuffer places) {

                            MarkerOptions markerOptions = new MarkerOptions();

                            markerOptions.position(places.get(0).getLatLng())
                                    .title(order.getCustomerName())
                                    .snippet(places.get(0).getAddress().toString())
                                    .infoWindowAnchor(0.5f, 0.5f)
                                    .icon(Utils_General.vectorToBitmap(Activity_Main.this, R.drawable.ic_place_accent_24dp,
                                            ContextCompat.getColor(Activity_Main.this, pickupIconColor)));

                            Marker marker = mGoogleMap.addMarker(markerOptions);

                            //Log.i(Constants.LOG_TAG, "add Marker to list");
                            mLocationMarkers.add(marker);
                            setMapCamera();
                        }
                    });
                }

                if (mShowDeliveryLocations) {

                    final int deliveryIconColor;

                    switch (order.getStatus()) {
                        case Constants.ORDER_STATUS_OPEN:
                        case Constants.ORDER_STATUS_BOOKED:
                        case Constants.ORDER_STATUS_ASSIGNED:
                        case Constants.ORDER_STATUS_PICKUP_COMPLETE:
                            deliveryIconColor = R.color.colorAccent;
                            break;
                        default:
                            deliveryIconColor = R.color.colorGreen;
                            break;
                    }

                    final PendingResult<PlaceBuffer> deliveryResult =
                            Places.GeoDataApi.getPlaceById(mClient, order.getDeliveryLocationId());

                    deliveryResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceBuffer places) {

                            MarkerOptions markerOptions = new MarkerOptions();

                            markerOptions.position(places.get(0).getLatLng())
                                    .title(order.getCustomerName())
                                    .snippet(places.get(0).getAddress().toString())
                                    .infoWindowAnchor(0.5f, 0.5f)
                                    .icon(Utils_General.vectorToBitmap(Activity_Main.this, R.drawable.ic_place_accent_24dp,
                                            ContextCompat.getColor(Activity_Main.this, deliveryIconColor)));

                            Marker marker = mGoogleMap.addMarker(markerOptions);

                            //Log.i(Constants.LOG_TAG, "add Marker to list");
                            mLocationMarkers.add(marker);
                            setMapCamera();
                        }
                    });
                }
            }


        }


    }

    private void paintDriverLocationToMap() {

        if (mDriverMarker != null) {
            mDriverMarker.remove();
        }


        if (mLastLocation != null) {

            // Add Marker for the driver
            LatLng driverLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(driverLatLng)
                    .title("me")
                    .snippet("")
                    .infoWindowAnchor(0.5f, 0.5f)
                    .icon(Utils_General.vectorToBitmap(this, R.drawable.ic_local_shipping_accent_24dp,
                            ContextCompat.getColor(this, R.color.colorPrimary)));

            mDriverMarker = mGoogleMap.addMarker(markerOptions);

        }

        setMapCamera();

    }

    private void setMapCamera() {

        //Log.i(Constants.LOG_TAG, "setMapCamera called");

        builder = new LatLngBounds.Builder();

        boolean hasPoint = false;

        if (mDriverMarker != null) {
            // add the driver to the lat lng bounds
            builder.include(mDriverMarker.getPosition());
            hasPoint = true;
        }

        if (mLocationMarkers != null) {
            //Log.i(Constants.LOG_TAG, "Markers are NOT null");

            for (int i = 0; i < mLocationMarkers.size(); i++) {

                final Marker marker = mLocationMarkers.get(i);

                //Log.i(Constants.LOG_TAG, "add marker to build");

                // add the driver to the lat lng bounds
                builder.include(marker.getPosition());
                hasPoint = true;

            }
        }

        if (hasPoint) {
            //mGoogleMap.setMaxZoomPreference(12);

            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
            mGoogleMap.animateCamera(cu);
        }


    }


}
