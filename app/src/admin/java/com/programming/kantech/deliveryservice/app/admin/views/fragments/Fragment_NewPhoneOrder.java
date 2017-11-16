package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.activities.Activity_SelectCustomer;
import com.programming.kantech.deliveryservice.app.admin.views.activities.Activity_SelectLocation;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Customer;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Location;
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
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by patrick keogh on 2017-08-22.
 * A fragment used to process the new order form and submit the results to Firebase
 */

public class Fragment_NewPhoneOrder extends Fragment {

    private Context mContext;

    // Reference to our rest service
    private ApiInterface apiService;

    // Firebase member variables
    private DatabaseReference mOrderRef;

    // Member variables
    private Customer mSelectedCustomer;
    private Distance mDistance;
    private Location mLocationPickup;
    private String mLocationPickupName;
    private String mLocationPickupAddress;
    private Location mLocationDelivery;
    private String mLocationDeliveryName;
    private String mLocationDeliveryAddress;

    // Views to bind too
    @BindView(R.id.tv_admin_customer_name)
    TextView tv_admin_customer_name;

    @BindView(R.id.tv_admin_customer_address)
    TextView tv_admin_customer_address;

    @BindView(R.id.tv_pickup_date)
    TextView tv_pickup_date;

    @BindView(R.id.tv_cust_location_pickup_name)
    TextView tv_cust_location_pickup_name;

    @BindView(R.id.tv_cust_location_pickup_address)
    TextView tv_cust_location_pickup_address;

    @BindView(R.id.iv_select_location_pickup)
    ImageView iv_select_location_pickup;

    @BindView(R.id.tv_cust_location_delivery_name)
    TextView tv_cust_location_delivery_name;

    @BindView(R.id.tv_cust_location_delivery_address)
    TextView tv_cust_location_delivery_address;

    @BindView(R.id.iv_select_location_delivery)
    ImageView iv_select_location_delivery;

    @BindView(R.id.btn_phone_order_add)
    Button btn_phone_order_add;

    @BindView(R.id.btn_phone_order_cancel)
    Button btn_phone_order_cancel;

    private long mDate;

    // Reference to the GetCustomerClickListener interface
    NewOrderListener mCallback;

    // NewOrderListener interface,
    // calls a methods in the host activity
    public interface NewOrderListener {

        // Notify the activity that this fragment has be loaded,
        // may be from activity, orientation change, or back button
        void onFragmentLoaded(String tag);
    }

