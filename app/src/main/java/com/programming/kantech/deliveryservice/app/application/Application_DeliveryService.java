package com.programming.kantech.deliveryservice.app.application;

import android.app.Application;
import android.content.Context;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by patrick keogh on 2017-08-15.
 *
 */

public class Application_DeliveryService extends Application {

    private static Application_DeliveryService instance;

    // Firebase references
    private DatabaseReference mDriverTableRef;
    private ValueEventListener mDriverDetailsListener;

    public Application_DeliveryService() {
        instance = this;

        // Get an instance of a firebase database
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

        // Get a reference to the drivers table
        mDriverTableRef = mFirebaseDatabase.getReference().child("drivers");

    }
    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
