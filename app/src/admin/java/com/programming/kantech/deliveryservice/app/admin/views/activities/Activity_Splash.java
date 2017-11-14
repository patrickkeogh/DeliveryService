package com.programming.kantech.deliveryservice.app.admin.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by patrick on 2017-11-03.
 * Will appear in the back ground while performing start up
 * operations.
 */

public class Activity_Splash extends AppCompatActivity {

    // Member variables for the Firebase Authorization
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;

    private DatabaseReference mAdminRef;

    @BindView(R.id.tv_splash_subtitle)
    TextView tv_splash_subtitle;

    @BindView(R.id.tv_splash_message)
    TextView tv_splash_message;

    @BindView(R.id.tv_splash_message2)
    TextView tv_splash_message2;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mProgressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

        // Set the subtitle for the splash page
        tv_splash_subtitle.setText(R.string.app_title_admin);

        mFirebaseAuth = FirebaseAuth.getInstance();

        // Where would a better place to call this be?
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FIREBASE_NOTIFICATION_TOPIC_ADMIN);

        tv_splash_message.setText(R.string.msg_signing_in);

        mAdminRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ADMIN);

        createAuthListener();

        //mAdminRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ADMIN);
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

        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // Signed In
                    tv_splash_message.setText(R.string.msg_sign_in_complete);

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
        Log.i(Constants.LOG_TAG, "Log the admin name:" + user.getDisplayName());

        tv_splash_message.setText(R.string.msg_splash_initializing_app);

        // get the admins data record, if one does not exist, create one

        mAdminRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOG_TAG, "onDataChange()called for driver");

                // The driver and admin object have the same fields, so we will use
                // the Driver object for both and store them in different nodes

                if (dataSnapshot.exists()) {


                    Driver driver = dataSnapshot.getValue(Driver.class);

                    if (driver != null) {
                        Log.i(Constants.LOG_TAG, "The admin is in the db:" + driver.toString());
                    }

                } else {
                    Log.i(Constants.LOG_TAG, "The admin is not in the database");
                    // user is not in the admin db. add them
                    Driver driver = new Driver(user.getUid(), user.getDisplayName(), user.getEmail(), "", false, false, "", "");

                    // to do: Not sure which way is better?????
                    mAdminRef.child(user.getUid()).setValue(driver);
                    //mDriverDBReference.push().setValue(driver);

                }

                // This only for testing, maybe lol

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Activity_Splash.this, Activity_Main.class);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
