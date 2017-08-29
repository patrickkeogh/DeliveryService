package com.programming.kantech.deliveryservice.app.admin.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.firebase.ui.auth.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_CustomerAdd;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_DriverDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_DriverList;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_Main;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_NewPhoneOrder;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_OrderList;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Customer;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by patrick keogh on 2017-08-18.
 *
 */

public class Activity_Main extends AppCompatActivity implements
        Fragment_DriverList.DriverClickListener,
        Fragment_NewPhoneOrder.GetCustomerClickListener,
        Fragment_DriverDetails.VerifyDriverClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    // Member variables
    private String mUsername;
    private ActionBar mActionBar;

    // Track whether to display a two-column or single-column UI
    private boolean mLandscapeView;
    private boolean mIsFirstTimeLoaded = true;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawer;

    @InjectView(R.id.nav_view)
    NavigationView mNavView;

    // Member variables for the Firebase database
    private DatabaseReference mUserRef;
    private DatabaseReference mDriverRef;
    private ChildEventListener mDriversTableEventListener;

    // Member variables for the Firebase Authorization
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(Constants.LOG_TAG, "onCreate() in Admin Activity_Main");

        ButterKnife.inject(this);

        // ToDo: Add code here
//        if (savedInstanceState != null) {
//
//        } else {
//
//
//        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle("Works");
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavView.setNavigationItemSelectedListener(this);

        Menu nav_Menu = mNavView.getMenu();
        nav_Menu.findItem(R.id.nav_admin_drivers).setVisible(true);

        mFirebaseAuth = FirebaseAuth.getInstance();

        // TODO: Where would a better place to call this be?
        FirebaseMessaging.getInstance().subscribeToTopic("Admin");

        mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ADMIN);
        mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);

        createAuthListener();

        // Determine if you're creating a two-pane or single-pane display
        mLandscapeView = (findViewById(R.id.layout_for_two_cols) != null);

        if (savedInstanceState == null && mIsFirstTimeLoaded) {
            // add main fragment to master container on first start
            mIsFirstTimeLoaded = false;
            Fragment_Main fragment = Fragment_Main.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN_ADD, fragment);
        }
    }


    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_admin_call_in_order) {
            // Add new phone orders here
            Fragment_NewPhoneOrder frag_order = Fragment_NewPhoneOrder.newInstance(null);
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN, frag_order);

        } else if (id == R.id.nav_admin_home) {
            Fragment_Main frag_main = Fragment_Main.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN, frag_main);

        } else if (id == R.id.nav_admin_drivers) {
            Fragment_DriverList frag_driver = Fragment_DriverList.newInstance();

            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_DRIVER_LIST, frag_driver);

        } else if (id == R.id.nav_admin_customers) {

            Fragment_CustomerAdd frag_customer = Fragment_CustomerAdd.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_ADD, frag_customer);


        }else if (id == R.id.nav_admin_manage_orders) {

            Fragment_OrderList frag_order_list = Fragment_OrderList.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_LIST, frag_order_list);

        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createAuthListener() {

        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // Signed In
                    //Utils_General.showToast(Activity_Main_old.this, "You are now signed in");

                    onSignedInInitialize(user);
                } else {
                    // Not signed in
                    onSignedOutCleanup();

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .setTheme(R.style.LoginTheme)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            Constants.REQUEST_CODE_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_SIGN_IN) {

            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Utils_General.showToast(this, "Signed In!");

            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Utils_General.showToast(this, "Signed In Cancelled!");
                finish();
            }
        } else if(requestCode == Constants.REQUEST_CODE_SELECT_CUSTOMER){
            if (resultCode == RESULT_OK) {
                // Customer was successfully selected
                Utils_General.showToast(this, "Customer Selected");

                // Get the customer from the intent data
                Customer customer = data.getParcelableExtra(Constants.EXTRA_CUSTOMER);

                //

                Fragment_NewPhoneOrder fragment = Fragment_NewPhoneOrder.newInstance(customer);
                replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_ADD, fragment);

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(this, "Customer Not Selected");
                //finish();
            }
        } else if(requestCode == Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION){
            if (resultCode == RESULT_OK) {
                // Customer was successfully selected
                Utils_General.showToast(this, "Pickup Location Selected");

                // Get the customer from the intent data
                Customer customer = data.getParcelableExtra(Constants.EXTRA_LOCATION);

                //

                Fragment_NewPhoneOrder fragment = Fragment_NewPhoneOrder.newInstance(customer);
                replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_ADD, fragment);

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(this, "Customer Not Selected");
                //finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mFirebaseAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthListener);
        }

        detachDatabaseReadListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthListener);
        attachDatabaseReadListeners();
    }

    private void onSignedOutCleanup() {
        //mUsername = ANONYMOUS;

        //mMessageAdapter.clear();
        detachDatabaseReadListeners();


    }

    private void onSignedInInitialize(final FirebaseUser user) {
        mUsername = user.getDisplayName();

        if (mActionBar != null) {
            mActionBar.setTitle(mUsername);
        }

        DatabaseReference adminRef = mUserRef.child(user.getUid());

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOG_TAG, "onDataChange()called for driver");

                if (dataSnapshot.exists()) {
                    // run some code

                    Driver driver = dataSnapshot.getValue(Driver.class);
                    Log.i(Constants.LOG_TAG, "The driver is in the db:" + driver.toString());

//                    if (!driver.getDriverApproved()) {
//                        //checkIfAuthorized();
//                    }

                } else {
                    Log.i(Constants.LOG_TAG, "The driver is not in the database");
                    // User is not in the driver db. add them
                    Driver driver = new Driver(user.getUid(), user.getDisplayName(), user.getEmail(), false, "");

                    // TODO: Not sure which way is better?????
                    mUserRef.child(user.getUid()).setValue(driver);
                    //mDriverDBReference.push().setValue(driver);

                    //checkIfAuthorized();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //checkIfAuthorized();
        attachDatabaseReadListeners();

    }

    private void replaceFragment(int container, String fragment_tag, Fragment fragment_in) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (findViewById(R.id.container_master) != null) {
            Log.i(Constants.LOG_TAG, "Found Master");
        } else {
            Log.i(Constants.LOG_TAG, "Master NOT Found");

        }

        FrameLayout container_details = (FrameLayout) findViewById(R.id.container_details);
        FrameLayout container_master = (FrameLayout) findViewById(R.id.container_master);

        switch (fragment_tag) {
            case Constants.TAG_FRAGMENT_MAIN_ADD:

                if (mLandscapeView) {
                    container_details.setVisibility(View.GONE);
                    container_master.setLayoutParams(new LinearLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                }

                transaction.add(container, fragment_in, fragment_tag);

                Log.i(Constants.LOG_TAG, "Main Fragment added");

                break;
            case Constants.TAG_FRAGMENT_MAIN:

                if (mLandscapeView) {
                    container_details.setVisibility(View.GONE);
                    container_master.setLayoutParams(new LinearLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                }


                transaction.replace(container, fragment_in, fragment_tag);

                Log.i(Constants.LOG_TAG, "Main Fragment replaced");

                break;
            case Constants.TAG_FRAGMENT_DRIVER_LIST:

                if (mLandscapeView) {
                    container_details.setVisibility(View.VISIBLE);

                    final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
                    int pixels = (int) (300 * scale + 0.5f);

                    container_master.setLayoutParams(new LinearLayout.LayoutParams(
                            pixels, FrameLayout.LayoutParams.MATCH_PARENT));
                }

                transaction.replace(container, fragment_in, fragment_tag);

                Log.i(Constants.LOG_TAG, "Drivers List Fragment replaced");

                break;
            case Constants.TAG_FRAGMENT_DRIVER_DETAILS:

                if (mLandscapeView) {
                    container_details.setVisibility(View.VISIBLE);

                    final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
                    int pixels = (int) (300 * scale + 0.5f);

                    container_master.setLayoutParams(new LinearLayout.LayoutParams(
                            pixels, FrameLayout.LayoutParams.MATCH_PARENT));
                }

                transaction.replace(container, fragment_in, fragment_tag);

                Log.i(Constants.LOG_TAG, "Drivers Details Fragment replaced");

                break;
            case Constants.TAG_FRAGMENT_CUSTOMER_ADD:

                if (mLandscapeView) {
                    container_details.setVisibility(View.GONE);
                    container_master.setLayoutParams(new LinearLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                }

                transaction.replace(container, fragment_in, fragment_tag);

                Log.i(Constants.LOG_TAG, "Main Fragment added");

                break;

            case Constants.TAG_FRAGMENT_ORDER_ADD:

                if (mLandscapeView) {
                    container_details.setVisibility(View.GONE);
                    container_master.setLayoutParams(new LinearLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                }

                transaction.replace(container, fragment_in, fragment_tag);

                Log.i(Constants.LOG_TAG, "Add Order Fragment added");

                break;
            case Constants.TAG_FRAGMENT_ORDER_LIST:

                if (mLandscapeView) {
                    container_details.setVisibility(View.VISIBLE);

                    final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
                    int pixels = (int) (300 * scale + 0.5f);

                    container_master.setLayoutParams(new LinearLayout.LayoutParams(
                            pixels, FrameLayout.LayoutParams.MATCH_PARENT));
                }

                transaction.replace(container, fragment_in, fragment_tag);

                Log.i(Constants.LOG_TAG, "Order List Fragment replaced");

                break;
            default:
                Utils_General.showToast(this, "Fragment not found");

        }

        // Commit the transaction
        transaction.commit();


    }

    @Override
    public void onDriverSelected(String key) {

        Fragment_DriverDetails fragment = Fragment_DriverDetails.newInstance(key);

        if (mLandscapeView) {
            replaceFragment(R.id.container_details, Constants.TAG_FRAGMENT_DRIVER_DETAILS, fragment);
        } else {
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_DRIVER_DETAILS, fragment);

        }

    }

    @Override
    public void onVerifyDriverClicked(Driver driver) {

        driver.setDriverApproved(true);

        mDriverRef.child(driver.getUid()).setValue(driver);

    }

    private void detachDatabaseReadListeners() {
        if (mDriversTableEventListener != null) {
            mDriverRef.removeEventListener(mDriversTableEventListener);
            mDriversTableEventListener = null;
        }
    }

    private void attachDatabaseReadListeners() {

        if (mDriversTableEventListener == null) {
            mDriversTableEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
//                    mMessageAdapter.add(friendlyMessage);

                    Utils_General.showToast(getApplicationContext(), "Child Changed");

                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mDriverRef.addChildEventListener(mDriversTableEventListener);
        }


    }

//    @Override
//    public void onCustomerSelected(Customer selectedCustomer) {
//        Log.i(Constants.LOG_TAG, "onCustomerSelected calledd");
//
//        Fragment_NewPhoneOrder fragment = Fragment_NewPhoneOrder.newInstance(selectedCustomer);
//        replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_ADD, fragment);
//
//    }

    @Override
    public void onGetCustomerSelected() {
        Log.i(Constants.LOG_TAG, "onGetCustomerSelected called");

        //Fragment_CustomerList fragment = Fragment_CustomerList.newInstance();
        //replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_LIST, fragment);

        Intent intent = new Intent(this, Activity_SelectCustomer.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_CUSTOMER );

    }

    @Override
    public void onGetLocationPickup(Customer customer) {
        Log.i(Constants.LOG_TAG, "onGetLocationPickup called CustKey:" + customer.getId());

        Intent intent = new Intent(this, Activity_SelectLocation.class);

        intent.putExtra(Constants.EXTRA_CUSTOMER_KEY, customer.getId());
        intent.putExtra(Constants.EXTRA_CUSTOMER_NAME, customer.getCompany());
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION );

    }

    @Override
    public void onGetLocationDelivery() {

    }
}
