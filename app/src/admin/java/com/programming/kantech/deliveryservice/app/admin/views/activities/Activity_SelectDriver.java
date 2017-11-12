package com.programming.kantech.deliveryservice.app.admin.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick keogh on 2017-09-06.
 * Displays a list of available drivers and allows the user to select one
 * then returning to the previous screen
 *
 */

public class Activity_SelectDriver extends AppCompatActivity{

    // Local member variables
    private FirebaseRecyclerAdapter<Driver, ViewHolder_Driver> mFireAdapter;

    @BindView(R.id.rv_drivers_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_driver);

        ButterKnife.bind(this);

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(R.string.admin_select_driver);
        }

        // Get a reference to the driver node
        DatabaseReference mDriversRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);

        // Set up the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(false);

        mFireAdapter = new FirebaseRecyclerAdapter<Driver, ViewHolder_Driver>(
                Driver.class,
                R.layout.item_driver,
                ViewHolder_Driver.class,
                mDriversRef.orderByChild(Constants.FIREBASE_CHILD_DRIVER_APPROVED).equalTo(true)) {

            @Override
            public void populateViewHolder(final ViewHolder_Driver holder, final Driver driver, int position) {
                //Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + driver.getDisplayName());

                holder.setPhoto(driver.getPhotoUrl(), Activity_SelectDriver.this);

                holder.setName(driver.getDisplayName());
                holder.setId(driver.getUid());

                // add click listener to the recyclerview item
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Send the selected customer back to the main activity
                        finishTheActivity(driver);
                    }
                });

            }

            @Override
            public void onBindViewHolder(ViewHolder_Driver viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
            }
        };

        mRecyclerView.setAdapter(mFireAdapter);

    }

    /**
     * Called when the activity is starting
     *
     * @param driver The Driver object to return to the calling activity
     */
    private void finishTheActivity(Driver driver) {

        Intent resultIntent = new Intent();

        resultIntent.putExtra(Constants.EXTRA_DRIVER, driver);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
    }
}
