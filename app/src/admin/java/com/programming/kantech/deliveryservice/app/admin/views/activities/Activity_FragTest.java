package com.programming.kantech.deliveryservice.app.admin.views.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_MainDetails;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-11-08.
 */

public class Activity_FragTest extends AppCompatActivity implements Fragment_MainDetails.MainDetailsFragmentListener {

    private FragmentManager mFragmentManager;

    private ActionBar mActionBar;
    private LinearLayout layout_for_two_frags;
    private FrameLayout container_full_screen;
    // Track whether to display a two-column or single-column UI
    private boolean mLandscapeView;

    // Bind the layout views
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Log.i(Constants.LOG_TAG, "onCreate() in Admin Activity_Main");

        layout_for_two_frags = (LinearLayout) findViewById(R.id.layout_for_two_frags);
        container_full_screen = (FrameLayout) findViewById(R.id.container_full_screen);

        ButterKnife.bind(this);

        mFragmentManager = getSupportFragmentManager();

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        // Get the current orientation
        mLandscapeView = (findViewById(R.id.layout_for_two_cols) != null);
        Log.i(Constants.LOG_TAG, "Update Landscape variable to:" + mLandscapeView);

        if (savedInstanceState == null) {
            // Add main fragment to master container

            Fragment_MainDetails fragment = Fragment_MainDetails.newInstance();

            if(mLandscapeView){

                layout_for_two_frags.setVisibility(View.GONE);
                container_full_screen.setVisibility(View.VISIBLE);

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container_full_screen, fragment).commit();

            }else{

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container_master, fragment).commit();
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(Constants.LOG_TAG, "onSaveInstanceState() called in Activity_FragTest");

    }


    @Override
    public void onFragmentLoaded(String tag) {

    }
}
