package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-11-09.
 * Activity used to show users where their driver is once their package has been picked up
 */

public class Activity_MyMap extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mClient;
    private GoogleMap mGoogleMap;
    private Order mOrder;
    private Marker mDriverMarker;
    private Marker mPickupMarker;
    private Marker mDeliveryMarker;

    private LatLngBounds.Builder builder;

    private DatabaseReference mDriverRef;
    private DatabaseReference mOrderRef;
    private ChildEventListener mDriverListener;

    private ValueEventListener mOrderListener;

    // View to bind
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.mapView)
    MapView mMapView;

    @BindView(R.id.empty_map)
    TextView mEmpty_map;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_map);

        ButterKnife.bind(this);

        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_ORDER)) {
                mOrder = savedInstanceState.getParcelable(Constants.STATE_INFO_ORDER);
            }
        } else {
            mOrder = getIntent().getParcelableExtra(Constants.EXTRA_ORDER);
        }

        if (mOrder == null) {
            throw new IllegalArgumentException("Must pass EXTRA_ORDER");
        }

        mDriverRef = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_DRIVER_LOCATIONS).child(mOrder.getDriverId());

        mOrderRef = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_ORDERS).child(mOrder.getId());

        Log.i(Constants.LOG_TAG, "DriverRef:" + mDriverRef.toString());

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(R.string.title_order_tracker);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mClient != null) {
            mClient.disconnect();
        }

        detachFirebaseDriverListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mClient = null;
    }

    /**
     * Save the order object in the state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the driver in the instance state
        outState.putParcelable(Constants.STATE_INFO_ORDER, mOrder);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(Constants.LOG_TAG, "onMapReady()");

        // TODO: Add permssion request for ACCESS_COARSE_LOCATION
        mGoogleMap = googleMap;

        mGoogleMap.setPadding(60, 60, 60, 60);

        // make sure we have the correct permissions
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_LOCATION_PERMISSION);
        }

        if (mClient == null) {
            buildApiClient();
            mClient.connect();
        } else {
            if (!mClient.isConnected()) {
                mClient.connect();
            }
        }

        //attachDriverReadListener();
        //attachOrderReadListener();


    }

    private void detachFirebaseDriverListeners() {
        if (mDriverListener != null) {
            mDriverRef.removeEventListener(mDriverListener);
            mDriverListener = null;
        }
    }

    private void attachDriverReadListener() {

        if (mDriverListener == null) {

            mDriverListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    final Double strLat = (Double) dataSnapshot.child("l").child("0").getValue();
                    final Double strLng = (Double) dataSnapshot.child("l").child("1").getValue();

                    if (strLat != null && strLng != null) {
                        updateDriverMarker(new LatLng(strLat, strLng));
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    final Double strLat = (Double) dataSnapshot.child("0").getValue();
                    final Double strLng = (Double) dataSnapshot.child("1").getValue();

                    if (strLat != null && strLng != null) {
                        updateDriverMarker(new LatLng(strLat, strLng));
                    }

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){

                        mDriverMarker.remove();
                        mDriverMarker = null;
                        updateCamera();
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mDriverRef.addChildEventListener(mDriverListener);
        }
    }

    private void attachOrderReadListener() {

        if (mOrderListener == null) {

            mOrderListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    mOrder = dataSnapshot.getValue(Order.class);
                    updateOrderMarkers();


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mOrderRef.addValueEventListener(mOrderListener);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Constants.LOG_TAG, "onConnected");

        attachDriverReadListener();
        attachOrderReadListener();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

    private void updateDriverMarker(LatLng point) {
        Log.i(Constants.LOG_TAG, "updateDriverMarker()");

        // This can be changed to a move position
        if (mDriverMarker != null)
            mDriverMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point)
                .title(mOrder.getDriverName())
                .snippet(mOrder.getStatus())
                .infoWindowAnchor(0.5f, 0.5f)
                .icon(Utils_General.vectorToBitmap(this, R.drawable.ic_local_shipping_accent_24dp,
                        ContextCompat.getColor(this, R.color.colorPrimary)));

        mDriverMarker = mGoogleMap.addMarker(markerOptions);

        updateCamera();

    }

    private void updateOrderMarkers() {
        Log.i(Constants.LOG_TAG, "updateOrderMarker()");


        if (mPickupMarker != null)
            mPickupMarker.remove();

        if (mDriverMarker != null)
            mDriverMarker.remove();

        // get the order pickup address
        final PendingResult<PlaceBuffer> pickupResult =
                Places.GeoDataApi.getPlaceById(mClient, mOrder.getPickupLocationId());

        pickupResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {


                Log.i(Constants.LOG_TAG, "Pickup:" + places.get(0).getAddress().toString());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(places.get(0).getLatLng())
                        .title("Pickup Location")
                        .snippet(places.get(0).getAddress().toString())
                        .infoWindowAnchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.defaultMarker
                                (Utils_General.getMarkerColorByStatus(
                                        Constants.ORDER_MARKER_LOCATION_TYPE_PICKUP, mOrder.getStatus())));


                mPickupMarker = mGoogleMap.addMarker(markerOptions);
                Log.i(Constants.LOG_TAG, "Pickup marker created:" + mPickupMarker.toString());

                updateCamera();

            }
        });

        // get the order pickup address
        final PendingResult<PlaceBuffer> deliveryResult =
                Places.GeoDataApi.getPlaceById(mClient, mOrder.getDeliveryLocationId());

        deliveryResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(places.get(0).getLatLng())
                        .title("Delivery Location")
                        .snippet(places.get(0).getAddress().toString())
                        .infoWindowAnchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.defaultMarker
                                (Utils_General.getMarkerColorByStatus(
                                        Constants.ORDER_MARKER_LOCATION_TYPE_DELIVERY, mOrder.getStatus())));


                mDeliveryMarker = mGoogleMap.addMarker(markerOptions);

                updateCamera();

            }
        });

    }

    private void updateCamera() {

        boolean hasPoint = false;

        builder = new LatLngBounds.Builder();

        if(mDriverMarker != null){
            builder.include(mDriverMarker.getPosition());
            hasPoint = true;
        }

//        if(mPickupMarker != null){
//            builder.include(mPickupMarker.getPosition());
//            hasPoint = true;
//        }

        if(mDeliveryMarker != null){
            builder.include(mDeliveryMarker.getPosition());
            hasPoint = true;
        }

        mGoogleMap.setMaxZoomPreference(12);

        mGoogleMap.setMinZoomPreference(8);

        Log.i(Constants.LOG_TAG, "has point:" + hasPoint);

        if (hasPoint) {

            mMapView.setVisibility(View.VISIBLE);
            mEmpty_map.setVisibility(View.GONE);

            mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    Log.i(Constants.LOG_TAG, "mover camera");
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
                    mGoogleMap.animateCamera(cu);

                }
            });
        }else{
            mMapView.setVisibility(View.GONE);
            mEmpty_map.setVisibility(View.VISIBLE);
        }

    }
}
