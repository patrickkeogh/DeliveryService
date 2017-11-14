package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;
import com.programming.kantech.deliveryservice.app.utils.Utils_Preferences;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-09-21.
 *
 */

public class Activity_Main extends AppCompatActivity {

    // Member variables
    private ActionBar mActionBar;
    private AppUser mAppUser;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(Constants.LOG_TAG, "onCreate() in User Activity_Main");

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_USER)) {
                mAppUser = savedInstanceState.getParcelable(Constants.STATE_INFO_USER);
            }
        } else {

            mAppUser = getIntent().getParcelableExtra(Constants.EXTRA_USER);
        }

        if (mAppUser == null) {
            throw new IllegalArgumentException("Must pass EXTRA_USER");
        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle(mAppUser.getContact_name());
        }
    }

    /**
     * Save the current state of this activity
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the driver in the instance state
        outState.putParcelable(Constants.STATE_INFO_USER, mAppUser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.i(Constants.LOG_TAG, "onOptionsItemSelected:" + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_user_orders:
                Intent intent = new Intent(Activity_Main.this, Activity_MyOrders.class);
                intent.putExtra(Constants.EXTRA_USER, mAppUser);
                startActivity(intent);
                return true;
            case R.id.action_sign_out:
                //Log.i(Constants.LOG_TAG, "Sign out clicked:");
                AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Intent intent = new Intent(Activity_Main.this, Activity_Splash.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @OnClick(R.id.btn_user_place_order)
    public void placeOrder() {

        // Open the place order activity
        Intent intent = new Intent(Activity_Main.this, Activity_PlaceOrder.class);
        intent.putExtra(Constants.EXTRA_USER, mAppUser);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void onSignedInInitialize(final FirebaseUser user) {
        String mUsername = user.getDisplayName();

        if (mActionBar != null) {
            mActionBar.setTitle(mUsername);
        }

        DatabaseReference userRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.FIREBASE_NODE_USERS).child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOG_TAG, "onDataChange()called for user");

                if (dataSnapshot.exists()) {
                    // run some code

                    mAppUser = dataSnapshot.getValue(AppUser.class);
                    Log.i(Constants.LOG_TAG, "The user is in the db:" + user.toString());

                    sendTokenToServer();

                } else {
                    Log.i(Constants.LOG_TAG, "The user is not in the database");

                    // The user is not in db, sent them to the registration activity
                    Intent intent = new Intent(Activity_Main.this, Activity_UserRegistration.class);

                    startActivity(intent);

                    finish();


                    // User is not in the driver db. add them
                    //User user = new Driver(user.getUid(), user.getDisplayName(), user.getEmail(), "", false, false, "", "");

                    // TODO: Not sure which way is better?????
                    //mUserRef.child(user.getUid()).setValue(driver);
                    //mDriverDBReference.push().setValue(driver);

                    //checkIfAuthorized();

                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendTokenToServer() {

        String token = FirebaseInstanceId.getInstance().getToken();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_USERS)
                .child(mAppUser.getId()).child(Constants.FIREBASE_CHILD_DEVICE);

                userRef.setValue(token);

        Utils_Preferences.saveHasTokenBeenSent(getApplicationContext(), true);

    }
}
