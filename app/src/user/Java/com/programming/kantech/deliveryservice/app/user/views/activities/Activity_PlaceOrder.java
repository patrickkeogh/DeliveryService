package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.data.model.pojo.directions.Distance;
import com.programming.kantech.deliveryservice.app.data.model.pojo.directions.Leg;
import com.programming.kantech.deliveryservice.app.data.model.pojo.directions.Results;
import com.programming.kantech.deliveryservice.app.data.model.pojo.directions.Route;
import com.programming.kantech.deliveryservice.app.data.retrofit.ApiClient;
import com.programming.kantech.deliveryservice.app.data.retrofit.ApiInterface;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by patrick keogh on 2017-09-21.
 *
 */

public class Activity_PlaceOrder extends AppCompatActivity {

    private long mSelectedDate;
    private AppUser mAppUser;
    private Distance mDistance;

    private ApiInterface apiService;

    private MaterialDialog.Builder builder;

    private MaterialDialog dialog_charging;

    private String mLocationPickupId = null;
    private String mLocationPickupName;
    private String mLocationPickupAddress;

    private String mLocationDeliveryId = null;
    private String mLocationDeliveryName;
    private String mLocationDeliveryAddress;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tv_selected_date)
    TextView tv_selected_date;

    @BindView(R.id.tv_user_order_company)
    TextView tv_user_order_company;

    @BindView(R.id.tv_user_order_name)
    TextView tv_user_order_name;

    @BindView(R.id.tv_user_order_phone)
    TextView tv_user_order_phone;

    @BindView(R.id.tv_location_address_pickup)
    TextView tv_location_address_pickup;

    @BindView(R.id.tv_location_name_pickup)
    TextView tv_location_name_pickup;

    @BindView(R.id.tv_location_name_delivery)
    TextView tv_location_name_delivery;

    @BindView(R.id.tv_location_address_delivery)
    TextView tv_location_address_delivery;

    @BindView(R.id.btn_user_place_order)
    Button btn_user_place_order;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        ButterKnife.bind(this);

        apiService = ApiClient.getClient().create(ApiInterface.class);

        // Get the logged user from the intent or the state
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_USER)) {
                mAppUser = savedInstanceState.getParcelable(Constants.STATE_INFO_USER);
            }
        } else {
            mAppUser = getIntent().getParcelableExtra(Constants.EXTRA_USER);
        }

        if (mAppUser == null) {
            throw new IllegalArgumentException("Must pass EXTRA_USER");
        } else {
            tv_user_order_company.setText(mAppUser.getCompany());
            tv_user_order_name.setText(mAppUser.getContact_name());
            tv_user_order_phone.setText(mAppUser.getContact_number());
        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("Place An Order");
        }

        setupForm();
    }

    /**
     * Save the current state of this activity
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the user in the instance state
        outState.putParcelable(Constants.STATE_INFO_USER, mAppUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Log.i(Constants.LOG_TAG, "onActivityResult in PlaceOrder");

        if (requestCode == Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION) {
            if (resultCode == RESULT_OK) {

                // Location was successfully selected
                //Utils_General.showToast(this, "Pickup Location Selected");

                // Get the customer from the intent data
                mLocationPickupId = data.getStringExtra(Constants.EXTRA_PLACE_ID);
                mLocationPickupName = data.getStringExtra(Constants.EXTRA_LOCATION_NAME);
                mLocationPickupAddress = data.getStringExtra(Constants.EXTRA_LOCATION_ADDRESS);

                setupForm();

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(this, getString(R.string.toast_pickup_location_not_selected));
                //finish();
            }
        } else if (requestCode == Constants.REQUEST_CODE_SELECT_DELIVERY_LOCATION) {
            if (resultCode == RESULT_OK) {

                // Location was successfully selected
                //Utils_General.showToast(this, "Delivery Location Selected");

                // Get the location data from the intent data
                mLocationDeliveryId = data.getStringExtra(Constants.EXTRA_PLACE_ID);
                mLocationDeliveryName = data.getStringExtra(Constants.EXTRA_LOCATION_NAME);
                mLocationDeliveryAddress = data.getStringExtra(Constants.EXTRA_LOCATION_ADDRESS);
                setupForm();

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(this, getString(R.string.toast_delivery_location_not_selected));
            }
        }

    }

    @OnClick(R.id.btn_user_place_order)
    public void placeOrder() {

        if(Utils_General.isNetworkAvailable(this)){

            final Order order = new Order();
            order.setCustomerId(mAppUser.getId());
            order.setCustomerName(mAppUser.getCompany());
            order.setCustomerContact(mAppUser.getContact_name());
            order.setCustomerPhone(mAppUser.getContact_number());
            order.setDriverName(Constants.FIREBASE_DEFAULT_NO_DRIVER);
            order.setDeliveryLocationId(mLocationDeliveryId);
            order.setPickupLocationId(mLocationPickupId);
            order.setType(Constants.ORDER_TYPE_USER);
            order.setStatus(Constants.ORDER_STATUS_BOOKED);

            long newDate = Utils_General.getStartTimeForDate(mSelectedDate);
            order.setPickupDate(newDate);

            Double distance;
            DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);

            if (mDistance != null) {
                order.setDistance(mDistance.getValue());
                order.setDistance_text(mDistance.getText());
                distance = Double.valueOf(df.format(mDistance.getValue() / 1000));
            } else {
                order.setDistance(0);
                order.setDistance_text(Constants.FIREBASE_DEFAULT_UNKNOWN_DISTANCE);
                distance = 0.0;
            }

            // TODO: store price in a db so it can be updated from Firebase
            double dblAmount = distance * Constants.COST_PER_KM;
            int intAmount = (int) (dblAmount * 100);

            order.setAmount(intAmount);


            Intent intent = new Intent(Activity_PlaceOrder.this, Activity_Checkout.class);
            intent.putExtra(Constants.EXTRA_ORDER, order);
            intent.putExtra(Constants.EXTRA_USER, mAppUser);

            startActivity(intent);
            finish();
        }else{
            Utils_General.showToast(this, getString(R.string.msg_no_network));
        }

    }

    @OnClick(R.id.layout_order_date)
    public void getDate() {

        Calendar cal = Calendar.getInstance(TimeZone.getDefault()); // Get current date

        // Create the DatePickerDialog instance
        DatePickerDialog datePicker = new DatePickerDialog(this,
                R.style.ThemeOverlay_AppCompat_Dialog_Alert, datePickerListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePicker.setCancelable(false);
        datePicker.setTitle(getString(R.string.alert_select_date));
        datePicker.show();
    }

    @OnClick(R.id.layout_order_location_pickup)
    public void getPickupLocation() {
        Intent intent = new Intent(Activity_PlaceOrder.this, Activity_SelectLocation.class);
        intent.putExtra(Constants.EXTRA_USER, mAppUser);
        intent.putExtra(Constants.EXTRA_LOCATION_SELECT_MESSAGE, R.string.select_pickup_location_title);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION);
    }

    @OnClick(R.id.layout_order_location_delivery)
    public void getDeliveryLocation() {

        // Open select location activity

        Intent intent = new Intent(Activity_PlaceOrder.this, Activity_SelectLocation.class);
        intent.putExtra(Constants.EXTRA_USER, mAppUser);
        intent.putExtra(Constants.EXTRA_LOCATION_SELECT_MESSAGE, R.string.select_delivery_location_title);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_DELIVERY_LOCATION);

    }

    // Date picker listener
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            final Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, selectedYear);
            c.set(Calendar.MONTH, selectedMonth);
            c.set(Calendar.DAY_OF_MONTH, selectedDay);

            c.set(Calendar.HOUR, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);


            mSelectedDate = c.getTimeInMillis();

            setupForm();

        }
    };

    private void setupForm() {

        if (mSelectedDate == 0) {
            tv_selected_date.setVisibility(View.GONE);
        } else {
            tv_selected_date.setVisibility(View.VISIBLE);
            SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());

            String dateString;

            dateString = format.format(mSelectedDate);
            tv_selected_date.setText(dateString);
        }

        if (!Objects.equals(mLocationPickupId, "")) {
            tv_location_name_pickup.setText(mLocationPickupName);
            tv_location_address_pickup.setText(mLocationPickupAddress);
        } else {
            tv_location_name_pickup.setText("");
            tv_location_address_pickup.setText("");
        }

        if (!Objects.equals(mLocationDeliveryId, "")) {
            tv_location_name_delivery.setText(mLocationDeliveryName);
            tv_location_address_delivery.setText(mLocationDeliveryAddress);
        } else {
            tv_location_name_delivery.setText("");
            tv_location_address_delivery.setText("");
        }

        if (mLocationPickupId != null && mLocationDeliveryId != null) {

            builder = new MaterialDialog.Builder(Activity_PlaceOrder.this)
                    .content(R.string.alert_fetching)
                    .cancelable(false)
                    .progress(true, 0);

            dialog_charging = builder.build();
            dialog_charging.show();


            String origin = "place_id:" + mLocationPickupId;

            String dest = "place_id:" + mLocationDeliveryId;

            Call<Results> call = apiService.getDistance(origin, dest);

            call.enqueue(new Callback<Results>() {
                @Override
                public void onResponse(@NonNull Call<Results> call, @NonNull Response<Results> response) {

                    //Log.i(Constants.LOG_TAG, "Response:" + response);

                    dialog_charging.dismiss();

                    if (response.isSuccessful()) {
                        // Code 200, 201
                        Results results = response.body();

                        List<Route> routes;

                        if (results != null) {
                            routes = results.getRoutes();
                            Route route = routes.get(0);

                            List<Leg> legs = route.getLegs();

                            Leg trip = legs.get(0);

                            mDistance = trip.getDistance();

                            Log.i(Constants.LOG_TAG, "Response Distance:" + mDistance.getText());

                            //btn_user_place_order.setEnabled(true);
                        }

                        if (mSelectedDate == 0 || mLocationPickupId == null || mLocationDeliveryId == null) {
                            btn_user_place_order.setEnabled(false);
                            btn_user_place_order.setBackgroundColor(ContextCompat.getColor(Activity_PlaceOrder.this, R.color.colorPrimaryLight));
                        } else {
                            btn_user_place_order.setEnabled(true);
                            btn_user_place_order.setBackgroundColor(ContextCompat.getColor(Activity_PlaceOrder.this, R.color.colorPrimary));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Results> call, @NonNull Throwable t) {
                    // Log error here since request failed
                    Log.e(Constants.LOG_TAG, t.toString());
                }
            });

        }
        if (mSelectedDate == 0 || mLocationPickupId == null || mLocationDeliveryId == null) {
            btn_user_place_order.setEnabled(false);
            btn_user_place_order.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        }


    }


}
