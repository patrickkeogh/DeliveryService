package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick keogh on 2017-09-01.
 * Main screen for this app.  Will show a map of driver and orders for a given date
 */

public class Fragment_MainDetails extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Local member variables
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder builder;

    private ArrayList<Driver> mDriversList = new ArrayList<>();
    private ArrayList<Marker> mDriverMarkers = new ArrayList<>();

    private ArrayList<Order> mOrdersList = new ArrayList<>();
    private ArrayList<Marker> mOrderMarkers = new ArrayList<>();

    // 12:00 AM of the selected day
    private long mDisplayDateStartTimeInMillis;

    // Firebase member variables
    private DatabaseReference mActiveDriverLocationRef;
    private ChildEventListener mActiveDriversEventListener;

    private Query mOrdersQuery;
    private DatabaseReference mOrdersRef;
    private ValueEventListener mOrdersEventListener;

    // Fragement views
    @BindView(R.id.mapView)
    MapView mMapView;

    // interface MainDetailsFragmentListener that triggers a callback in the host activity
    MainDetailsFragmentListener mCallback;

    public Fragment_MainDetails() {
    }

    // MainDetailsFragmentListener interface, calls a method in the host activity
    // depending on the order selected in the master list
    public interface MainDetailsFragmentListener {
        void onFragmentLoaded(String tag);
    }

    /**
     * Static factory method that takes a datetime object parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_MainDetails newInstance(long date_in) {
        Fragment_MainDetails f = new Fragment_MainDetails();
        Bundle args = new Bundle();

        // Add any required arguments for start up - None needed right now
        args.putLong(Constants.EXTRA_DATE_FILTER, date_in);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_DATE_FILTER)) {
                mDisplayDateStartTimeInMillis = savedInstanceState.getLong(Constants.STATE_INFO_DATE_FILTER);
            }
        } else {
            mDisplayDateStartTimeInMillis = getArguments().getLong(Constants.EXTRA_DATE_FILTER);
        }

        if (mDisplayDateStartTimeInMillis == 0) {
            throw new IllegalArgumentException("Must pass EXTRA_DATE_FILTER");
        }

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_main_details, container, false);

        ButterKnife.bind(this, rootView);

        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        // Get a root reference to the orders table
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        mActiveDriverLocationRef = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_DRIVER_LOCATIONS);

        return rootView;

    }

    /**
     * Save the current state of this fragment
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the driver in the instance state
        outState.putLong(Constants.STATE_INFO_DATE_FILTER, mDisplayDateStartTimeInMillis);
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_MainDetails.MainDetailsFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MainDetailsFragmentListener");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // TODO: Add permssion request for ACCESS_COARSE_LOCATION
        mGoogleMap = googleMap;

        mGoogleMap.setPadding(60, 60, 60, 60);

//        if (ActivityCompat.checkSelfPermission(getContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
//                        PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(getActivity(), new String[]{
//                    android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_LOCATION_PERMISSION);
//        }

        buildGoogleApiClient();
        //mGoogleMap.setMyLocationEnabled(true);

        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

//        // Show all active drivers
//        boolean mShowDrivers = true;
//        if (mShowDrivers) {
//            attachDriverReadListener();
//        }
//
//        // Get all orders not completed
//        if (mShowOrders) {
//            attachOrderReadListener();
//        }
    }

    private void paintHomeMarker() {

        // Clear any existing markers
//        mGoogleMap.clear();
//        builder = new LatLngBounds.Builder();

        // Add Marker for home office
        // This will have to be downloaded
        // This only hard codes for testing purposes
//        LatLng home = new LatLng(44.3916, -79.6882);
//        drawMarker(home, "HOME", "Kan-Tech Delivery Service",
//                Utils_General.vectorToBitmap(getContext(), R.drawable.ic_menu_home,
//                        ContextCompat.getColor(getContext(), R.color.colorAccent)));

    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Notify the activity that this fragment was loaded
        mCallback.onFragmentLoaded(Constants.TAG_FRAGMENT_MAIN_DETAILS);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        attachDriverReadListener();
        attachOrderReadListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        detachFirebaseDriverListeners();
        detachFirebaseOrderListeners();
    }

    private void detachFirebaseDriverListeners() {
        if (mActiveDriversEventListener != null) {
            mActiveDriverLocationRef.removeEventListener(mActiveDriversEventListener);
            mActiveDriversEventListener = null;
        }
    }

    private void detachFirebaseOrderListeners() {

        if (mOrdersEventListener != null) {
            mOrdersQuery.removeEventListener(mOrdersEventListener);
            mOrdersEventListener = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
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



    private void attachOrderReadListener() {

        // Get all orders for the selected date and driver
        mOrdersQuery = getDatabaseQueryForMap();

        if (mOrdersEventListener == null) {

            mOrdersEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Log.i(Constants.LOG_TAG, "onDataChange in attachOrderListener on Fragment_MainDetails");
                    mOrdersList = new ArrayList<>();



                    if (dataSnapshot != null) {
                        Log.i(Constants.LOG_TAG, "we have a snapshot");


                        // Get each order that is in progress
                        for (DataSnapshot orders : dataSnapshot.getChildren()) {

                            Order order = orders.getValue(Order.class);
                            mOrdersList.add(order);
                        }
                    }

                    Log.i(Constants.LOG_TAG, "OrderCount:" + mOrdersList.size());

                    repaintOrderMarkers();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mOrdersQuery.addValueEventListener(mOrdersEventListener);

        }

    }

    private void attachDriverReadListener() {

        if (mActiveDriversEventListener == null) {
            mActiveDriversEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String driverKey = dataSnapshot.getKey();

                    final Double strLat = (Double) dataSnapshot.child("l").child("0").getValue();
                    final Double strLng = (Double) dataSnapshot.child("l").child("1").getValue();

                    if (strLat != null && strLng != null) {

                        // Get the driver just signed in
                        DatabaseReference driverRef = FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child(Constants.FIREBASE_NODE_DRIVERS)
                                .child(driverKey);

                        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    Driver driver = dataSnapshot.getValue(Driver.class);

                                    if (driver != null) {
                                        //Log.i(Constants.LOG_TAG, "driver was added:" + driver.toString());

                                        mDriversList.add(driver);
                                        addDriverMarker(driver, new LatLng(strLat, strLng));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                // This is called when a drivers location changes
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.exists()) {

                        //Log.i(Constants.LOG_TAG, "dataSnapshot:" + dataSnapshot.toString());

                        String driverKey = dataSnapshot.getKey();

                        Double strLat = (Double) dataSnapshot.child("l").child("0").getValue();
                        Double strLng = (Double) dataSnapshot.child("l").child("1").getValue();

                        if (strLat != null && strLng != null) {

                            LatLng driverLatLng = new LatLng(strLat, strLng);


                            for (int i = 0; i < mDriversList.size(); i++) {

                                Driver d = mDriversList.get(i);

                                if (Objects.equals(d.getUid(), driverKey)) {
                                    updateDriverMarker(i, driverLatLng);
                                }
                            }
                        }
                    }
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        //Log.i(Constants.LOG_TAG, "dataSnapshot:" + dataSnapshot.toString());

                        String driverKey = dataSnapshot.getKey();

                        for (int i = 0; i < mDriversList.size(); i++) {

                            Driver d = mDriversList.get(i);

                            if (Objects.equals(d.getUid(), driverKey)) {
                                mDriversList.remove(i);
                                removeDriverMarker(i);
                            }
                        }

                        // remove driver marker and reset camera

                        //removerDriverMarker();

                        //paintMarkers();

                    }
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mActiveDriverLocationRef.addChildEventListener(mActiveDriversEventListener);
        }


    }

    private void updateDriverMarker(int index, LatLng point) {

        // get the marker from the object array
        Marker marker = mDriverMarkers.get(index);

        // Change the position of the marker
        marker.setPosition(point);

        // Update the camera
        updateCamera();

    }

    private void removeDriverMarker(int index) {

        // get the marker ref from the object array
        Marker marker = mDriverMarkers.get(index);

        // remove the driver marker
        marker.remove();

        //remove the marker ref from the object array
        mDriverMarkers.remove(index);

        // update the camera
        updateCamera();

    }

    private void addDriverMarker(Driver driver, LatLng point) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point)
                .title(driver.getDisplayName())
                .snippet("something")
                .infoWindowAnchor(0.5f, 0.5f)
                .icon(Utils_General.vectorToBitmap(getContext(), R.drawable.ic_local_shipping_accent_24dp,
                        ContextCompat.getColor(getContext(), R.color.colorPrimary)));

        Marker marker = mGoogleMap.addMarker(markerOptions);

        mDriverMarkers.add(marker);

        updateCamera();

    }

    private void addOrderMarker(String location, Order order, Place place) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place.getLatLng())
                .title(order.getCustomerName())
                .snippet(place.getAddress().toString())
                .infoWindowAnchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.defaultMarker(Utils_General.getMarkerColorByStatus(location, order.getStatus())));

        Marker marker = mGoogleMap.addMarker(markerOptions);

        mOrderMarkers.add(marker);

        updateCamera();


    }

    private void repaintOrderMarkers() {

        // clear all order markers from the map
        for (Marker oldMarker : mOrderMarkers) {
            oldMarker.remove();
        }

        // clear the order marker list
        mOrderMarkers = new ArrayList<>();

        // if we have orders to show
        if (mOrdersList != null) {

            for (final Order order : mOrdersList) {

                // get the order pickup address
                final PendingResult<PlaceBuffer> pickupResult =
                        Places.GeoDataApi.getPlaceById(mGoogleApiClient, order.getPickupLocationId());

                pickupResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        // add the order pickup location marker
                        addOrderMarker(Constants.ORDER_MARKER_LOCATION_TYPE_PICKUP, order, places.get(0));
                    }
                });

                // get the order delivery address
                final PendingResult<PlaceBuffer> deliveryResult =
                        Places.GeoDataApi.getPlaceById(mGoogleApiClient, order.getDeliveryLocationId());

                deliveryResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        // add the order delivery location marker
                        addOrderMarker(Constants.ORDER_MARKER_LOCATION_TYPE_DELIVERY, order, places.get(0));
                    }
                });
            }
        }

        updateCamera();

    }

    private void updateCamera() {

        builder = new LatLngBounds.Builder();

        // add each driver marker position to the bounds builder
        for (Marker driverMarker : mDriverMarkers) {

            builder.include(driverMarker.getPosition());
        }

        // add each order marker position to the bounds builder
        for (Marker orderMarker : mOrderMarkers) {

            builder.include(orderMarker.getPosition());
        }

        mGoogleMap.setMaxZoomPreference(12);

        mGoogleMap.setMinZoomPreference(8);

        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
                mGoogleMap.moveCamera(cu);
                //mGoogleMap.animateCamera(cu);
            }
        });

    }

//    public void showDrivers(boolean b) {
//        // Whether to show the drivers or not
//
//        mDriversList = new ArrayList<>();
//
//        mShowDrivers = b;
//
//        // Show all active drivers
//        if (mShowDrivers) {
//            attachDriverReadListener();
//        } else {
//            paintMarkers();
//            detachFirebaseDriverListeners();
//        }
//
//    }
//
//    public void showOrders(boolean b) {
//
//        Toast.makeText(getActivity(), "Fragment 1: Refresh called.",
//                Toast.LENGTH_SHORT).show();
//
//        // Whether to show the drivers or not
//
//        mOrdersList = new ArrayList<>();
//
//        mShowOrders = b;
//
//        // Show all active drivers
//        if (mShowOrders) {
//            attachOrderReadListener();
//        } else {
//            paintMarkers();
//            detachFirebaseOrderListeners();
//        }
//
//
//    }

    private Query getDatabaseQueryForMap() {

        return mOrdersRef.orderByChild(Constants.FIREBASE_CHILD_PICKUP_DATE)
                .equalTo(mDisplayDateStartTimeInMillis);
    }
}


