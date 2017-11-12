package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    //private Marker mDriverMarker;
    private ArrayList<Driver> mDriversList = new ArrayList<>();
    private ArrayList<Order> mOrdersList = new ArrayList<>();

    private ArrayList<LatLng> mCoordsList = new ArrayList<>();

    //private List<Marker> mMarkers_Drivers = new ArrayList<>();

    // 12:00 AM of the selected day
    private long mDisplayDateStartTimeInMillis;


    private boolean mShowOrders = true;
    private boolean mShowDrivers = true;

    private DatabaseReference mActiveDriverLocationRef;
    private ChildEventListener mActiveDriversEventListener;

    private Query mOrdersQuery;
    private ValueEventListener mOrdersEventListener;

    @BindView(R.id.mapView)
    MapView mapView;

    // Define a new interface StepNavClickListener that triggers a callback in the host activity
    MainDetailsFragmentListener mCallback;

    // MainDetailsFragmentListener interface, calls a method in the host activity
    // depending on the order selected in the master list
    public interface MainDetailsFragmentListener {
        void onFragmentLoaded(String tag);
    }

    /**
     * Static factory method that takes a order object parameter,
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

    /**
     * Static factory method that,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_MainDetails newInstance() {
        return new Fragment_MainDetails();
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

        mActiveDriverLocationRef = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_DRIVER_LOCATIONS);

        mOrdersQuery = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_ORDERS).orderByChild(Constants.FIREBASE_CHILD_INPROGRESS).equalTo(true);

        return rootView;

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

        // Show all active drivers
        if (mShowDrivers) {
            attachDriverReadListener();
        }

        // Get all orders not completed
        if (mShowOrders) {
            //attachOrderReadListener();
        }
    }

    private void paintMarkers() {

        // Clear any existing markers
        mGoogleMap.clear();
        builder = new LatLngBounds.Builder();

        // Add Marker for home office
        LatLng home = new LatLng(44.3916, -79.6882);
        drawMarker(home, "HOME", "Kan-Tech Delivery Service",
                Utils_General.vectorToBitmap(getContext(), R.drawable.ic_menu_home,
                        ContextCompat.getColor(getContext(), R.color.colorAccent)));


        if (mDriversList != null) {
            if (mDriversList.size() != 0) {

                //Log.i(Constants.LOG_TAG, "Paint Markers: Driver list is not null or zero");

                // Add markers for active drivers
                for (int i = 0; i < mDriversList.size(); i++) {

                    Driver d = mDriversList.get(i);
                    LatLng coords = mCoordsList.get(i);
                    drawMarker(coords, d.getDisplayName(), "",
                            Utils_General.vectorToBitmap(getContext(), R.drawable.ic_local_shipping_accent_24dp,
                                    ContextCompat.getColor(getContext(), R.color.colorPrimary)));

                }

            }

        }


        // Add Markers for pickup and delivery locations
        if (mOrdersList != null) {
            for (int i = 0; i < mOrdersList.size(); i++) {

                final Order order = mOrdersList.get(i);

                final PendingResult<PlaceBuffer> pickupResult =
                        Places.GeoDataApi.getPlaceById(mGoogleApiClient, order.getPickupLocationId());

                pickupResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        drawMarker(places.get(0).getLatLng(), order.getCustomerName(), places.get(0).getAddress().toString(),
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                });

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

    private LatLngBounds.Builder builder;

    private void drawMarker(LatLng point, String title, String snip, BitmapDescriptor bitmapDescriptor) {
        //Log.i(Constants.LOG_TAG, "drawMarker()");

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point)
                .title(title)
                .snippet(snip)
                .infoWindowAnchor(0.5f, 0.5f)
                .icon(bitmapDescriptor);

        mGoogleMap.addMarker(markerOptions);

        builder.include(markerOptions.getPosition());

        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
                mGoogleMap.animateCamera(cu);
            }
        });
    }


    private void attachOrderReadListener() {

        if (mOrdersEventListener == null) {

            mOrdersEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Log.i(Constants.LOG_TAG, "onDataChange in attachOrderListener on Fragment_MainDetails");
                    mOrdersList = new ArrayList<>();

                    if (dataSnapshot != null) {
                        //Log.i(Constants.LOG_TAG, "we have a snapshot");

                        // Get each order that is in progress
                        for (DataSnapshot orders : dataSnapshot.getChildren()) {

                            Order order = orders.getValue(Order.class);
                            //Log.i(Constants.LOG_TAG, "ORDERS:" + order.getCustomerName());

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

    private void attachDriverReadListener() {

        if (mActiveDriversEventListener == null) {
            mActiveDriversEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    //Log.i(Constants.LOG_TAG, "onChildAdded");

                    String driverKey = dataSnapshot.getKey();

                    final Double strLat = (Double) dataSnapshot.child("l").child("0").getValue();
                    final Double strLng = (Double) dataSnapshot.child("l").child("1").getValue();

                    if (strLat != null && strLng != null) {

                        // Get the driver just signed in
                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS).child(driverKey);

                        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    Driver driver = dataSnapshot.getValue(Driver.class);

                                    if (driver != null) {
                                        //Log.i(Constants.LOG_TAG, "driver was added:" + driver.toString());

                                        mDriversList.add(driver);
                                        mCoordsList.add(new LatLng(strLat, strLng));
                                        paintMarkers();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

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
                                    mCoordsList.set(i, driverLatLng);
                                }
                            }

                            paintMarkers();
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
                                mCoordsList.remove(i);
                            }
                        }

                        paintMarkers();

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

    public void showDrivers(boolean b) {
        // Whether to show the drivers or not

        mDriversList = new ArrayList<>();

        mShowDrivers = b;

        // Show all active drivers
        if (mShowDrivers) {
            attachDriverReadListener();
        } else {
            paintMarkers();
            detachFirebaseDriverListeners();
        }

    }

    public void showOrders(boolean b) {

        Toast.makeText(getActivity(), "Fragment 1: Refresh called.",
                Toast.LENGTH_SHORT).show();

        // Whether to show the drivers or not

        mOrdersList = new ArrayList<>();

        mShowOrders = b;

        // Show all active drivers
        if (mShowOrders) {
            attachOrderReadListener();
        } else {
            paintMarkers();
            detachFirebaseOrderListeners();
        }


    }
}


