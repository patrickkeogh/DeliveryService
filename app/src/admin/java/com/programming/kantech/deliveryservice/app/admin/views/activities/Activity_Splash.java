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
import com.google.firebase.messaging.FirebaseMessaging;
import com.programming.kantech.deliveryservice.app.R;
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
        //Log.i(Constants.LOG_TAG, "Log the users name:" + user.getDisplayName());

        tv_splash_message.setText(R.string.msg_splash_initializing_app);

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
}
