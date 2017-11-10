package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-11-09.
 */

public class Activity_MyMap extends AppCompatActivity implements OnMapReadyCallback {

    // Local Member variables
    private ActionBar mActionBar;
    private GoogleApiClient mClient;
    private Order mOrder;

    // View to bind
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.mapView)
    MapView mMapView;


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
        } else {

        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("Order Details");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(Constants.LOG_TAG, "onMapReady()");


    }
}
