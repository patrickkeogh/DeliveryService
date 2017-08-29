package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-14.
 *
 */

public class Fragment_DriverDetails extends Fragment {

    // Member variables
    private Driver mDriver;
    private String mPostKey;

    // Fragment views
    private TextView tv_driver_name;
    private TextView tv_driver_email;


    private Button btn_verify;

    private DatabaseReference mDriverRef;
    private ValueEventListener mDriverDetailsListener;

    // Define a new interface onVerifyDriverClicked that triggers a callback in the host activity
    VerifyDriverClickListener mCallback;

    // onDriverSelected interface, calls a method in the host activity named onDriverSelected
    public interface VerifyDriverClickListener {
        void onVerifyDriverClicked(Driver driver);
    }

    /**
     * Static factory method that takes a driver object parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_DriverDetails newInstance(String key) {
        Fragment_DriverDetails f = new Fragment_DriverDetails();
        Bundle args = new Bundle();

        // Add any required arguments for start up - None needed right now
        args.putString(Constants.EXTRA_DRIVER_KEY, key);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_drivers_details, container, false);

        tv_driver_name = rootView.findViewById(R.id.tv_driver_name);
        tv_driver_email = rootView.findViewById(R.id.tv_driver_email);

        btn_verify = rootView.findViewById(R.id.btn_verify_driver);

        btn_verify.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                // Update driver record
                mCallback.onVerifyDriverClicked(mDriver);

            }
        });

        // Get arguments used during start up
        Bundle args = getArguments();

        // Get post key from args
        mPostKey = args.getString(Constants.EXTRA_DRIVER_KEY);

        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Get a reference to the drivers table
        mDriverRef = FirebaseDatabase.getInstance()
                .getReference().child(Constants.FIREBASE_NODE_DRIVERS).child(mPostKey);

        return rootView;

    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_DriverDetails.VerifyDriverClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DriverClickListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        attachDatabaseReadListener();

    }

    private void detachDatabaseReadListener() {
        if (mDriverDetailsListener != null) {
            mDriverRef.removeEventListener(mDriverDetailsListener);
            mDriverDetailsListener = null;
        }
    }

    private void attachDatabaseReadListener() {
        if (mDriverDetailsListener == null) {

            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.w(Constants.LOG_TAG, "loadPost:onDataChange");

                    // Get Post object and use the values to update the UI
                    Driver driver = dataSnapshot.getValue(Driver.class);
                    setViewData(driver);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(Constants.LOG_TAG, "loadPost:onCancelled", databaseError.toException());
                }
            };
            mDriverRef.addValueEventListener(listener);
            mDriverDetailsListener = listener;
        }
    }

    private void setViewData(Driver driver) {

        if(driver != null){
            tv_driver_name.setText(driver.getDisplayName());
            tv_driver_email.setText(driver.getEmail());

            if(driver.getDriverApproved()){
                btn_verify.setVisibility(View.GONE);
            }

        }

        mDriver = driver;

    }
}