    /**
     * Static factory method that
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_NewPhoneOrder newInstance(@Nullable Customer customer) {
        Fragment_NewPhoneOrder f = new Fragment_NewPhoneOrder();

        Bundle args = new Bundle();

        // Add any required arguments for start up - None needed right now
        args.putParcelable(Constants.EXTRA_CUSTOMER_KEY, customer);
        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        apiService = ApiClient.getClient().create(ApiInterface.class);

        // Get a reference to the locations table
        mOrderRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_order_add, container, false);

        ButterKnife.bind(this, rootView);

        setupForm();

        return rootView;
    }

    @OnClick(R.id.layout_order_date)
    public void onSelectDateClicked() {

        Calendar cal = Calendar.getInstance(TimeZone.getDefault()); // Get current date

        // Create the DatePickerDialog instance
        DatePickerDialog datePicker = new DatePickerDialog(mContext,
                R.style.ThemeOverlay_AppCompat_Dialog_Alert, datePickerListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePicker.setCancelable(false);
        datePicker.setTitle("Select A Pickup Date");
        datePicker.show();
    }

    @OnClick(R.id.layout_order_customer)
    public void onSelectCustomerClicked() {
        Intent intent = new Intent(getActivity(), Activity_SelectCustomer.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_CUSTOMER);
    }

    @OnClick(R.id.layout_order_location_pickup)
    public void onSelectPickupLocationClicked() {
        Intent intent = new Intent(getActivity(), Activity_SelectLocation.class);

        intent.putExtra(Constants.EXTRA_CUSTOMER, mSelectedCustomer);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION);
    }

    @OnClick(R.id.layout_order_location_delivery)
    public void onSelectDeliveryLocationClicked() {
        Intent intent = new Intent(getActivity(), Activity_SelectLocation.class);

        intent.putExtra(Constants.EXTRA_CUSTOMER, mSelectedCustomer);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_DELIVERY_LOCATION);
    }

    @OnClick(R.id.btn_phone_order_add)
    public void btnAddOrderClicked() {
        createNewOrder();
    }

    @OnClick(R.id.btn_phone_order_cancel)
    public void btnCancelOrderClicked() {
        cancelNewOrder();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Notify the activity this fragment was loaded
        mCallback.onFragmentLoaded(Constants.TAG_FRAGMENT_ORDER_ADD);
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_NewPhoneOrder.NewOrderListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement GetCustomerClickListener");
        }
    }

    private void setupForm() {

        if (mSelectedCustomer != null) {
            iv_select_location_pickup.setEnabled(true);
            iv_select_location_pickup.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_place_accent_24dp));

            iv_select_location_delivery.setEnabled(true);
            iv_select_location_delivery.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_place_accent_24dp));

            tv_admin_customer_name.setText(mSelectedCustomer.getCompany());
            tv_admin_customer_address.setText(mSelectedCustomer.getContact_name());

        } else {
            iv_select_location_pickup.setEnabled(false);
            iv_select_location_pickup.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_place_disabled_24dp));

            iv_select_location_delivery.setEnabled(false);
            iv_select_location_delivery.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_place_disabled_24dp));

            tv_admin_customer_name.setText("");
            tv_admin_customer_address.setText("");
        }

        if (mLocationPickup != null) {
            tv_cust_location_pickup_name.setText(mLocationPickupName);
            tv_cust_location_pickup_address.setText(mLocationPickupAddress);
        } else {
            tv_cust_location_pickup_name.setText("");
            tv_cust_location_pickup_address.setText("");
        }

        if (mLocationDelivery != null) {
            tv_cust_location_delivery_name.setText(mLocationDeliveryName);
            tv_cust_location_delivery_address.setText(mLocationDeliveryAddress);
        } else {
            tv_cust_location_delivery_name.setText("");
            tv_cust_location_delivery_address.setText("");

        }

        if (mDate != 0) {

            SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
            String dateString;

            dateString = format.format(mDate);
            tv_pickup_date.setText(dateString);
        } else {
            tv_pickup_date.setText("");
        }

        if (mDate == 0 || mSelectedCustomer == null || mLocationDelivery == null || mLocationPickup == null) {
            btn_phone_order_add.setEnabled(false);
            btn_phone_order_add.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryLight));
        } else {
            btn_phone_order_add.setEnabled(true);
            btn_phone_order_add.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }

    }

    // Get the distance between the pickup and delivery location
    private void getOrderDistance() {

        if (Utils_General.isNetworkAvailable(mContext)) {

            if (mLocationDelivery != null && mLocationPickup != null) {

                // Create origin and destination strings for google
                String origin = "place_id:" + mLocationPickup.getPlaceId();
                String dest = "place_id:" + mLocationDelivery.getPlaceId();

                Call<Results> call = apiService.getDistance(origin, dest);

                call.enqueue(new Callback<Results>() {
                    @Override
                    public void onResponse(@NonNull Call<Results> call, @NonNull Response<Results> response) {

                        //Log.i(Constants.LOG_TAG, "Response:" + response);

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

                                //Log.i(Constants.LOG_TAG, "Response Distance:" + mDistance.getText());
                            }

                            //distance_ok(fetchResults);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Results> call, @NonNull Throwable t) {
                        // Log error here since request failed
                        Log.e(Constants.LOG_TAG, t.toString());
                        //distance_failed(response.message());
                    }
                });


            }

        } else {
            Utils_General.showToast(mContext, getString(R.string.msg_no_network));
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_SELECT_CUSTOMER) {
            if (resultCode == RESULT_OK) {
                // Customer was successfully selected
                Utils_General.showToast(getContext(), "Customer Selected");

                // Get the customer from the intent data
                mSelectedCustomer = data.getParcelableExtra(Constants.EXTRA_CUSTOMER);
                setupForm();

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(getContext(), "Customer Not Selected");
                //finish();
            }
        } else if (requestCode == Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION) {
            if (resultCode == RESULT_OK) {
                // Customer was successfully selected
                Utils_General.showToast(getContext(), "Pickup Location Selected");

                // Get the customer from the intent data
                mLocationPickup = data.getParcelableExtra(Constants.EXTRA_LOCATION);
                mLocationPickupName = data.getStringExtra(Constants.EXTRA_LOCATION_NAME);
                mLocationPickupAddress = data.getStringExtra(Constants.EXTRA_LOCATION_ADDRESS);
                setupForm();
                getOrderDistance();

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(getContext(), "Pickup Location Not Selected");
                //finish();
            }
        } else if (requestCode == Constants.REQUEST_CODE_SELECT_DELIVERY_LOCATION) {
            if (resultCode == RESULT_OK) {
                // Customer was successfully selected
                Utils_General.showToast(getContext(), "Delivery Location Selected");

                // Get the customer from the intent data
                mLocationDelivery = data.getParcelableExtra(Constants.EXTRA_LOCATION);
                mLocationDeliveryName = data.getStringExtra(Constants.EXTRA_LOCATION_NAME);
                mLocationDeliveryAddress = data.getStringExtra(Constants.EXTRA_LOCATION_ADDRESS);
                setupForm();
                getOrderDistance();

            } else if (resultCode == RESULT_CANCELED) {
                Utils_General.showToast(getContext(), "Delivery Location Not Selected");
                //finish();
            }
        }
    }

    private void cancelNewOrder() {

        mSelectedCustomer = null;
        mLocationPickup = null;
        mLocationDelivery = null;
        mDate = 0;

        setupForm();


    }

    private void createNewOrder() {

        if (Utils_General.isNetworkAvailable(mContext)) {

            // Avoid passing null as parent to dialogs
            final ViewGroup nullParent = null;

            LayoutInflater inflater = this.getLayoutInflater();
            View dialog_confirm = inflater.inflate(R.layout.alert_confirm_order, nullParent);

            TextView tv_name = dialog_confirm.findViewById(R.id.tv_confirm_order_company);
            tv_name.setText(mSelectedCustomer.getCompany());

            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.alert_confirm_new_order)
                    .setView(dialog_confirm)
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // get today at 00:00 hrs
                            //Calendar date = new GregorianCalendar();
                            //long mDisplayDateStartTimeInMillis = Utils_General.getStartTimeForDate(date.getTimeInMillis());

                            final Order order = new Order();
                            order.setCustomerId(mSelectedCustomer.getId());
                            order.setCustomerName(mSelectedCustomer.getCompany());
                            order.setCustomerContact(mSelectedCustomer.getContact_name());
                            order.setCustomerPhone(mSelectedCustomer.getContact_number());

                            order.setDeliveryLocationId(mLocationDelivery.getPlaceId());
                            order.setPickupLocationId(mLocationPickup.getPlaceId());
                            order.setType(Constants.ORDER_TYPE_PHONE);
                            order.setStatus(Constants.ORDER_STATUS_BOOKED);
                            order.setDistance(mDistance.getValue());
                            order.setDistance_text(mDistance.getText());

                            order.setDriverName(Constants.FIREBASE_DEFAULT_NO_DRIVER);

                            DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
                            Double distance = Double.valueOf(df.format(mDistance.getValue() / 1000));

                            // TODO: store in a db so it can be changed from Firebase
                            double dblAmount = distance * 2.13;
                            int intAmount = (int) (dblAmount * 100);

                            order.setAmount(intAmount);

                            long newDate = Utils_General.getStartTimeForDate(mDate);

                            //Log.i(Constants.LOG_TAG, "THIS:" + newDate);
                            order.setPickupDate(newDate);

                            mOrderRef
                                    .push()
                                    .setValue(order, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError,
                                                               DatabaseReference databaseReference) {

                                            String uniqueKey = databaseReference.getKey();

                                            order.setId(uniqueKey);

                                            mOrderRef.child(uniqueKey).setValue(order);

                                            Utils_General.showToast(getContext(), getString(R.string.order_booked));

                                            mLocationDelivery = null;
                                            mLocationPickup = null;
                                            mSelectedCustomer = null;
                                            mDate = 0;

                                            setupForm();

                                            Log.i(Constants.LOG_TAG, "key:" + uniqueKey);
                                        }
                                    });

                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Utils_General.showToast(getContext(), getString(R.string.order_cancelled));

                            cancelNewOrder();

                            setupForm();

                        }
                    })
                    .show();

        } else {
            Utils_General.showToast(mContext, getString(R.string.msg_no_network));
        }
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


            mDate = c.getTimeInMillis();

            setupForm();

        }
    };
}
