package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.user.provider.Contract_DeliveryService;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-09-21.
 * An activity to collect user info and store it in Firebase
 */
public class Activity_UserRegistration extends AppCompatActivity {

    private FirebaseUser mUser;
    private DatabaseReference mUserRef;
    private Place mPlace;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.et_user_add_fullname)
    EditText et_user_add_fullname;

    @BindView(R.id.et_user_add_company)
    EditText et_user_add_company;

    @BindView(R.id.et_user_add_phone)
    EditText et_user_add_phone;

    @BindView(R.id.tv_registration_address_name)
    TextView tv_registration_address_name;

    @BindView(R.id.tv_registration_address)
    TextView tv_registration_address;

    @BindView(R.id.btn_user_register)
    Button btn_user_register;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        //Log.i(Constants.LOG_TAG, "onCreate() in User Activity_Main");

        ButterKnife.bind(this);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser == null) {
            throw new IllegalArgumentException("User is not authenticated");
        } else {
            et_user_add_fullname.setText(mUser.getDisplayName());
        }

        mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_USERS);

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle(R.string.title_user_registration);
        }

        // Add text watchers for the edit fields
        et_user_add_fullname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setupForm();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        et_user_add_phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_PLACE_PICKER && resultCode == Activity.RESULT_OK) {

            Place place = PlacePicker.getPlace(this, data);

            if (place == null) {
                Log.i(Constants.LOG_TAG, "No place selected");
                return;
            }

            mPlace = place;

            tv_registration_address_name.setText(place.getName().toString());

            tv_registration_address.setText(place.getAddress().toString());

            setupForm();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @OnClick(R.id.layout_registration_address)
    public void getCustomerAddress() {
        mPlace = null;
        onGetLocationButtonClicked();
    }

    @OnClick(R.id.btn_user_register)
    public void registerCustomer() {

        final AppUser app_user = new AppUser();
        //final Location location = new Location();

        app_user.setId(mUser.getUid());
        app_user.setCompany(et_user_add_company.getText().toString().trim());
        app_user.setContact_name(et_user_add_fullname.getText().toString().trim());
        app_user.setContact_number(et_user_add_phone.getText().toString());
        app_user.setEmail(mUser.getEmail());

        app_user.setPlaceId(mPlace.getId());

        // Add location to local db
        // Insert a new place into DB
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract_DeliveryService.PlaceEntry.COLUMN_PLACE_ID, mPlace.getId());
        getContentResolver().insert(Contract_DeliveryService.PlaceEntry.CONTENT_URI, contentValues);

        // Push the new user registration to firebase
        //mUserRef.child(mUser.getUid()).setValue(app_user);

        mUserRef.child(mUser.getUid()).setValue(app_user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {

                Utils_General.showToast(getApplicationContext(), "Registration complete");

                Intent intent = new Intent(Activity_UserRegistration.this, Activity_Main.class);
                intent.putExtra(Constants.EXTRA_USER, app_user);
                startActivity(intent);
                finish();
            }
        });
    }



    private void setupForm() {

        // Validate the data, disabling the submit button until all fields are correct

        String name = et_user_add_fullname.getText().toString().trim();
        String company = et_user_add_company.getText().toString().trim();
        String phone = et_user_add_phone.getText().toString().trim();



        if (Objects.equals(name, "") || Objects.equals(company, "") || Objects.equals(phone, "") || mPlace == null) {
            btn_user_register.setEnabled(false);
            btn_user_register.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        } else {
            btn_user_register.setEnabled(true);
            btn_user_register.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }

    /***
     * Button Click event handler to handle clicking the "select Location" Button
     *
     */
    public void onGetLocationButtonClicked() {
        Log.i(Constants.LOG_TAG, "onGetLocationButtonClicked() called");
        try {

            // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
            // when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(this);
            startActivityForResult(i, Constants.REQUEST_CODE_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(Constants.LOG_TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
        }
    }
}
