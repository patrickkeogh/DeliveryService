package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.activities.Activity_SelectCustomer;
import com.programming.kantech.deliveryservice.app.admin.views.activities.Activity_SelectLocation;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Customer;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Location;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by patrick keogh on 2017-08-22.
 *
 */

public class Fragment_NewPhoneOrder extends Fragment {

    private DatabaseReference mOrderRef;

    private Customer mSelectedCustomer;

    private Location mLocationPickup;
    private String mLocationPickupName;
    private String mLocationPickupAddress;

    private Location mLocationDelivery;
    private String mLocationDeliveryName;
    private String mLocationDeliveryAddress;

    private TextView tv_cust_name;
    private TextView tv_pickup_date;

    private TextView tv_cust_location_pickup_name;
    private TextView tv_cust_location_pickup_address;

    private TextView tv_cust_location_delivery_name;
    private TextView tv_cust_location_delivery_address;

    private LinearLayout layout_order_customer;
    private LinearLayout layout_location_pickup;
    private LinearLayout layout_location_delivery;

    private Button btn_select_customer;
    private Button btn_select_location_pickup;
    private Button btn_select_location_delivery;

    private Button btn_get_date;

    private Button btn_submit_order;
    private Button btn_cancel_order;

    private long mDate;

    // Define a new interface onCustomerSelected that triggers a callback in the host activity
    Fragment_NewPhoneOrder.GetCustomerClickListener mCallback;

    // onCustomerSelected interface, calls a method in the host activity named onCustomerSelected
    public interface GetCustomerClickListener {
        void onGetCustomerSelected();

        void onGetLocationPickup(Customer mSelectedCustomer);

        void onGetLocationDelivery();
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Load the saved state if there is one
        if (savedInstanceState != null) {
            Log.i(Constants.LOG_TAG, "Fragment_Ingredients savedInstanceState is not null");
            if (savedInstanceState.containsKey(Constants.STATE_INFO_CUSTOMER)) {
                Log.i(Constants.LOG_TAG, "we found the recipe key in savedInstanceState");
                mSelectedCustomer = savedInstanceState.getParcelable(Constants.STATE_INFO_CUSTOMER);
            }

        } else {
            Log.i(Constants.LOG_TAG, "Fragment_Ingredients savedInstanceState is null, get data from intent");
            Bundle args = getArguments();
            mSelectedCustomer = args.getParcelable(Constants.EXTRA_CUSTOMER);
        }

        // Get a reference to the locations table
        mOrderRef = FirebaseDatabase.getInstance().getReference().child("orders");

        // TODO: Make sure we have a customer

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_order_add, container, false);

        tv_cust_name = rootView.findViewById(R.id.tv_customer_name);
        tv_pickup_date = rootView.findViewById(R.id.tv_pickup_date);


        tv_cust_location_pickup_name = rootView.findViewById(R.id.tv_cust_location_pickup_name);
        tv_cust_location_pickup_address = rootView.findViewById(R.id.tv_cust_location_pickup_address);

        tv_cust_location_delivery_name = rootView.findViewById(R.id.tv_cust_location_delivery_name);
        tv_cust_location_delivery_address = rootView.findViewById(R.id.tv_cust_location_delivery_address);

        layout_order_customer = rootView.findViewById(R.id.layout_order_customer);
        layout_location_pickup = rootView.findViewById(R.id.layout_location_pickup);
        layout_location_delivery = rootView.findViewById(R.id.layout_location_delivery);

        btn_select_customer = rootView.findViewById(R.id.btn_customer_select_customer);
        btn_select_location_pickup = rootView.findViewById(R.id.btn_customer_select_pickup_location);
        btn_select_location_delivery = rootView.findViewById(R.id.btn_customer_select_delivery_location);
        btn_submit_order = rootView.findViewById(R.id.btn_phone_order_add);
        btn_cancel_order = rootView.findViewById(R.id.btn_phone_order_cancel);

        btn_get_date = rootView.findViewById(R.id.btn_get_pickup_date);
        btn_get_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar cal = Calendar.getInstance(TimeZone.getDefault()); // Get current date

