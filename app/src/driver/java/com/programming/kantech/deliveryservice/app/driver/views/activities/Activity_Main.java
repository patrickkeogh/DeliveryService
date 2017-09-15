package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;
import com.programming.kantech.deliveryservice.app.utils.Utils_Preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class Activity_Main extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static final String ANONYMOUS = "anonymous";
    private GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private LatLngBounds.Builder builder;

    private boolean mShowPickupOrders = true;
    private boolean mShowDeliveryOrders = true;

    private long mDisplayDateStartTimeInMillis;

    private Driver mDriver;

    private ArrayList<Order> mOrdersList = new ArrayList<>();

    private String mUsername;
    private ActionBar mActionBar;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.mapView)
    MapView mMapView;

    private MenuItem mMenu_previous;
    private MenuItem mMenu_next;


    private Query mOrdersQuery;
    private ValueEventListener mOrdersEventListener;
    private DatabaseReference mDriverRef;

    // Member variables for the Firebase Authorization
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(Constants.LOG_TAG, "onCreate() in Main Activity_Main");

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

        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);


        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FIREBASE_NOTIFICATION_TOPIC_DRIVER);

        mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);

        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // Signed In
                    Utils_General.showToast(Activity_Main.this, "You are now signed in");

                    onSignedInInitialize(user);


                } else {

                    // Not signed in
                    onSignedOutCleanup();

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .setTheme(R.style.LoginTheme)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                                    .build(),
                            Constants.REQUEST_CODE_SIGN_IN);
                }


            }
        };


        //checkIfAuthorized();

    }

    private void sendTokenToServer(FirebaseUser user) {

        String token = FirebaseInstanceId.getInstance().getToken();

        mDriverRef.child(user.getUid()).child("device").setValue(token);

        Utils_Preferences.saveHasTokenBeenSent(getApplicationContext(), true);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Get a ref to the forward and backward menu buttons
        mMenu_previous = menu.getItem(0);
        mMenu_next = menu.getItem(1);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        Log.i(Constants.LOG_TAG, "onPrepareOptionsMenu called");

        Calendar currentDate = Utils_General.getTodayAtStartTime();

        //Log.i(Constants.LOG_TAG, "CurrentDate:" + currentDate.getTimeInMillis());
        //Log.i(Constants.LOG_TAG, "DisplayDate:" + mDisplayDateStartTime.getTimeInMillis());

        if(mDisplayDateStartTimeInMillis != currentDate.getTimeInMillis()){
            mMenu_previous.setVisible(true);
        }else{
            mMenu_previous.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_driver_previous_day:

                mDisplayDateStartTimeInMillis -= Constants.DAY;

                String yesterday = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);

                if (mActionBar != null) {
                    mActionBar.setTitle(yesterday);
                }

                invalidateOptionsMenu();

                detachFirebaseOrderListeners();
                attachOrderReadListener();

                return true;
            case R.id.action_driver_next_day:

                mDisplayDateStartTimeInMillis += Constants.DAY;

                String tomorrow = Utils_General.getFormattedLongDateStringFromLongDate(mDisplayDateStartTimeInMillis);

                if (mActionBar != null) {
                    mActionBar.setTitle(tomorrow);
                }

                // Prepare the menu
                invalidateOptionsMenu();

                detachFirebaseOrderListeners();
                attachOrderReadListener();

                return true;
            case R.id.action_driver_orders:
                Intent intent_orders = new Intent(this, Activity_ShowOrders.class);
                intent_orders.putExtra(Constants.EXTRA_DRIVER, mDriver);
                startActivity(intent_orders);
                return true;
            case R.id.action_driver_photo:
                Intent intent_photo = new Intent(this, Activity_Photo.class);
                intent_photo.putExtra(Constants.EXTRA_DRIVER, mDriver);
                startActivity(intent_photo);
                return true;
            case R.id.action_sign_out:
                removeDriverFromActive();
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();

        if (mFirebaseAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthListener);
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;

        // Remove the driver from the active driver node


    }

    private void onSignedInInitialize(final FirebaseUser user) {

        mUsername = user.getDisplayName();

        DatabaseReference driverRef = mDriverRef.child(user.getUid());

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOG_TAG, "onDataChange()called for driver");

                if (dataSnapshot.exists()) {
                    // run some code

                    mDriver = dataSnapshot.getValue(Driver.class);
                    Log.i(Constants.LOG_TAG, "The driver is in the db:" + mDriver.toString());

                    if (!mDriver.getDriverApproved()) {
                        checkIfAuthorized(user);
                    } else {

                        buildGoogleApiClients();
                    }

                } else {
                    Log.i(Constants.LOG_TAG, "The driver is not in the database");
                    // User is not in the driver db. add them
                    mDriver = new Driver(user.getUid(), user.getDisplayName(), user.getEmail(),"", false, false, "");

                    mDriverRef.child(user.getUid()).setValue(mDriver);
                    //mDriverDBReference.push().setValue(driver);

                    checkIfAuthorized(user);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkIfAuthorized(FirebaseUser user) {
        //if (!Utils_Preferences.getHasTokenBeenSent(getApplicationContext()))
            sendTokenToServer(user);

        Log.i(Constants.LOG_TAG, "checkIfAuthorized() in Driver has been called");

        Intent intent = new Intent(Activity_Main.this, Activity_NewDriverRequest.class);
        startActivity(intent);
        finish();

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.i(Constants.LOG_TAG, "Location Changed");

        if (getApplicationContext() != null) {
            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            // Get the drivers id
            String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference driverAvailable = FirebaseDatabase.getInstance().getReference("driver_location");
            GeoFire geoFireDriver = new GeoFire(driverAvailable);

            Log.i(Constants.LOG_TAG, "Write geo location for firebase");
            geoFireDriver.setLocation(driverId, new GeoLocation(location.getLatitude(), location.getLongitude()));

            paintMarkers();


        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Constants.LOG_TAG, "onConnected()");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Activity_Main.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_LOCATION_PERMISSION);
        }

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

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        paintMarkers();

        attachOrderReadListener();

    }

    private void attachOrderReadListener() {

        //long queryStartTime = mDisplayDateStartTime.getTimeInMillis();

//        mDisplayDateStartTime.add(Calendar.HOUR_OF_DAY, 24);
//        mDisplayDateStartTime.add(Calendar.SECOND, -1);
//        long queryEndTime = mDisplayDateStartTime.getTimeInMillis();
//
//        //Reset display Date
//        mDisplayDateStartTime.add(Calendar.HOUR_OF_DAY, -24);
//        mDisplayDateStartTime.add(Calendar.SECOND, 1);


        Log.i(Constants.LOG_TAG, "QueryStartTime:" + mDisplayDateStartTimeInMillis);
        //Log.i(Constants.LOG_TAG, "QueryEndTime:  " + queryEndTime);

        //Utils_General.getStartTimeForDate(mSelectedOrder.getPickupDate()



        String strQuery = "true_" + mDisplayDateStartTimeInMillis + "_" + mDriver.getUid();

        Log.i(Constants.LOG_TAG, "strQuery:  " + strQuery);


        mOrdersQuery = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_ORDERS)
                .orderByChild("inProgressDateDriverId").equalTo(strQuery);

        if (mOrdersEventListener == null) {

            mOrdersEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(Constants.LOG_TAG, "onDataChange in attachOrderListener");
                    mOrdersList = new ArrayList<>();

                    if (dataSnapshot != null) {
                        Log.i(Constants.LOG_TAG, "wqe have a snapshot");

                        // Get each order that is in progress
                        for (DataSnapshot orders : dataSnapshot.getChildren()) {

                            Order order = orders.getValue(Order.class);
                            Log.e(Constants.LOG_TAG, "ORDERS:" + order.getCustomerName());

                            mOrdersList.add(order);
                        }
                    }

                    paintMarkers();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mOrdersQuery.addValueEventListener(mOrdersEventListener);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Constants.LOG_TAG, "onConnectionSuspended()");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(Constants.LOG_TAG, "onConnectionSuspended()");

    }

    protected synchronized void buildGoogleApiClients() {
        Log.i(Constants.LOG_TAG, "buildGoogleApiClient()");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
    }

    private void removeDriverFromActive() {

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_NODE_DRIVER_LOCATIONS);

            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        removeDriverFromActive();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeDriverFromActive();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(Constants.LOG_TAG, "onMapReady() called in Driver");

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_LOCATION_PERMISSION);
        }

        // TODO: Add permssion request for ACCESS_COARSE_LOCATION
        mGoogleMap = googleMap;
        mGoogleMap.setPadding(100, 100, 100, 100);

        //mGoogleMap.setMyLocationEnabled(true);

        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);


    }

    private void paintMarkers() {

        // Clear any existing markers
        mGoogleMap.clear();
        builder = new LatLngBounds.Builder();

        // Add Marker for home office
        LatLng home = new LatLng(44.3916, -79.6882);

        drawMarker(home, "HOME", "Kan-Tech Delivery Service",
                Utils_General.vectorToBitmap(this, R.drawable.ic_menu_home,
                        ContextCompat.getColor(this, R.color.colorAccent)));

        if (mLastLocation != null) {

            // Add Marker for the driver
            LatLng driver = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            drawMarker(driver, "ME", "",
                    Utils_General.vectorToBitmap(this, R.drawable.ic_local_shipping_accent_24dp,
                            ContextCompat.getColor(this, R.color.colorPrimary)));

        }

        // Add Markers for pickup and delivery locations for this driver
        if (mOrdersList != null) {
            for (int i = 0; i < mOrdersList.size(); i++) {

                final Order order = mOrdersList.get(i);

                if (mShowPickupOrders) {

                    final PendingResult<PlaceBuffer> pickupResult =
                            Places.GeoDataApi.getPlaceById(mGoogleApiClient, order.getPickupLocationId());

                    pickupResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceBuffer places) {

                            drawMarker(places.get(0).getLatLng(), order.getCustomerName(), places.get(0).getAddress().toString(),
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        }
                    });
                }

                if (mShowDeliveryOrders) {
                    final PendingResult<PlaceBuffer> deliveryResult =
                            Places.GeoDataApi.getPlaceById(mGoogleApiClient, order.getDeliveryLocationId());

                    deliveryResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceBuffer places) {

                            drawMarker(places.get(0).getLatLng(), order.getCustomerName(), places.get(0).getAddress().toString(),
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        }
                    });
                }


            }
        }


    }

    private void drawMarker(LatLng point, String title, String snip, BitmapDescriptor bitmapDescriptor) {
        Log.i(Constants.LOG_TAG, "drawMarker()");

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point)
                .title(title)
                .snippet(snip)
                .infoWindowAnchor(0.5f, 0.5f)
                .icon(bitmapDescriptor);

        mGoogleMap.addMarker(markerOptions);

        builder.include(markerOptions.getPosition());

        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
        mGoogleMap.animateCamera(cu);

    }

    @OnCheckedChanged({R.id.rb_driver_all, R.id.rb_driver_pickups, R.id.rb_driver_deliveries})
    public void onRadioButtonCheckChanged(CompoundButton button, boolean checked) {
        if (checked) {
            switch (button.getId()) {
                case R.id.rb_driver_all:
                    mShowDeliveryOrders = true;
                    mShowPickupOrders = true;
                    break;
                case R.id.rb_driver_pickups:
                    mShowDeliveryOrders = false;
                    mShowPickupOrders = true;
                    break;
                case R.id.rb_driver_deliveries:
                    mShowDeliveryOrders = true;
                    mShowPickupOrders = false;
                    break;
            }

            paintMarkers();
        }
    }

    private void detachFirebaseOrderListeners() {

        if (mOrdersEventListener != null) {
            mOrdersQuery.removeEventListener(mOrdersEventListener);
            mOrdersEventListener = null;
        }
    }


}
