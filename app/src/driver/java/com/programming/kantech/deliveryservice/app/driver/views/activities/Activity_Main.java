package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;
import com.programming.kantech.deliveryservice.app.utils.Utils_Preferences;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class Activity_Main extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";

    private String mUsername;
    private ActionBar mActionBar;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    // Member variables for the Firebase database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriverDBReference;

    // Member variables for the Firebase Authorization
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(Constants.LOG_TAG, "onCreate() in Main Activity_Main");

        ButterKnife.inject(this);

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle(mUsername);
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseMessaging.getInstance().subscribeToTopic("Driver");

        mDriverDBReference = mFirebaseDatabase.getReference().child("user_drivers");

        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // Signed In
                    Utils_General.showToast(Activity_Main.this, "You are now signed in");

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
                            Constants.RESULT_CODE_SIGN_IN);
                }


            }
        };

        //checkIfAuthorized();

    }

    private void sendTokenToServer(FirebaseUser user) {

        String token = FirebaseInstanceId.getInstance().getToken();

        mDriverDBReference.child(user.getUid()).child("device").setValue(token);

        Utils_Preferences.saveHasTokenBeenSent(getApplicationContext(), true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RESULT_CODE_SIGN_IN) {

            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Utils_General.showToast(this, "Signed In!");

            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Utils_General.showToast(this, "Signed In Cancelled!");
                finish();
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
            case R.id.action_log_device_id:
                // Get token
                String token = FirebaseInstanceId.getInstance().getToken();
                Log.i(Constants.LOG_TAG, "Device Id:" + token);
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

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
    }

    private void onSignedInInitialize(final FirebaseUser user) {

        mUsername = user.getDisplayName();

        if (mActionBar != null) {
            mActionBar.setTitle(mUsername);
        }

        DatabaseReference driverRef = mDriverDBReference.child(user.getUid());

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOG_TAG, "onDataChange()called for driver");

                if (dataSnapshot.exists()) {
                    // run some code

                    Driver driver = dataSnapshot.getValue(Driver.class);
                    Log.i(Constants.LOG_TAG, "The driver is in the db:" + driver.toString());

                    if(!driver.getDriverApproved()){
                        checkIfAuthorized(user);
                    }

                } else {
                    Log.i(Constants.LOG_TAG, "The driver is not in the database");
                    // User is not in the driver db. add them
                    Driver driver = new Driver(user.getUid(), user.getDisplayName(), user.getEmail(), false, "");

                    // TODO: Not sure which way is better?????
                    mDriverDBReference.child(user.getUid()).setValue(driver);
                    //mDriverDBReference.push().setValue(driver);

                    checkIfAuthorized(user);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //checkIfAuthorized();
        //attachDatabaseReadListener();

    }

    private void checkIfAuthorized(FirebaseUser user) {
        if(!Utils_Preferences.getHasTokenBeenSent(getApplicationContext())) sendTokenToServer(user);

        Log.i(Constants.LOG_TAG, "checkIfAuthorized() in Driver has been called");

        Intent intent = new Intent(Activity_Main.this, Activity_NewDriverRequest.class);
        startActivity(intent);
        finish();

    }


}
