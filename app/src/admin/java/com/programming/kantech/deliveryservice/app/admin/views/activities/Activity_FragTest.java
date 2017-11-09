package com.programming.kantech.deliveryservice.app.admin.views.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.firebase.ui.auth.AuthUI;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_CustomerList;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_MainDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_NewPhoneOrder;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Customer;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patri on 2017-11-08.
 */

public class Activity_FragTest extends AppCompatActivity implements
        Fragment_MainDetails.MainDetailsFragmentListener,
Fragment_CustomerList.CustomerClickListener{

    private FragmentManager mFragmentManager;

    private ActionBar mActionBar;
    private LinearLayout layout_for_two_frags;
    private FrameLayout container_full_screen;
    // Track whether to display a two-column or single-column UI
    private boolean mLandscapeView;

    private Fragment mFragmentFullScreen;
    private Fragment mFragmentMaster;
    private Fragment mFragmentDetails;

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

            if (mLandscapeView) {

                layout_for_two_frags.setVisibility(View.GONE);
                container_full_screen.setVisibility(View.VISIBLE);

                // Add the fragment to the 'fragment_container' FrameLayout
                mFragmentManager.beginTransaction()
                        .add(R.id.container_full_screen, fragment, Constants.TAG_FRAGMENT_MAIN_DETAILS).commit();

            } else {

                // Add the fragment to the 'fragment_container' FrameLayout
                mFragmentManager.beginTransaction()
                        .add(R.id.container_master, fragment, Constants.TAG_FRAGMENT_MAIN_DETAILS).commit();
            }

        } else {

            // Get the Fragments from the state
            if (savedInstanceState.containsKey(Constants.STATE_INFO_FRAGMENT_FULLSCREEN)) {
                String tag_fullscreen = savedInstanceState.getString(Constants.STATE_INFO_FRAGMENT_FULLSCREEN);

                mFragmentFullScreen = new Fragment();
                mFragmentFullScreen = mFragmentManager.findFragmentByTag(tag_fullscreen);
            }

            if (savedInstanceState.containsKey(Constants.STATE_INFO_FRAGMENT_MASTER)) {
                String tag_master = savedInstanceState.getString(Constants.STATE_INFO_FRAGMENT_MASTER);

                mFragmentMaster = new Fragment();
                mFragmentMaster = mFragmentManager.findFragmentByTag(tag_master);
            }

            if (savedInstanceState.containsKey(Constants.STATE_INFO_FRAGMENT_DETAILS)) {
                String tag_details = savedInstanceState.getString(Constants.STATE_INFO_FRAGMENT_DETAILS);

                mFragmentDetails = new Fragment();
                mFragmentDetails = mFragmentManager.findFragmentByTag(tag_details);
            }
        }

        if (!mLandscapeView) {

            if (mFragmentFullScreen != null) {
                if (mFragmentFullScreen instanceof Fragment_MainDetails) {

                    mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    mFragmentManager.beginTransaction().remove(mFragmentFullScreen).commit();
                    mFragmentManager.executePendingTransactions();

                    // Add the fragment to the 'fragment_container' FrameLayout
                    mFragmentManager.beginTransaction()
                            .replace(R.id.container_master, mFragmentFullScreen, Constants.TAG_FRAGMENT_MAIN_DETAILS).commit();

                }
            }

            if (mFragmentMaster != null) {
                if (mFragmentMaster instanceof Fragment_MainDetails) {

                    mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    mFragmentManager.beginTransaction().remove(mFragmentMaster).commit();
                    mFragmentManager.executePendingTransactions();

                    // Add the fragment to the 'fragment_container' FrameLayout
                    mFragmentManager.beginTransaction()
                            .replace(R.id.container_master, mFragmentMaster, Constants.TAG_FRAGMENT_MAIN_DETAILS).commit();

                }
            }

        }else{
            if (mFragmentFullScreen != null) {
                if (mFragmentFullScreen instanceof Fragment_MainDetails) {

                    layout_for_two_frags.setVisibility(View.GONE);
                    container_full_screen.setVisibility(View.VISIBLE);

                    mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    mFragmentManager.beginTransaction().remove(mFragmentFullScreen).commit();
                    mFragmentManager.executePendingTransactions();

                    // Add the fragment to the 'fragment_container' FrameLayout
                    mFragmentManager.beginTransaction()
                            .replace(R.id.container_full_screen, mFragmentFullScreen, Constants.TAG_FRAGMENT_MAIN_DETAILS).commit();

                }
            }

            if (mFragmentMaster != null) {
                if (mFragmentMaster instanceof Fragment_MainDetails) {

                    layout_for_two_frags.setVisibility(View.GONE);
                    container_full_screen.setVisibility(View.VISIBLE);

                    mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    mFragmentManager.beginTransaction().remove(mFragmentMaster).commit();
                    mFragmentManager.executePendingTransactions();

                    // Add the fragment to the 'fragment_container' FrameLayout
                    mFragmentManager.beginTransaction()
                            .replace(R.id.container_full_screen, mFragmentMaster, Constants.TAG_FRAGMENT_MAIN_DETAILS).commit();

                }
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(Constants.LOG_TAG, "onSaveInstanceState() called in Activity_FragTest");

        Fragment frag_master = mFragmentManager.findFragmentById(R.id.container_master);

        if (frag_master != null) {
            String tag_details = frag_master.getTag();
            Log.i(Constants.LOG_TAG, "fragment tag stored for master:" + tag_details);
            outState.putString(Constants.STATE_INFO_FRAGMENT_MASTER, tag_details);
        }

        if (mLandscapeView) {

            Fragment frag_details = mFragmentManager.findFragmentById(R.id.container_details);

            if (frag_details != null) {
                String tag_details = frag_details.getTag();
                Log.i(Constants.LOG_TAG, "fragment tag stored for details:" + tag_details);
                outState.putString(Constants.STATE_INFO_FRAGMENT_DETAILS, tag_details);
            }

            Fragment frag_fullscreen = mFragmentManager.findFragmentById(R.id.container_full_screen);

            if (frag_fullscreen != null) {
                String tag_details = frag_fullscreen.getTag();
                Log.i(Constants.LOG_TAG, "fragment tag stored for full screen:" + tag_details);
                outState.putString(Constants.STATE_INFO_FRAGMENT_FULLSCREEN, tag_details);
            }


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_frag_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_customers:

                if (mLandscapeView) {

                    layout_for_two_frags.setVisibility(View.VISIBLE);
                    container_full_screen.setVisibility(View.GONE);
                }

                Fragment_CustomerList frag = new Fragment_CustomerList().newInstance(null);

                // Add the fragment to the 'fragment_container' FrameLayout
                mFragmentManager.beginTransaction()
                        .add(R.id.container_master, frag, Constants.TAG_FRAGMENT_CUSTOMER_LIST).commit();

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onCustomerSelected(Customer customer) {

    }

    @Override
    public void onAddCustomerClicked() {

    }

    @Override
    public void onFragmentLoaded(String tag) {

    }
}
