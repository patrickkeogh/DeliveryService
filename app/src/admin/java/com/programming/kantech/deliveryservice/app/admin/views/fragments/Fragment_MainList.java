package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-18.
 *
 */

public class Fragment_MainList extends Fragment {

    // Member variables
    private Button btn_main_show_drivers;
    private Button btn_main_show_locations_pickup;
    private Button btn_main_show_locations_delivery;

    private boolean mShowDrivers = false;
    private boolean mShowPickupLocations = false;



    /**
     * Static factory method that
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_MainList newInstance() {
        return new Fragment_MainList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_main_list, container, false);

        btn_main_show_drivers = rootView.findViewById(R.id.btn_main_show_drivers);
        btn_main_show_drivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowDrivers = !mShowDrivers;
                setupButtons();
            }
        });

        btn_main_show_locations_pickup = rootView.findViewById(R.id.btn_main_show_locations_pickup);
        btn_main_show_locations_pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        setupButtons();




        //Log.i(Constants.LOG_TAG, "onCreateView() driver: " + mDriver.toString());

        return rootView;

    }

    private void setupButtons() {
        Log.i(Constants.LOG_TAG, "setupButtons:" + mShowDrivers);
        if(mShowDrivers){
            btn_main_show_drivers.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        }else{
            btn_main_show_drivers.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));
        }
    }


}
