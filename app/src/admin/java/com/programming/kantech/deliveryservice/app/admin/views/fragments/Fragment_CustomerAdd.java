package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Customer;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Location;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-08-18.
 *
 */

public class Fragment_CustomerAdd extends Fragment {

    // Views to bind to
    @BindView(R.id.et_customer_add_company)
    EditText et_customer_add_company;

    @BindView(R.id.et_customer_add_contact)
    EditText et_customer_add_contact;

    @BindView(R.id.et_customer_add_email)
    EditText et_customer_add_email;

    @BindView(R.id.et_customer_add_phone)
    EditText et_customer_add_phone;


    @BindView(R.id.tv_location_name)
    TextView tv_location_name;

    @BindView(R.id.tv_location_address)
    TextView tv_location_address;

    // Firebase member variables
    private DatabaseReference mCustomerRef;
    private DatabaseReference mLocationsRef;

    // Local member variables
    private Customer mCustomer;
    private Location mLocation;
    private Place mPlace;

    // Define a new interface SaveCustomerListener that triggers a callback in the host activity
    SaveCustomerListener mCallback;

    // SaveCustomerListener interface, calls a method in the host activity
    // after a new customer has been saved
    public interface SaveCustomerListener {
        void onCustomerSaved(Customer customer);
        void onFragmentLoaded(String tag);
    }

    /**
     * Static factory method that takes a driver object parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_CustomerAdd newInstance() {
        return new Fragment_CustomerAdd();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_customer_add, container, false);

        ButterKnife.bind(this, rootView);

        et_customer_add_phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        mCustomer = new Customer();
        mLocation = new Location();

        Button btn_add_customer = rootView.findViewById(R.id.btn_customer_add);

        btn_add_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddCustomerClicked();
            }
        });

        mCustomerRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_CUSTOMERS);
        mLocationsRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_LOCATIONS);

        return rootView;

    }

    @OnClick(R.id.iv_select_location)
    public void onSelectLocationClicked() {

        onGetLocationButtonClicked();
    }

    @OnClick(R.id.layout_location_select)
    public void onSelectLocationAreaClicked() {

        onGetLocationButtonClicked();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Notify the activity this fragment was loaded
        mCallback.onFragmentLoaded(Constants.TAG_FRAGMENT_CUSTOMER_ADD);
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_CustomerAdd.SaveCustomerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement SaveCustomerListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_PLACE_PICKER && resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(getContext(), data);
            if (place == null) {
                //Log.i(Constants.LOG_TAG, "No place selected");
                return;
            }

            mPlace = place;

            tv_location_address.setText(mPlace.getAddress().toString());
            tv_location_name.setText(mPlace.getName().toString());
        }
    }

    /***
     * Button Click event handler to handle clicking the "select Address" Button
     *
     */
    public void onGetLocationButtonClicked() {
        try {

            // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
            // when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(getActivity());
            startActivityForResult(i, Constants.REQUEST_CODE_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(Constants.LOG_TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
        }
    }

    /***
     * Button Click event handler to handle clicking the "Add Customer" Button
     *
     */
    public void onAddCustomerClicked() {
        boolean hasErrors = false;

        // Validate the customer info

        if (et_customer_add_company.getText().length() == 0) {
            et_customer_add_company.setError(getString(R.string.error_company_is_required));
            hasErrors = true;
        }

        if (et_customer_add_contact.getText().length() == 0) {
            et_customer_add_contact.setError(getString(R.string.error_contact_name_is_required));
            hasErrors = true;
        }

        if (et_customer_add_phone.getText().length() != 14) {
            et_customer_add_phone.setError(getString(R.string.error_phone_number));
            hasErrors = true;
        }

        if (hasErrors) {
            Utils_General.showToast(getContext(), getString(R.string.error_msg));
        } else {

            // Avoid passing null as parent to dialogs
            final ViewGroup nullParent = null;

            LayoutInflater inflater = this.getLayoutInflater();
            View dialog_confirm = inflater.inflate(R.layout.alert_confirm_customer, nullParent);

            TextView tv_confirm_company_name = dialog_confirm.findViewById(R.id.tv_confirm_company_name);
            tv_confirm_company_name.setText(et_customer_add_company.getText().toString());

            TextView tv_confirm_company_address = dialog_confirm.findViewById(R.id.tv_confirm_company_address);
            tv_confirm_company_address.setText(mPlace.getAddress());

            new AlertDialog.Builder(getContext())
            .setTitle(R.string.alert_title_add_new_customer)
            .setView(dialog_confirm)
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    // Add the customer and display message
                    mCustomer.setCompany(et_customer_add_company.getText().toString());
                    mCustomer.setEmail(et_customer_add_email.getText().toString());
                    mCustomer.setContact_name(et_customer_add_contact.getText().toString());
                    mCustomer.setContact_number(et_customer_add_phone.getText().toString());
                    mCustomer.setPlaceId(mPlace.getId());

                    mLocation.setPlaceId(mPlace.getId());
                    mLocation.setMainAddress(true);

                    //Log.i(Constants.LOG_TAG, "Customer:" + mCustomer.toString());

                    mCustomerRef.push()
                    .setValue(mCustomer, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError,
                                               DatabaseReference databaseReference) {
                            String uniqueKey = databaseReference.getKey();

                            mCustomer.setId(uniqueKey);

                            mCustomerRef.child(uniqueKey).setValue(mCustomer);

                            mLocation.setCustId(uniqueKey);

                            addLocation();
                        }
                    });
                }
            })
            .show();
        }
    }

    private void addLocation() {

        mLocationsRef
                .child(mCustomer.getId())
                .push()
                .setValue(mLocation, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        String uniqueKey = databaseReference.getKey();

                        mLocation.setId(uniqueKey);

                        mLocationsRef.child(mCustomer.getId()).child(uniqueKey).setValue(mLocation);

                        // Inform user the new customer has been added and
                        // send them back to the management screen with the new customer highlighted
                        Utils_General.showToast(getContext(), getString(R.string.msg_customer_saved));
                        mCallback.onCustomerSaved(mCustomer);


                    }
                });

    }
}
