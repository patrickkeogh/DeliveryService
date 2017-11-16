package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.data.model.pojo.stripe.Charge;
import com.programming.kantech.deliveryservice.app.data.model.pojo.stripe.Outcome;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-09-26.
 * Activity that process's credit cards and returns a token from Stripe
 * Token is sent to the server where the actual charge is made in a Cloud Function
 */

public class Activity_Checkout extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    // Member variables
    private Order mOrder;
    private AppUser mAppUser;
    private Token mToken;
    private GoogleApiClient mClient;

    private MaterialDialog.Builder builder;

    private MaterialDialog dialog_charging;

    // Member variables for the payment fields
    //private String mPaymentName;
    private String mPaymentMonth;
    private String mPaymentYear;
    private String mPaymentNumber;
    private String mPaymentSecurity;
    private String mStatus;

    // Member variables for the Firebase database
    private DatabaseReference mOrderRef;
    private ValueEventListener mChargeEventListener;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.btn_user_checkout_confirm_order)
    Button btn_user_checkout_confirm_order;

    @BindView(R.id.btn_user_checkout_cancel_order)
    Button btn_user_checkout_cancel_order;

    @BindView(R.id.sp_user_checkout_exp_month)
    Spinner sp_user_checkout_exp_month;

    @BindView(R.id.sp_user_checkout_exp_year)
    Spinner sp_user_checkout_exp_year;

    @BindView(R.id.et_user_checkout_card_number_1)
    EditText et_user_checkout_card_number_1;

    @BindView(R.id.et_user_checkout_card_number_2)
    EditText et_user_checkout_card_number_2;

    @BindView(R.id.et_user_checkout_card_number_3)
    EditText et_user_checkout_card_number_3;

    @BindView(R.id.et_user_checkout_card_number_4)
    EditText et_user_checkout_card_number_4;

    @BindView(R.id.tv_user_checkout_company)
    TextView tv_user_checkout_company;

    @BindView(R.id.tv_user_checkout_date)
    TextView tv_user_checkout_date;

    @BindView(R.id.tv_user_checkout_location_pickup)
    TextView tv_user_checkout_location_pickup;

    @BindView(R.id.tv_user_checkout_location_delivery)
    TextView tv_user_checkout_location_delivery;

    @BindView(R.id.tv_user_checkout_amount)
    TextView tv_user_checkout_amount;

    @BindView(R.id.et_user_checkout_svc)
    EditText et_user_checkout_svc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Log.i(Constants.LOG_TAG, "onCreate() in User Activity_Checkout");

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_ORDER)) {
                mOrder = savedInstanceState.getParcelable(Constants.STATE_INFO_ORDER);
            }
            if (savedInstanceState.containsKey(Constants.STATE_INFO_USER)) {
                mAppUser = savedInstanceState.getParcelable(Constants.STATE_INFO_USER);
            }
        } else {
            mOrder = getIntent().getParcelableExtra(Constants.EXTRA_ORDER);
            mAppUser = getIntent().getParcelableExtra(Constants.EXTRA_USER);
        }

        if (mOrder == null || mAppUser == null) {
            throw new IllegalArgumentException("Must pass EXTRA_ORDER & EXTRA_USER");
        } else {
            tv_user_checkout_company.setText(mOrder.getCustomerName());
            tv_user_checkout_date.setText(Utils_General.getFormattedLongDateStringFromLongDate(mOrder.getPickupDate()));

            int scale = 100;
            BigDecimal num1 = new BigDecimal(mOrder.getDistance());
            BigDecimal num2 = new BigDecimal(1000);
            Log.i(Constants.LOG_TAG, "new method" + num1.divide(num2, scale, RoundingMode.DOWN).toString());

            DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
            Double km = Double.valueOf(df.format(mOrder.getDistance() / 1000));

            //Double km = (double) (mOrder.getDistance() / 1000);
            Log.i(Constants.LOG_TAG, "km:" + km);

            String distance_text = km + " km " + " @ $2.13/km";

            double amount = (double) (mOrder.getAmount());

            amount = amount / 100;

            Log.i(Constants.LOG_TAG, "Amount after converting to double:" + amount);

            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
            currency.setMaximumFractionDigits(2);
            currency.setMinimumFractionDigits(2);

            String display_amount = currency.format(amount);

            display_amount += " (" + distance_text + ")";

            //tv_user_checkout_distance.setText(distance_text);

            tv_user_checkout_amount.setText(display_amount);

        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        // Set the toolbar title
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle(R.string.activity_title_checkout);
        }

        // get an array of months for the month spinner
        String[] months = Utils_General.getSpinnerMonths();

        // Create an adapter to hold the month values
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, months);
        sp_user_checkout_exp_month.setAdapter(adapter);
        sp_user_checkout_exp_month.setOnItemSelectedListener(new MonthSelectedListener());
        mPaymentMonth = sp_user_checkout_exp_month.getSelectedItem().toString();

        String[] years = Utils_General.getSpinnerYears();

        // Create an adapter to hold the year values
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.item_spinner, years);
        sp_user_checkout_exp_year.setAdapter(adapter2);
        sp_user_checkout_exp_year.setOnItemSelectedListener(new YearSelectedListener());
        mPaymentYear = sp_user_checkout_exp_year.getSelectedItem().toString();


        setupEditTextListeners();

        validatePaymentFields();

        mOrderRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.FIREBASE_NODE_ORDERS);

    }

    /**
     * Save the current state of this activity
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the driver in the instance state
        outState.putParcelable(Constants.STATE_INFO_USER, mAppUser);
        outState.putParcelable(Constants.STATE_INFO_ORDER, mOrder);
    }

    @OnClick(R.id.btn_user_checkout_cancel_order)
    public void cancelOrder() {

        Intent intent = new Intent(Activity_Checkout.this, Activity_PlaceOrder.class);
        intent.putExtra(Constants.EXTRA_USER, mAppUser);
        startActivity(intent);
        finish();


    }

//    @OnClick(R.id.btn_user_checkout_good_card)
//    public void btn_good_card_clicked() {
//
//        // This is just for testing
//        et_user_checkout_card_number_1.setText("4242");
//        et_user_checkout_card_number_2.setText("4242");
//        et_user_checkout_card_number_3.setText("4242");
//        et_user_checkout_card_number_4.setText("4242");
//
//        et_user_checkout_svc.setText("123");
//
//        sp_user_checkout_exp_month.setSelection(11);
//        sp_user_checkout_exp_year.setSelection(6);
//    }
//
//    @OnClick(R.id.btn_user_checkout_bad_card)
//    public void btn_bad_card_clicked() {
//
//        // This is just for testing
//        et_user_checkout_card_number_1.setText("4000");
//        et_user_checkout_card_number_2.setText("0000");
//        et_user_checkout_card_number_3.setText("0000");
//        et_user_checkout_card_number_4.setText("0002");
//
//        et_user_checkout_svc.setText("123");
//
//        sp_user_checkout_exp_month.setSelection(11);
//        sp_user_checkout_exp_year.setSelection(6);
//    }

    @OnClick(R.id.btn_user_checkout_confirm_order)
    public void confirmOrder() {

        // disable buttons so they cannot be clicked twice
        btn_user_checkout_confirm_order.setEnabled(false);
        btn_user_checkout_confirm_order.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        btn_user_checkout_cancel_order.setEnabled(false);
        btn_user_checkout_cancel_order.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));

        // start a dialog while credit card details are verified and until a resulkt from Stripe is received

        builder = new MaterialDialog.Builder(Activity_Checkout.this)
                .content(R.string.alert_verifying_credit_card)
                .cancelable(false)
                .progress(true, 0);

        dialog_charging = builder.build();
        dialog_charging.show();

        // Hide the keyboard
        Utils_General.hideKeyboard(this, getWindow().getDecorView().getRootView().getWindowToken());

        //int month = Integer.parseInt(mPaymentMonth);
        //int year = Integer.parseInt(mPaymentYear);

        // Used for testing
        // Uses live data
        //Card card = new Card(mPaymentNumber, month, year, mPaymentSecurity);

        // Good purchase - test
        Card card = new Card("4242424242424242", 12, 2023, "123");

        // Declined card
        //Card card2 = new Card("4000000000000002", 12, 2023, "123");

        // Wrong number
        //Card card3 = new Card("4242424242424241", 12, 2023, "123");

        // Wrong cvc
        //Card card4 = new Card("4000000000000127", 12, 2023, "999");


        //card.setCurrency("usd");

        //card.setName("[NAME_SURNAME]");
        //card.setAddressZip("[ZIP]");
        /*
        card.setNumber("4242424242424242");
        card.setExpMonth(12);
        card.setExpYear(19);
        card.setCVC("123");
        */

        //Card card = card0;

        if (!card.validateCard()) {
            Utils_General.showToast(this, getString(R.string.toast_invalid_card_number));
        } else {
            Stripe stripe = new Stripe(this, Constants.STRIPE_PUBLIC_KEY);
            stripe.createToken(card, new TokenCallback() {
                @Override
                public void onError(Exception error) {
                    Log.e(Constants.LOG_TAG, error.getMessage());
                    dialog_charging.dismiss();
                }

                @Override
                public void onSuccess(Token token) {

                    mToken = token;

                    dialog_charging.dismiss();

                    // Now add the order to the database with the token
                    // A firebase function will use the token to make the actual Stripe charge

                    submitOrder();

                    //Log.i(Constants.LOG_TAG, "Token:" + mToken.toString());
                }
            });
        }
    }

    private void submitOrder() {
        if (Utils_General.isNetworkAvailable(Activity_Checkout.this)) {
            // Stripe will only accept integers
            double amount = (double) (mOrder.getAmount());
            amount = amount / 100;
            String display_amount = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount);

            new MaterialDialog.Builder(this)
                    .title(R.string.msg_charged)
                    .content(display_amount)
                    .positiveText(getString(R.string.confirm))
                    .cancelable(false)
                    .negativeText(getString(R.string.cancel))
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Utils_General.showToast(Activity_Checkout.this, getString(R.string.order_cancelled));
                            btn_user_checkout_confirm_order.setEnabled(true);
                            btn_user_checkout_cancel_order.setEnabled(true);
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            builder = new MaterialDialog.Builder(Activity_Checkout.this)
                                    .content(R.string.alert_charging)
                                    .cancelable(false)
                                    .progress(true, 0);

                            dialog_charging = builder.build();
                            dialog_charging.show();


                            // Get a reference to the locations table
                            final DatabaseReference orderRef =
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.FIREBASE_NODE_ORDERS);

                            orderRef
                                    .push()
                                    .setValue(mOrder, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError,
                                                               DatabaseReference databaseReference) {

                                            String uniqueKey = databaseReference.getKey();

                                            mOrder.setId(uniqueKey);

                                            orderRef.child(uniqueKey).setValue(mOrder);

                                            // On server we will check for an update to the "token" field
                                            // which will trigger a call to Strips
                                            orderRef.child(uniqueKey).child(Constants.FIREBASE_CHILD_TOKEN).setValue(mToken);

                                            //Utils_General.showToast(Activity_Checkout.this, getString(R.string.order_booked));


                                            attachChargeListener();

                                        }
                                    });


                        }
                    })
                    .show();

        } else {
            Utils_General.showToast(Activity_Checkout.this, getString(R.string.msg_no_network));
        }
    }

    private void detachDatabaseReadListeners() {
        if (mChargeEventListener != null) {
            mOrderRef.removeEventListener(mChargeEventListener);
            mChargeEventListener = null;
        }
    }

    private void attachChargeListener() {
        if (mChargeEventListener == null) {

            mChargeEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Log.i(Constants.LOG_TAG, dataSnapshot.toString());
                    //Utils_General.showToast(Activity_Checkout.this, "charge added:");

                    Charge charge = dataSnapshot.getValue(Charge.class);
                    if (charge != null) {
                        Outcome outcome = charge.getOutcome();

                        mStatus = charge.getStatus();
                        String title = "Payment " + mStatus.substring(0, 1).toUpperCase() + mStatus.substring(1);

                        // Delete the order if the charge was rejected
                        if (Objects.equals(mStatus, "failed")) {
                            mOrderRef.child(mOrder.getId()).setValue(null);
                            mOrder.setId(null);
                        }

                        showAlertMessage(title, outcome.getSeller_message());


                        //Utils_General.showToast(Activity_Checkout.this, "charge status:" + charge.getStatus());
                    } else {
                        Log.i(Constants.LOG_TAG, "No Charge object");
                        // if no charge object, this is not a paymnet update

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mOrderRef.child(mOrder.getId()).child("charge").addValueEventListener(mChargeEventListener);
        }


    }

    private void showAlertMessage(String title, String message) {
        //Log.i(Constants.LOG_TAG, "Alert Message called");

        dialog_charging.dismiss();

        final ViewGroup nullParent = null;

        LayoutInflater inflater = this.getLayoutInflater();
        View dialog_confirm = inflater.inflate(R.layout.alert_confirm, nullParent);

        TextView tv_message = dialog_confirm.findViewById(R.id.tv_alert_message);
        tv_message.setText(message);

        if (Objects.equals(mStatus, "failed")) {
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setView(dialog_confirm)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //cancelOrder();

                            detachDatabaseReadListeners();
                        }
                    })
                    .show();
        } else {

            Intent intent = new Intent(Activity_Checkout.this, Activity_CheckoutSuccess.class);
            intent.putExtra(Constants.EXTRA_ORDER, mOrder);
            intent.putExtra(Constants.EXTRA_USER, mAppUser);

            startActivity(intent);
            finish();
        }
    }


    private void setupEditTextListeners() {

        et_user_checkout_card_number_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (et_user_checkout_card_number_1.getText().toString().length() == 4) {
                    et_user_checkout_card_number_2.requestFocus();

                }
                validatePaymentFields();
            }
        });

        et_user_checkout_card_number_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (et_user_checkout_card_number_2.getText().toString().length() == 4) {
                    et_user_checkout_card_number_3.requestFocus();

                }
                validatePaymentFields();
            }
        });

        et_user_checkout_card_number_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (et_user_checkout_card_number_3.getText().toString().length() == 4) {
                    et_user_checkout_card_number_4.requestFocus();

                }
                validatePaymentFields();
            }
        });

        et_user_checkout_card_number_4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                validatePaymentFields();
            }
        });

        et_user_checkout_svc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                validatePaymentFields();
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        final Place[] thisPlace = new Place[1];

        PendingResult<PlaceBuffer> pickupResult = Places.GeoDataApi.getPlaceById(mClient, mOrder.getPickupLocationId());

        pickupResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {

                thisPlace[0] = places.get(0);
                tv_user_checkout_location_pickup.setText(thisPlace[0].getAddress().toString());

            }
        });

        PendingResult<PlaceBuffer> deliveryResult = Places.GeoDataApi.getPlaceById(mClient, mOrder.getDeliveryLocationId());

        deliveryResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {

                thisPlace[0] = places.get(0);
                tv_user_checkout_location_delivery.setText(thisPlace[0].getAddress().toString());

            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mClient != null) {
            mClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient = null;
        detachDatabaseReadListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mClient == null) {
            buildApiClient();
            mClient.connect();
        } else {
            if (!mClient.isConnected()) {
                mClient.connect();
            }
        }
    }

    private void buildApiClient() {
        if (mClient == null) {
            mClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }

    private void validatePaymentFields() {

        String number1 = et_user_checkout_card_number_1.getText().toString();
        String number2 = et_user_checkout_card_number_2.getText().toString();
        String number3 = et_user_checkout_card_number_3.getText().toString();
        String number4 = et_user_checkout_card_number_4.getText().toString();

        mPaymentNumber = number1.trim() + number2.trim() + number3.trim() + number4.trim();

        String cvc = et_user_checkout_svc.getText().toString();
        mPaymentSecurity = cvc.trim();


        if (mPaymentMonth == null || mPaymentYear == null || mPaymentNumber.length() != 16 || mPaymentSecurity.length() != 3) {
            btn_user_checkout_confirm_order.setEnabled(false);
            btn_user_checkout_confirm_order.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        } else {
            btn_user_checkout_confirm_order.setEnabled(true);
            btn_user_checkout_confirm_order.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }


    }

    private class MonthSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            mPaymentMonth = sp_user_checkout_exp_month.getSelectedItem().toString();
            validatePaymentFields();
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }

    private class YearSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {

            mPaymentYear = sp_user_checkout_exp_year.getSelectedItem().toString();
            validatePaymentFields();

        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }
}


