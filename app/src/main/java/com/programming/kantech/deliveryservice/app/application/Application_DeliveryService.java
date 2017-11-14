package com.programming.kantech.deliveryservice.app.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-15.
 *
 */

public class Application_DeliveryService extends Application {

    private static Application_DeliveryService instance;

    public Context mContext;

    public Application_DeliveryService() {

        instance = this;
    }
    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();

    }

}