                // Create the DatePickerDialog instance
                DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                        R.style.ThemeOverlay_AppCompat_Dialog_Alert, datePickerListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH));
                datePicker.setCancelable(false);
                datePicker.setTitle("Select A Pickup Date");
                datePicker.show();


            }
        });

        btn_select_location_pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), Activity_SelectLocation.class);

                intent.putExtra(Constants.EXTRA_CUSTOMER_KEY, mSelectedCustomer.getId());
                intent.putExtra(Constants.EXTRA_CUSTOMER_NAME, mSelectedCustomer.getCompany());
                startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_PICKUP_LOCATION);
            }
        });

        btn_select_location_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), Activity_SelectLocation.class);

                intent.putExtra(Constants.EXTRA_CUSTOMER_KEY, mSelectedCustomer.getId());
                intent.putExtra(Constants.EXTRA_CUSTOMER_NAME, mSelectedCustomer.getCompany());
                startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_DELIVERY_LOCATION);
            }
        });

        btn_select_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Activity_SelectCustomer.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_CUSTOMER);
            }
        });

        layout_order_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Activity_SelectCustomer.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_CUSTOMER);
            }
        });

        btn_submit_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createNewOrder();

            }
        });

        btn_cancel_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelNewOrder();
            }
        });

        setupForm();

        return rootView;
    }


    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_NewPhoneOrder.GetCustomerClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement GetCustomerClickListener");
        }
    }

    private void setupForm() {

        if (mSelectedCustomer != null) {
            tv_cust_name.setVisibility(View.VISIBLE);
            btn_select_location_pickup.setEnabled(true);
            btn_select_location_pickup.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

            btn_select_location_delivery.setEnabled(true);
            btn_select_location_delivery.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

            tv_cust_name.setText(mSelectedCustomer.getCompany());

            btn_select_customer.setVisibility(View.GONE);
        } else {
            tv_cust_name.setVisibility(View.GONE);
            btn_select_location_pickup.setEnabled(false);
            btn_select_location_pickup.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));

            btn_select_location_delivery.setEnabled(false);
            btn_select_location_delivery.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));

            btn_select_customer.setVisibility(View.VISIBLE);
        }

        if (mLocationPickup != null) {
            btn_select_location_pickup.setVisibility(View.GONE);
            layout_location_pickup.setVisibility(View.VISIBLE);
            tv_cust_location_pickup_name.setText(mLocationPickupName);
            tv_cust_location_pickup_address.setText(mLocationPickupAddress);
        } else {
            btn_select_location_pickup.setVisibility(View.VISIBLE);
            layout_location_pickup.setVisibility(View.GONE);


        }

        if (mLocationDelivery != null) {
            btn_select_location_delivery.setVisibility(View.GONE);
            layout_location_delivery.setVisibility(View.VISIBLE);
            tv_cust_location_delivery_name.setText(mLocationDeliveryName);
            tv_cust_location_delivery_address.setText(mLocationDeliveryAddress);
        } else {
            btn_select_location_delivery.setVisibility(View.VISIBLE);
            layout_location_delivery.setVisibility(View.GONE);
        }

        if (mSelectedCustomer == null || mLocationDelivery == null || mLocationPickup == null) {
            btn_submit_order.setEnabled(false);
            btn_submit_order.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));
        } else {
            btn_submit_order.setEnabled(true);
            btn_submit_order.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        }

        if(mDate == 0){
            tv_pickup_date.setVisibility(View.GONE);
            btn_get_date.setVisibility(View.VISIBLE);
        }else{
            tv_pickup_date.setVisibility(View.VISIBLE);
            SimpleDateFormat format =  new SimpleDateFormat(Constants.DATE_FORMAT);

            String dateString;

            dateString = format.format(mDate);
            tv_pickup_date.setText(dateString);

            btn_get_date.setVisibility(View.GONE);
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

        LayoutInflater inflater = this.getLayoutInflater();
        View dialog_confirm = inflater.inflate(R.layout.alert_confirm_order, null);

        TextView tv_name = dialog_confirm.findViewById(R.id.tv_confirm_order_company);
        tv_name.setText(mSelectedCustomer.getCompany());

        new AlertDialog.Builder(getContext())
                .setTitle("Please confirm a new order for:")
                .setView(dialog_confirm)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // get today at 00:00 hrs
                        Calendar date = new GregorianCalendar();
                        long mDisplayDateStartTimeInMillis = Utils_General.getStartTimeForDate(date.getTimeInMillis());

                        //long startDateMillis = mDisplayDateStartTime.getTimeInMillis();
                        Log.i(Constants.LOG_TAG, "StartTime for 12th:" + mDisplayDateStartTimeInMillis);

                        final Order order = new Order();
                        order.setCustomerId(mSelectedCustomer.getId());
                        order.setCustomerName(mSelectedCustomer.getCompany());
                        order.setDeliveryLocationId(mLocationDelivery.getPlaceId());
                        order.setPickupLocationId(mLocationPickup.getPlaceId());
                        order.setType(Constants.ORDER_TYPE_PHONE);
                        order.setStatus(Constants.ORDER_STATUS_BOOKED);
                        long newDate = Utils_General.getStartTimeForDate(mDate);
                        Log.i(Constants.LOG_TAG, "THIS:" + newDate);
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


    }

    // Listener
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
