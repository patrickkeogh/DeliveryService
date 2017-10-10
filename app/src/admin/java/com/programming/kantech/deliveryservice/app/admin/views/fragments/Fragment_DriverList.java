package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.activities.Activity_SelectDriver;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-14.
 * Uses FirebaseDatabase-UI
 *
 */

public class Fragment_DriverList extends Fragment {

    // Member variables
    private FirebaseRecyclerAdapter<Driver, ViewHolder_Driver> mFireAdapter;
    private RecyclerView rv_drivers_list;

    // Firebase references
    private DatabaseReference mDriverRef;

    // Define a new interface onDriverSelected that triggers a callback in the host activity
    DriverClickListener mCallback;

    // onDriverSelected interface, calls a method in the host activity named onDriverSelected
    public interface DriverClickListener {
        void onDriverSelected(Driver driver);
    }

    // Mandatory empty constructor
    public Fragment_DriverList() {

    }

    /**
     * Static factory method that
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_DriverList newInstance() {
        return new Fragment_DriverList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.i(Constants.LOG_TAG, "onCreteView() in Fragment_DriverList");

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_drivers_master, container, false);

        // Get a reference to the drivers table
        mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);

        // Get a reference to the RecyclerView in the fragment_master_list xml layout file
        rv_drivers_list = rootView.findViewById(R.id.rv_drivers_list);

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        rv_drivers_list.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        rv_drivers_list.setHasFixedSize(false);

        mFireAdapter = new FirebaseRecyclerAdapter<Driver, ViewHolder_Driver>(
                Driver.class,
                R.layout.item_driver,
                ViewHolder_Driver.class,
                mDriverRef) {

            @Override
            public void populateViewHolder(ViewHolder_Driver holder, final Driver driver, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + driver.toString());

                holder.setName(driver.getDisplayName());
                holder.setPhoto(driver.getPhotoUrl(), getActivity());
                holder.setId(driver.getUid());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Notify the activity a driver was clicked
                        mCallback.onDriverSelected(driver);
                    }
                });
            }

        };

        rv_drivers_list.setAdapter(mFireAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_DriverList.DriverClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DriverClickListener");
        }
    }
}
