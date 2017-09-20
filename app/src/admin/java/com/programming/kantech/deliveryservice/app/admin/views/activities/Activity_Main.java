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
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_CustomerDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_CustomerList;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_DriverDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_DriverList;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_MainDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_MainList;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_NewPhoneOrder;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_OrderDetails;
import com.programming.kantech.deliveryservice.app.admin.views.fragments.Fragment_OrderList;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Customer;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Arrays;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by patrick keogh on 2017-08-18.
 *
 */

public class Activity_Main extends AppCompatActivity implements
        Fragment_DriverList.DriverClickListener,
        Fragment_OrderList.OrderClickListener,
        Fragment_CustomerList.CustomerClickListener,
        Fragment_NewPhoneOrder.GetCustomerClickListener,
        Fragment_DriverDetails.VerifyDriverClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private ActionBar mActionBar;

    // Track whether to display a two-column or single-column UI
    private boolean mLandscapeView;
    private boolean mIsFirstTimeLoaded = true;

    private boolean mShowDriverIcon = true;
    private boolean mShowDrivers = true;
    private boolean mShowOrderIcon = true;
    private boolean mShowOrders = true;

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

    private MenuItem mMenuItem_Driver;
    private MenuItem mMenuItem_Order;


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
        nav_Menu.findItem(R.id.nav_admin_manage_drivers).setVisible(true);

        mFirebaseAuth = FirebaseAuth.getInstance();

        // TODO: Where would a better place to call this be?
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FIREBASE_NOTIFICATION_TOPIC_ADMIN);
        FirebaseMessaging.getInstance().unsubscribeFromTopic("Admin");

        mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ADMIN);
        mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);

        createAuthListener();

        // Determine if you're creating a two-pane or single-pane display
        mLandscapeView = (findViewById(R.id.layout_for_two_cols) != null);

        if (savedInstanceState == null && mIsFirstTimeLoaded) {
            // add main fragment to master container on first start
            mIsFirstTimeLoaded = false;
            Fragment_MainDetails fragment = Fragment_MainDetails.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN_DETAILS, fragment, true);

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

        // if we are in 2 pane mode, remove what ever is in the details container
        // when loading the lookup lists
        if (mLandscapeView) {
            clearDetailsContainer();
        }

        if (id == R.id.nav_admin_call_in_order) {
            // Add new phone orders here
            Fragment_NewPhoneOrder frag_order = Fragment_NewPhoneOrder.newInstance(null);
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_ADD, frag_order, true);

            setActionBarTitle("Create Phone Order");

        } else if (id == R.id.nav_admin_home) {
            Fragment_MainDetails fragment = Fragment_MainDetails.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_MAIN_DETAILS, fragment, true);

            setActionBarTitle("Map View");

        } else if (id == R.id.nav_admin_manage_drivers) {

            Fragment_DriverList frag_driver = Fragment_DriverList.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_DRIVER_LIST, frag_driver, false);

            setActionBarTitle("Manage Drivers");

        } else if (id == R.id.nav_admin_manage_customers) {

            Fragment_CustomerList frag_customer = Fragment_CustomerList.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_LIST, frag_customer, false);

            setActionBarTitle("Manage Customers");

        } else if (id == R.id.nav_admin_manage_orders) {

            Fragment_OrderList frag_order_list = Fragment_OrderList.newInstance();
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_LIST, frag_order_list, false);

            setActionBarTitle("Manage Orders");

        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clearDetailsContainer() {

        if (getSupportFragmentManager().findFragmentById(R.id.container_details) != null) {

            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager()
                            .findFragmentById(R.id.container_details)).commit();
        }


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
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
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
        } else if (requestCode == Constants.REQUEST_CODE_SELECT_CUSTOMER) {
            if (resultCode == RESULT_OK) {
                // Customer was successfully selected
                Utils_General.showToast(this, "Customer Selected");

                // Get the customer from the intent data
                Customer customer = data.getParcelableExtra(Constants.EXTRA_CUSTOMER);

                //

                Fragment_NewPhoneOrder fragment = Fragment_NewPhoneOrder.newInstance(customer);
                replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_ADD, fragment, true);

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(this, "Customer Not Selected");
                //finish();
            }
        } else if (requestCode == Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION) {
            if (resultCode == RESULT_OK) {
                // Customer was successfully selected
                Utils_General.showToast(this, "Pickup Location Selected");

                // Get the customer from the intent data
                Customer customer = data.getParcelableExtra(Constants.EXTRA_LOCATION);

                //

                Fragment_NewPhoneOrder fragment = Fragment_NewPhoneOrder.newInstance(customer);
                replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_ADD, fragment, true);

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

        mMenuItem_Driver = (MenuItem) menu.findItem(R.id.action_show_drivers);
        mMenuItem_Order = (MenuItem) menu.findItem(R.id.action_show_orders);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        Log.i(Constants.LOG_TAG, "onPrepareOptionsMenu called");

        mMenuItem_Driver.setVisible(mShowDriverIcon);
        if(!mShowDrivers){
            mMenuItem_Driver.setIcon(R.drawable.ic_menu_drive);
        }else{
            mMenuItem_Driver.setIcon(R.drawable.ic_menu_drive_white);
        }

        mMenuItem_Order.setVisible(mShowOrderIcon);
        if(!mShowOrders){
            mMenuItem_Order.setIcon(R.drawable.ic_shopping_cart);
        }else{
            mMenuItem_Order.setIcon(R.drawable.ic_shopping_cart_white);
        }


        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_orders:
                mShowOrders = !mShowOrders;
                invalidateOptionsMenu();

                Fragment_MainDetails frag_order = (Fragment_MainDetails)
                        getSupportFragmentManager().findFragmentByTag(Constants.TAG_FRAGMENT_MAIN_DETAILS);

                if(frag_order != null){
                    frag_order.showOrders(mShowOrders);
                }

                return true;
            case R.id.action_show_drivers:
                mShowDrivers = !mShowDrivers;
                invalidateOptionsMenu();

                Fragment_MainDetails frag_driver = (Fragment_MainDetails)
                        getSupportFragmentManager().findFragmentByTag(Constants.TAG_FRAGMENT_MAIN_DETAILS);

                if(frag_driver != null){
                    frag_driver.showDrivers(mShowDrivers);
                }

                return true;
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
        String mUsername = user.getDisplayName();

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

                } else {
                    Log.i(Constants.LOG_TAG, "The driver is not in the database");
                    // User is not in the driver db. add them
                    Driver driver = new Driver(user.getUid(), user.getDisplayName(), user.getEmail(), "", false, false, "", "");

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

    private void replaceFragment(int container, String fragment_tag, Fragment fragment_in, boolean showFullScreen) {

        // Get a fragment transaction to replace fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Get references to the containers used to host the fragments
        FrameLayout container_details = (FrameLayout) findViewById(R.id.container_details);
        FrameLayout container_master = (FrameLayout) findViewById(R.id.container_master);

        // Change the width of the master container if full screen required for landscape mode

        if (mLandscapeView) {
            if(!showFullScreen){
                container_details.setVisibility(View.VISIBLE);
                final float scale = getResources().getDisplayMetrics().density;
                int requiredWidth = 0;
                switch (fragment_tag){
                    case Constants.TAG_FRAGMENT_ORDER_LIST:
                    case Constants.TAG_FRAGMENT_ORDER_DETAILS:
                        requiredWidth = getResources().getInteger(R.integer.container_width_order_list);
                        break;
                    case Constants.TAG_FRAGMENT_CUSTOMER_LIST:
                    case Constants.TAG_FRAGMENT_CUSTOMER_DETAILS:
                        requiredWidth = getResources().getInteger(R.integer.container_width_customer_list);
                        break;
                    default:
                        requiredWidth = getResources().getInteger(R.integer.container_width);

                }

                int pixels = (int) (requiredWidth * scale + 0.5f);

                container_master.setLayoutParams(new LinearLayout.LayoutParams(
                        pixels, FrameLayout.LayoutParams.MATCH_PARENT));
            }else{
                container_details.setVisibility(View.GONE);
                container_master.setLayoutParams(new LinearLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            }

        }

        if(!Objects.equals(fragment_tag, Constants.TAG_FRAGMENT_MAIN_DETAILS)){
            mShowDriverIcon = false;
            mShowOrderIcon = false;

        }else{
            mShowDriverIcon = true;
            mShowOrderIcon = true;
        }

        invalidateOptionsMenu();

        if(Objects.equals(fragment_tag, Constants.TAG_FRAGMENT_MAIN_LIST_START)){
            transaction.add(container, fragment_in, fragment_tag);
        }else{
            transaction.replace(container, fragment_in, fragment_tag);
        }

        // Commit the transaction
        transaction.commit();

    }

    @Override
    public void onDriverSelected(String key) {

        Fragment_DriverDetails fragment = Fragment_DriverDetails.newInstance(key);

        if (mLandscapeView) {
            replaceFragment(R.id.container_details, Constants.TAG_FRAGMENT_DRIVER_DETAILS, fragment, false);
        } else {
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_DRIVER_DETAILS, fragment, false);

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

                    //Utils_General.showToast(getApplicationContext(), "Child Changed");

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

    @Override
    public void onGetCustomerSelected() {
        Log.i(Constants.LOG_TAG, "onGetCustomerSelected called");

        //Fragment_CustomerList fragment = Fragment_CustomerList.newInstance();
        //replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_LIST, fragment);

        Intent intent = new Intent(this, Activity_SelectCustomer.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_CUSTOMER);

    }

    @Override
    public void onGetLocationPickup(Customer customer) {
        Log.i(Constants.LOG_TAG, "onGetLocationPickup called CustKey:" + customer.getId());

        Intent intent = new Intent(this, Activity_SelectLocation.class);

        intent.putExtra(Constants.EXTRA_CUSTOMER_KEY, customer.getId());
        intent.putExtra(Constants.EXTRA_CUSTOMER_NAME, customer.getCompany());
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION);

    }

    @Override
    public void onGetLocationDelivery() {

    }

    // Callback from Fragment_CustomerList
    @Override
    public void onCustomerSelected(Customer customer) {
        //Log.i(Constants.LOG_TAG, "onCustomerSelected called CustKey:" + customer);

        Fragment_CustomerDetails fragment = Fragment_CustomerDetails.newInstance(customer);

        if (mLandscapeView) {
            replaceFragment(R.id.container_details, Constants.TAG_FRAGMENT_CUSTOMER_DETAILS, fragment, false);
        } else {
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_DETAILS, fragment, false);
        }
    }

    @Override
    public void onAddCustomerClicked() {
        Log.i(Constants.LOG_TAG, "onAddCustomerClicked() called");

        Fragment_CustomerAdd fragment = Fragment_CustomerAdd.newInstance();

        Log.i(Constants.LOG_TAG, "mLandscape = " + mLandscapeView);

        replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_CUSTOMER_ADD, fragment, true);

    }

    @Override
    public void onOrderSelected(Order order) {
        Log.i(Constants.LOG_TAG, "onOrderSelected() called");

        Fragment_OrderDetails fragment = Fragment_OrderDetails.newInstance(order);

        if (mLandscapeView) {
            replaceFragment(R.id.container_details, Constants.TAG_FRAGMENT_ORDER_DETAILS, fragment, false);
        } else {
            replaceFragment(R.id.container_master, Constants.TAG_FRAGMENT_ORDER_DETAILS, fragment, false);
        }

    }

    public void setActionBarTitle(String title) {

        if (mActionBar != null) {
            mActionBar.setTitle(title);
        }
    }
}
