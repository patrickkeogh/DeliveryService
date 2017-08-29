package com.programming.kantech.deliveryservice.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patri on 2017-08-15.
 */

public class Service_FirebaseUserListener extends Service {

    // Firebase references
    private DatabaseReference mDriverRef;
    private ValueEventListener mDriverDetailsListener;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Service_FirebaseUserListener(String key) {

        mDriverRef = FirebaseDatabase.getInstance().getReference().child("drivers").child(key);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.w(Constants.LOG_TAG, "loadPost:onDataChange");

                // Get Post object and use the values to update the UI
                Driver driver = dataSnapshot.getValue(Driver.class);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(Constants.LOG_TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDriverRef.addValueEventListener(listener);

    }


}
