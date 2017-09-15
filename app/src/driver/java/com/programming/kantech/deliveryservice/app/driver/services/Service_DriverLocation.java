package com.programming.kantech.deliveryservice.app.driver.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-09-04.
 */

public class Service_DriverLocation extends Service implements com.google.android.gms.location.LocationListener {

    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    //private LocationManager mLocationManager = null;
    Location mLastLocation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(Constants.LOG_TAG, "We have a location change");

        if(getApplicationContext()!=null){
            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

            // Get the drivers id
            String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference driverAvailable = FirebaseDatabase.getInstance().getReference("driver");
            GeoFire geoFireDriver = new GeoFire(driverAvailable);

            geoFireDriver.setLocation(driverId, new GeoLocation(location.getLatitude(), location.getLongitude()));

        }
    }

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {
            Log.e(Constants.LOG_TAG, "LocationListener " + provider);
            //mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
