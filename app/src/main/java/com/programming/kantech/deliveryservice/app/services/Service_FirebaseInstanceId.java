package com.programming.kantech.deliveryservice.app.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_Preferences;

/**
 * Created by patrick keogh on 2017-08-15.
 */

public class Service_FirebaseInstanceId extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(Constants.LOG_TAG, "Refreshed token called in Service: " + refreshedToken);

        // Get a Firebase User reference
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is logged in, send the new token to the database
            sendRegistrationToServer(refreshedToken, user);
            Utils_Preferences.saveHasTokenBeenSent(getApplicationContext(), true);
        }else{
            //  User is not logged in yet, send token to the server after they log in
            Utils_Preferences.saveHasTokenBeenSent(getApplicationContext(), false);
        }
    }

    /**
     * Persist token to third-party servers.
     *
     * @param token The new token.
     * @param user
     */
    private void sendRegistrationToServer(String token, FirebaseUser user) {
        // TODO: Implement this method to send token to your app server.

        // Get a reference to the drivers table
        DatabaseReference mDriverTableRef = FirebaseDatabase.getInstance().getReference().child("drivers");

        mDriverTableRef.child(user.getUid()).child("device").setValue(token);


    }
}
