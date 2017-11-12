package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick keogh on 2017-08-14.
 * Uses FirebaseDatabase-UI, ButterKnife
 *
 */

public class Fragment_DriverList extends Fragment {

    // Member variables
    private FirebaseRecyclerAdapter<Driver, ViewHolder_Driver> mFireAdapter;
    private RecyclerView.AdapterDataObserver mObserver;

    // Bind the layout views
    @BindView(R.id.rv_drivers_list)
    RecyclerView rv_drivers_list;

    @BindView(R.id.tv_empty_view)
    TextView tv_empty_view;

    // Firebase references
    private DatabaseReference mDriverRef;

    // Define a new interface onDriverSelected that triggers a callback in the host activity
    DriverClickListener mCallback;

    // onDriverSelected interface, calls a method in the host activity named onDriverSelected
    public interface DriverClickListener {
        void onDriverSelected(Driver driver);
        void onFragmentLoaded(String tag);
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

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_drivers_master, container, false);

        // Bind to our rootView
        ButterKnife.bind(this, rootView);

        // Get a reference to the drivers table
        mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);

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
                holder.setName(driver.getDisplayName());
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

        // Hide or show the list depending on if there are records
        mDriverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Perform initial setup, this will only be called once
                if(dataSnapshot.hasChildren()){
                    showList(true);
                }else{
                    showList(false);
                }

                // Create an observer to check if the list changes
                mObserver = new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {

                        int count = mFireAdapter.getItemCount();
                        if(count == 0){
                            showList(false);
                        }else{
                            showList(true);
                        }
                    }

                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        int count = mFireAdapter.getItemCount();
                        if(count == 0){
                            showList(false);
                        }else{
                            showList(true);
                        }
                    }
                };
                mFireAdapter.registerAdapterDataObserver(mObserver);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showList(boolean bShowList) {

        if(bShowList){
            rv_drivers_list.setVisibility(View.VISIBLE);
            tv_empty_view.setVisibility(View.GONE);
        }else{
            rv_drivers_list.setVisibility(View.GONE);
            tv_empty_view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Notify the activity this fragment was loaded
        mCallback.onFragmentLoaded(Constants.TAG_FRAGMENT_DRIVER_LIST);
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
