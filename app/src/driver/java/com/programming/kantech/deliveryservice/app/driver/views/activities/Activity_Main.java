package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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

import java.util.ArrayList;
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
 * Created by patrick keogh on 2017-10-04.
 * Main activity, displays a map showing drivers and destinations
 */

public class Activity_Main extends AppCompatActivity implements
        com.google.android.gms.location.LocationListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Local member variables
    private Driver mDriver;
    private ActionBar mActionBar;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mClient;
    private LatLngBounds.Builder builder;
    private Order mSelectedOrder;
    private FirebaseRecyclerAdapter<Order, ViewHolder_Order> mFireAdapter;

    // Get a ref to our api service that will fetch distances between 2 places
    private ApiInterface apiService;

    // Determines if the filter view should be visible on the screen
    private boolean mShowFilter = true;

    private boolean useCameraAnimation = false;

    // Marker for the driver
    private Marker mDriverMarker;

    // Location member variables
    Location mLastLocation;
    LocationRequest mLocationRequest;

    // The orders returned by the query
    private ArrayList<Order> mOrdersList = new ArrayList<>();

    // List of Markers for all order destinations, pickup and delivery
    private ArrayList<Marker> mLocationMarkers = new ArrayList<>();

    // booleans used to filter by order status
    //private boolean mShowOpen = true;
    //private boolean mShowComplete = true;
    private boolean mShowDeliveryLocations = true;
    private boolean mShowPickupLocations = true;

    // 12:00 AM of the selected day
    private long mDisplayDateStartTimeInMillis;
    private long mTodayDateStartTimeInMillis;

    // Firebase member variables
    //private DatabaseReference mDriverRef;
    private DatabaseReference mOrdersRef;
    private DatabaseReference mDriverLocationRef;
    private Query mOrdersQuery;

    // Event listener to list for new or updated orders
    private ValueEventListener mOrdersEventListener;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.mapView)
    MapView mMapView;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;

    @BindView(R.id.rv_driver_orders)
    RecyclerView mRecyclerView;

    @BindView(R.id.layout_map_filter)
    LinearLayout layout_map_filter;

    @BindView(R.id.tv_order_filter_date)
    TextView tv_order_filter_date;

    @BindView(R.id.btn_driver_pickup_complete)
    AppCompatButton btn_driver_pickup_complete;

    @BindView(R.id.btn_driver_delivery_complete)
    AppCompatButton btn_driver_delivery_complete;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_DRIVER)) {
                mDriver = savedInstanceState.getParcelable(Constants.STATE_INFO_DRIVER);
            }
        } else {

            mDriver = getIntent().getParcelableExtra(Constants.EXTRA_DRIVER);
        }

        if (mDriver == null) {
            throw new IllegalArgumentException("Must pass EXTRA_DRIVER");
        }

        // use ButterKnife to inject the layout views
        ButterKnife.bind(this);

        // Set the distance api service
        apiService = ApiClient.getClient().create(ApiInterface.class);

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Create the display date, today at 00:00 hrs
        Calendar date = new GregorianCalendar();
        mDisplayDateStartTimeInMillis = Utils_General.getStartTimeForDate(date.getTimeInMillis());
        mTodayDateStartTimeInMillis = Utils_General.getStartTimeForDate(date.getTimeInMillis());


        //Log.i(Constants.LOG_TAG, "Start Time for today in Millis:" + mDisplayDateStartTimeInMillis);

        String showDate = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);

        tv_order_filter_date.setText(showDate);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle(showDate);
        }

        // Setup the Navigation Drawer
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
        //mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);

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

        showOrHideFilterView();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store thr driver object in state
        outState.putParcelable(Constants.STATE_INFO_DRIVER, mDriver);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_driver_orders:
                Intent intent_orders = new Intent(this, Activity_ShowOrders.class);
                intent_orders.putExtra(Constants.EXTRA_DRIVER, mDriver);
                startActivity(intent_orders);
                return true;
            case R.id.action_driver_filter:

                showOrHideFilterView();

                return true;
            case R.id.action_sign_out:
                //Utils_General.showToast(this, "Signout called");
                removeDriverFromActive();
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

    private void showOrHideFilterView() {

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

    private void loadFirebaseAdapter() {

        // Make sure we are starting with an fresh FirebaseAdapter
        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }

        // Create the adapter and add it too the recyclerview
        mFireAdapter = new FirebaseRecyclerAdapter<Order, ViewHolder_Order>(
                Order.class,
                R.layout.item_driver_order,
                ViewHolder_Order.class,
                getDatabaseQueryForList()) {
            @Override
            public ViewHolder_Order onCreateViewHolder(ViewGroup parent, int viewType) {
                return super.onCreateViewHolder(parent, viewType);
            }

            @Override
            public void populateViewHolder(final ViewHolder_Order holder, final Order order, int position) {
                //Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + order.getCustomerName());

                // only show distances if the order is not complete
                if (!Objects.equals(order.getStatus(), Constants.ORDER_STATUS_COMPLETE)) {


                    // make sure we have the drivers location
                    if (mLastLocation != null) {

                        // create origin string for the driver using lat, long coordinates
                        String origin = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();

                        // create destination string for a location using a google PlaceId
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
                                            //Log.i(Constants.LOG_TAG, "Routes:" + routes.toString());
                                            Route route = routes.get(0);

                                            List<Leg> legs = route.getLegs();

                                            Leg trip = legs.get(0);

                                            Distance distance = trip.getDistance();

                                            //Log.i(Constants.LOG_TAG, "Response Distance:" + distance.getText());

                                            // if we are less than half 500 metres, notify driver we have arrived
                                            if (distance.getValue() < 500) {
                                                holder.setDistance("0 km");

                                            } else {
                                                holder.setDistance(distance.getText());
                                            }

                                        }


                                    }


                                } else {
                                    Log.i(Constants.LOG_TAG, "A distance could not be fetched");
                                    holder.setDistance("Unknown");
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Results> call, @NonNull Throwable t) {
                                // Log error here since request failed
                                Log.e(Constants.LOG_TAG, t.toString());
                                holder.setDistance("Unknown");
                            }
                        });
                    }


                } else {
                    // make sure we are not holding a prior distance string
                    holder.setDistance("");
                }


                holder.setCustomerName(order.getCustomerName());
                holder.setOrderStatus(order.getStatus());

                // change the background color of the list item depending on the status
                switch (order.getStatus()) {
                    case Constants.ORDER_STATUS_BOOKED:
                    case Constants.ORDER_STATUS_ASSIGNED:
                        holder.setBackgroundColor(Activity_Main.this, R.color.colorPrimary);
                        break;
                    case Constants.ORDER_STATUS_PICKUP_COMPLETE:
                        holder.setBackgroundColor(Activity_Main.this, R.color.colorGreen);
                        break;
                    case Constants.ORDER_STATUS_COMPLETE:
                        holder.setBackgroundColor(Activity_Main.this, R.color.colorAccent);
                        break;

                }

                // change foreground color of the list item if it is the selected item
                if (mSelectedOrder != null) {
                    if (Objects.equals(order.getId(), mSelectedOrder.getId())) {
                        holder.setSelectedColor(Activity_Main.this, R.color.colorPrimaryLight);
                    } else {
                        holder.setSelectedColor(Activity_Main.this, R.color.colorWhite);
                    }
                }

                // fetch the pickup address from google.places
                final Place[] thisPlace = new Place[1];
                PendingResult<PlaceBuffer> result_pickup = Places.GeoDataApi.getPlaceById(mClient, order.getPickupLocationId());

                result_pickup.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        thisPlace[0] = places.get(0);
                        holder.setPickupAddress(thisPlace[0].getAddress().toString());


                    }
                });

                // fetch the delivery address from google.places
                PendingResult<PlaceBuffer> result_delivery = Places.GeoDataApi.getPlaceById(mClient, order.getDeliveryLocationId());

                result_delivery.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        thisPlace[0] = places.get(0);
                        holder.setDeliveryAddress(thisPlace[0].getAddress().toString());
                    }
                });

                // set the onclick listener for when a order is selected from the list
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

    private void paintDriverLocationToMap() {

        if (mDriverMarker != null) {
            mDriverMarker.remove();
        }

        // We only want to show the driver if the view is on today
        if (mTodayDateStartTimeInMillis == mDisplayDateStartTimeInMillis) {

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


    }

    private void paintMarkersToMap() {
        //Log.i(Constants.LOG_TAG, "paintMarkersToMap");

        // Clear any existing markers
        mGoogleMap.clear();
        mLocationMarkers = new ArrayList<>();

        // Show the driver marker
        paintDriverLocationToMap();

        // Add Markers for pickup and delivery locations for this driver
        if (mOrdersList != null) {
            //Log.i(Constants.LOG_TAG, "we have orders");
            for (int i = 0; i < mOrdersList.size(); i++) {

                final Order order = mOrdersList.get(i);

                if (mShowPickupLocations) {

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
                                    .icon(BitmapDescriptorFactory.defaultMarker(Utils_General.getMarkerColorByStatus(Constants.ORDER_MARKER_LOCATION_TYPE_PICKUP, order.getStatus())));

                            Marker marker = mGoogleMap.addMarker(markerOptions);

                            //Log.i(Constants.LOG_TAG, "add Marker to list");
                            mLocationMarkers.add(marker);
                            setMapCamera();
                        }
                    });
                }

                if (mShowDeliveryLocations) {

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
                                    .icon(BitmapDescriptorFactory.defaultMarker(Utils_General.getMarkerColorByStatus(Constants.ORDER_MARKER_LOCATION_TYPE_DELIVERY, order.getStatus())));

                            Marker marker = mGoogleMap.addMarker(markerOptions);

                            mLocationMarkers.add(marker);
                            setMapCamera();
                        }
                    });
                }
            }


        }


    }

    private void setMapCamera() {

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

            mGoogleMap.setMaxZoomPreference(12);

            mGoogleMap.setMinZoomPreference(8);

            mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);

                    if(useCameraAnimation){
                        mGoogleMap.animateCamera(cu);
                    }else{
                        mGoogleMap.moveCamera(cu);
                        // we want to skip the animation the first time through
                        useCameraAnimation = true;
                    }


                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();

        if (mClient != null) {
            mClient.disconnect();
        }
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
    public void onStart() {
        super.onStart();

        if (mClient == null) {
            buildGoogleApiClients();
            mClient.connect();
        } else {
            if (!mClient.isConnected()) {
                mClient.connect();
            }
        }
    }

    @Override
    protected void onStop() {
        //Log.i(Constants.LOG_TAG, "onstop Called");
        super.onStop();
        removeDriverFromActive();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        // Triggered when the driver's location changes
        //Log.i(Constants.LOG_TAG, "Location Changed");

        if (getApplicationContext() != null) {

            mLastLocation = location;

            // get the latitude and longitude of the drivers current location
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            // use the db ref to create a geofire reference to the node
            GeoFire geoFireDriver = new GeoFire(mDriverLocationRef);

            //Log.i(Constants.LOG_TAG, "Write geo location for firebase");
            // Write the drivers location to the geofire db
            geoFireDriver.setLocation(mDriver.getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));

            paintDriverLocationToMap();

            mFireAdapter.notifyDataSetChanged();

        }

    }

    private void removeDriverFromActive() {

        // Runs on sign out or app goes to the background

        if (mClient != null && mClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            GeoFire geoFire = new GeoFire(mDriverLocationRef);
            geoFire.removeLocation(userId);
        }

    }

    private Query getDatabaseQueryForList() {
        String strQueryStart = mDisplayDateStartTimeInMillis + "_" + mDriver.getUid() + Constants.FIREBASE_STATUS_SORT_PICKUP_COMPLETE;
        String strQueryEnd = mDisplayDateStartTimeInMillis + "_" + mDriver.getUid() + Constants.FIREBASE_STATUS_SORT_COMPLETE;
        return mOrdersRef.orderByChild("queryDateDriverId").startAt(strQueryStart).endAt(strQueryEnd);
    }

    private Query getDatabaseQueryForMap() {
        String strQuery = "true_" + mDisplayDateStartTimeInMillis + "_" + mDriver.getUid();
        return mOrdersRef.orderByChild("inProgressDateDriverId").equalTo(strQuery);
    }

    private void attachOrderReadListener() {

        // Get all orders for the selected date and driver
        mOrdersQuery = getDatabaseQueryForMap();

        if (mOrdersEventListener == null) {

            mOrdersEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Log.i(Constants.LOG_TAG, "onDataChange in attachOrderListener");


                    // Create a list of returned orders
                    ArrayList<Order> newOrderList = new ArrayList<>();

                    if (dataSnapshot != null) {
                        //Log.i(Constants.LOG_TAG, "we have an order snapshot");

                        // Get each order that is in progress for the selected date and driver
                        for (DataSnapshot orders : dataSnapshot.getChildren()) {

                            Order order = orders.getValue(Order.class);
                            //Log.i(Constants.LOG_TAG, "ORDERS:" + order.getCustomerName());

                            newOrderList.add(order);
                        }
                    }

//                    if (newOrderList.size() > mOrdersList.size()) {
//                        // Should be a new order, make a sound
//                        final MediaPlayer mp = MediaPlayer.create(Activity_Main.this, R.raw.alarm_rooster);
//                        //mp.start();
//                    }

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
        //Log.i(Constants.LOG_TAG, "setDrawerButtons called");

        Drawable top;

        if (mSelectedOrder == null) {
            top = ContextCompat.getDrawable(this, R.drawable.ic_info_outline_100dp);
            btn_driver_pickup_complete.setEnabled(false);
            btn_driver_pickup_complete.setText(R.string.btn_select_order);
            btn_driver_pickup_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

            btn_driver_delivery_complete.setEnabled(false);
            btn_driver_delivery_complete.setText(R.string.btn_select_order);
            btn_driver_delivery_complete.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
        } else {
            //Log.i(Constants.LOG_TAG, "Order is not null");

            btn_driver_pickup_complete.setText(R.string.btn_pickup_complete);
            btn_driver_delivery_complete.setText(R.string.btn_delivery_complete);

            Log.i(Constants.LOG_TAG, "Status:" + mSelectedOrder.getStatus());

            switch (mSelectedOrder.getStatus()) {
                case Constants.ORDER_STATUS_ASSIGNED:
                    //Log.i(Constants.LOG_TAG, "Status is assigned");
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
                    //Log.i(Constants.LOG_TAG, "Status is default");

            }


        }


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
    public void onPickupLocationsSelected(CompoundButton button, boolean checked) {
        mShowPickupLocations = checked;
        paintMarkersToMap();
    }

    @OnCheckedChanged(R.id.cb_driver_delivery_locations)
    public void onDeliveryLocationsSelected(CompoundButton button, boolean checked) {
        mShowDeliveryLocations = checked;
        paintMarkersToMap();
    }

//    @OnCheckedChanged({R.id.rb_driver_all, R.id.rb_driver_open, R.id.rb_driver_complete})
//    public void onRadioButtonCheckChanged(CompoundButton button, boolean checked) {
//        if (checked) {
//            switch (button.getId()) {
//                case R.id.rb_driver_all:
//                    mShowOpen = true;
//                    mShowComplete = true;
//                    break;
//                case R.id.rb_driver_open:
//                    mShowOpen = true;
//                    mShowComplete = false;
//                    break;
//                case R.id.rb_driver_complete:
//                    mShowOpen = true;
//                    mShowComplete = false;
//                    break;
//            }
//            paintMarkersToMap();
//        }
//    }

    private void showAlertMessage(String title, String company, String address,
                                  final String cancelText, final String status) {
        //Log.i(Constants.LOG_TAG, "Alert Message called");

        final ViewGroup nullParent = null;

        LayoutInflater inflater = this.getLayoutInflater();
        View dialog_confirm = inflater.inflate(R.layout.alert_confirm_location_complete, nullParent);

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

                        // Update the order status, it has been confirmed
                        mSelectedOrder.setStatus(status);

                        if (Objects.equals(status, Constants.ORDER_STATUS_COMPLETE)) {

                            mSelectedOrder.setInProgress(false);
                            mSelectedOrder.setInProgressDriverId("false_" + mDriver.getUid());

                            mSelectedOrder.setQueryDateDriverId(mSelectedOrder.getPickupDate() + "_" + mDriver.getUid() + Constants.FIREBASE_STATUS_SORT_COMPLETE);

                            // Construct query string for inprogress_date_driver
                            String strQuery = "false_" +
                                    mSelectedOrder.getPickupDate()
                                    + "_" + mDriver.getUid();

                            mSelectedOrder.setInProgressDateDriverId(strQuery);

                        } else {

                            mSelectedOrder.setQueryDateDriverId(mSelectedOrder.getPickupDate() + "_" +
                                    mDriver.getUid() + Constants.FIREBASE_STATUS_SORT_PICKUP_COMPLETE);
                        }


                        // Save the order to firebase
                        mOrdersRef.child(mSelectedOrder.getId()).setValue(mSelectedOrder);

                        detachFirebaseOrderListeners();
                        attachOrderReadListener();

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
}
