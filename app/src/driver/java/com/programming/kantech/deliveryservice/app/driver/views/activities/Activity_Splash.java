package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;
import com.programming.kantech.deliveryservice.app.utils.Utils_Preferences;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick on 2017-11-03.
 * An activity used to limit access to the app, until verified and logged in.
 */

public class Activity_Splash extends AppCompatActivity {

    // The driver object for the logged in auth user
    private Driver mDriver;

    // Member variables for the Firebase Authorization
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;

    // Member variables for the Firebase database Refs
    private DatabaseReference mDriverRef;

    @BindView(R.id.tv_request_title)
    TextView mTitle;

    @BindView(R.id.tv_request_message)
    TextView mMessage;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

        initializeApp();

        mFirebaseAuth = FirebaseAuth.getInstance();

        // TODO: Where would a better place to call this be?
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FIREBASE_NOTIFICATION_TOPIC_ADMIN);

        createAuthListener();

        mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS);
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_splash, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG, "onOptionsItemSelected:" + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                //removeDriverFromActive();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthListener);
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
                //finish();
            }
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
                    //onSignedOutCleanup();

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .setTheme(R.style.LoginTheme)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                                    .build(),
                            Constants.REQUEST_CODE_SIGN_IN);
                }
            }
        };
    }

    // Things in this mehod will only be done on new app installs
    private void initializeApp() {

        // TODO Add if this device has not subscribed to a topic yet
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FIREBASE_NOTIFICATION_TOPIC_DRIVER);
    }

    private void onSignedInInitialize(final FirebaseUser user) {

        //mUsername = user.getDisplayName();

        // Do a one time fetch to Firebase to get the Driver Info
        DatabaseReference driverRef = mDriverRef.child(user.getUid());

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOG_TAG, "onDataChange()called for driver");

                // Check if a driver document was found for the use signed in (by userId)
                if (dataSnapshot.exists()) {
                    // run some code

                    mDriver = dataSnapshot.getValue(Driver.class);
                    //Log.i(Constants.LOG_TAG, "The driver is in the db:" + mDriver.toString());

                    if(mDriver != null){

                        if (!mDriver.getDriverApproved()) {
                            showDriverNotAuthorized(user);
                        } else {
                            Intent intent = new Intent(Activity_Splash.this, Activity_Main.class);
                            intent.putExtra(Constants.EXTRA_DRIVER, mDriver);
                            startActivity(intent);
                            finish();
                        }
                    }



                } else {
                    //Log.i(Constants.LOG_TAG, "The driver is not in the database");
                    // User is not in the driver db. add them
                    mDriver = new Driver(user.getUid(), user.getDisplayName(), user.getEmail(), "", false, false, "", "");

                    mDriverRef.child(user.getUid()).setValue(mDriver);
                    //mDriverDBReference.push().setValue(driver);

                    //checkIfAuthorized(user);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //checkIfAuthorized();
        //attachDatabaseReadListeners();

    }

    private void sendTokenToServer(FirebaseUser user) {

        String token = FirebaseInstanceId.getInstance().getToken();
        mDriverRef.child(user.getUid()).child(Constants.FIREBASE_CHILD_DEVICE).setValue(token);
        Utils_Preferences.saveHasTokenBeenSent(getApplicationContext(), true);

    }

    private void showDriverNotAuthorized(FirebaseUser user) {
        // ToDo: Check if token has been sent before
        //if (!Utils_Preferences.getHasTokenBeenSent(getApplicationContext()))
        sendTokenToServer(user);

        mProgressBar.setVisibility(View.VISIBLE);

        Log.i(Constants.LOG_TAG, "checkIfAuthorized() in DriverSplash has been called");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(Constants.LOG_TAG, "stop progress bar");
                //Do something after 10 secs
                mProgressBar.setVisibility(View.GONE);
                mTitle.setText(R.string.request_new_driver_title_completed);
                mMessage.setText(R.string.request_new_driver_message_completed);
            }
        }, 5000);

    }
}
