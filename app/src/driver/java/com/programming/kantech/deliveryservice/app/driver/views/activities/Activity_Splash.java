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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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


    @BindView(R.id.tv_splash_message)
    TextView tv_splash_message;

    @BindView(R.id.tv_splash_message2)
    TextView tv_splash_message2;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mProgressBar;

    @BindView(R.id.tv_splash_subtitle)
    TextView tv_splash_subtitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

        // Set the subtitle for the splash page
        tv_splash_subtitle.setText(R.string.app_title_driver);

        initializeApp();

        mFirebaseAuth = FirebaseAuth.getInstance();

        // TODO: Where would a better place to call this be?
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FIREBASE_NOTIFICATION_TOPIC_ADMIN);

        tv_splash_message.setText(R.string.msg_signing_in);

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
        //Log.i(Constants.LOG_TAG, "onOptionsItemSelected:" + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                //Utils_General.showToast(this, "Signout called");
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(Activity_Splash.this, Activity_Splash.class));
                                finish();
                            }
                        });

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
                tv_splash_message.setText(R.string.msg_sign_in_complete);

            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Utils_General.showToast(this, getString(R.string.msg_sign_in_cancelled));
                tv_splash_message.setText(R.string.msg_sign_in_cancelled);
            }
        }

    }

    private void createAuthListener() {

        if (Utils_General.isNetworkAvailable(this)) {
            Log.i(Constants.LOG_TAG, "we have the internet");

            // we have the internet


            mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user != null) {
                        // Signed In
                        tv_splash_message.setText(R.string.msg_sign_in_complete);
                        mProgressBar.setVisibility(View.GONE);

                        onSignedInInitialize(user);
                    } else {
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

        } else {
            Log.i(Constants.LOG_TAG, "NO internet");
            // no internet, cannot login
            // This is only for testing
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tv_splash_message.setText("You do not have an internet connection.");
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }, 2000);
        }
    }

    // Things in this mehod will only be done on new app installs
    private void initializeApp() {

        // TODO Add if this device has not subscribed to a topic yet
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FIREBASE_NOTIFICATION_TOPIC_DRIVER);
    }

    private void onSignedInInitialize(final FirebaseUser user) {
        //Log.i(Constants.LOG_TAG, "onSignedInInitialize called in Splash Driver");

        //mUsername = user.getDisplayName();
        tv_splash_message.setText(R.string.msg_splash_initializing_app);
        mProgressBar.setVisibility(View.VISIBLE);

        // Do a one time fetch to Firebase to get the Driver Info
        DatabaseReference driverRef = mDriverRef.child(user.getUid());

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOG_TAG, "onDataChange()called for driver");

                // Check if a driver document was found for the use signed in (by userId)
                if (dataSnapshot.exists()) {
                    // run some code
                    Log.i(Constants.LOG_TAG, "we have a snapshot");

                    mDriver = dataSnapshot.getValue(Driver.class);



                    if (mDriver != null) {

                        if (!mDriver.getDriverApproved()) {
                            showDriverNotAuthorized(user);
                        } else {

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(Activity_Splash.this, Activity_Main.class);
                                    intent.putExtra(Constants.EXTRA_DRIVER, mDriver);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 5000); // This is just for testing, maybe lol

                        }
                    }


                } else {
                    // User is not in the driver db. add them
                    mDriver = new Driver(user.getUid(), user.getDisplayName(), user.getEmail(), "", false, false, "", "");

                    mDriverRef.child(user.getUid()).setValue(mDriver);

                    showDriverNotAuthorized(user);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(Constants.LOG_TAG, "error");

            }
        });
    }

    private void sendTokenToServer(FirebaseUser user) {

        String token = FirebaseInstanceId.getInstance().getToken();
        mDriverRef.child(user.getUid()).child(Constants.FIREBASE_CHILD_DEVICE).setValue(token);
        Utils_Preferences.saveHasTokenBeenSent(getApplicationContext(), true);

    }

    private void showDriverNotAuthorized(FirebaseUser user) {

        // Notify the user they have not been approved yet.
        // Will need to add something for when drivers are rejected or blocked later

        // Check if token has been sent before
        if (!Utils_Preferences.getHasTokenBeenSent(getApplicationContext()))
            sendTokenToServer(user);

        //Log.i(Constants.LOG_TAG, "Log the users name:" + user.getDisplayName());

        // This is only for testing
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv_splash_message.setText(R.string.msg_approval_failed);
                tv_splash_message2.setText(R.string.msg_driver_notification);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }, 5000);

    }
}
