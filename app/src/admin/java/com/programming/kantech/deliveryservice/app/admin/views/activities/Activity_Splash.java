package com.programming.kantech.deliveryservice.app.admin.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Arrays;

/**
 * Created by patrick on 2017-11-03.
 */

public class Activity_Splash extends AppCompatActivity {

    // Member variables for the Firebase Authorization
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;

    // Member variables for the Firebase database Refs
    private DatabaseReference mAdminRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mFirebaseAuth = FirebaseAuth.getInstance();

        // TODO: Where would a better place to call this be?
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FIREBASE_NOTIFICATION_TOPIC_ADMIN);

        createAuthListener();

        mAdminRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ADMIN);
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

    private void onSignedInInitialize(final FirebaseUser user) {

        //String mUsername = user.getDisplayName();

        // User is signed in, send them to the main page

        Intent intent = new Intent(Activity_Splash.this, Activity_Main.class);

        startActivity(intent);
        finish();






        //DatabaseReference adminRef = mAdminRef.child(user.getUid());

//        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.i(Constants.LOG_TAG, "onDataChange()called for driver");
//
//                if (dataSnapshot.exists()) {
//                    // run some code
//
//                    Driver driver = dataSnapshot.getValue(Driver.class);
//                    Log.i(Constants.LOG_TAG, "The driver is in the db:" + driver.toString());
//
//                } else {
//                    Log.i(Constants.LOG_TAG, "The driver is not in the database");
//                    // User is not in the driver db. add them
//                    Driver driver = new Driver(user.getUid(), user.getDisplayName(), user.getEmail(), "", false, false, "", "");
//
//                    // TODO: Not sure which way is better?????
//                    //mUserRef.child(user.getUid()).setValue(driver);
//                    //mDriverDBReference.push().setValue(driver);
//
//                    //checkIfAuthorized();
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        //checkIfAuthorized();
        //attachDatabaseReadListeners();

    }
}
