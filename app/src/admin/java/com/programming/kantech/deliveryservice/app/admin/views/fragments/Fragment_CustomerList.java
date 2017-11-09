package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.HPLinearLayoutManager;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Customers;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Customer;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-08-29.
 * Displays a list of customers in a FirebaseAdapter
 */

public class Fragment_CustomerList extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private FirebaseRecyclerAdapter<Customer, ViewHolder_Customers> mFireAdapter;

    private GoogleApiClient mClient;

    private Customer mSelectedCustomer;
    private RecyclerView.AdapterDataObserver mObserver;

    // Bind the layout views
    @BindView(R.id.rv_customer_list)
    RecyclerView rv_customer_list;

    @BindView(R.id.tv_empty_view)
    TextView tv_empty_view;

    // Firebase references
    private DatabaseReference mCustomersRef;

    // Define a new interface onCustomerSelected that triggers a callback in the host activity
    CustomerClickListener mCallback;

    // onCustomerSelected interface, calls a method in the host activity named onCustomerSelected
    // onAddCustomerClicked interface, calls a method in the host activity named onAddCustomerClicked
    public interface CustomerClickListener {
        void onCustomerSelected(Customer customer);

        void onAddCustomerClicked();

        void onFragmentLoaded(String tag);
    }

    // Mandatory empty constructor
    public Fragment_CustomerList() {

    }

    /**
     * Static factory method that
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_CustomerList newInstance(Customer selectedCustomer) {

        Fragment_CustomerList f = new Fragment_CustomerList();
        Bundle args = new Bundle();
        args.putParcelable(Constants.EXTRA_CUSTOMER, selectedCustomer);

        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get the fragment layout for the customer list
        final View rootView = inflater.inflate(R.layout.fragment_customer_list, container, false);

        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_CUSTOMER)) {
                Log.i(Constants.LOG_TAG, "we found the recipe key in savedInstanceState");
                mSelectedCustomer = savedInstanceState.getParcelable(Constants.STATE_INFO_CUSTOMER);
            }

        } else {
            Log.i(Constants.LOG_TAG, "Activity_Photo savedInstanceState is null, get data from intent: ");
            mSelectedCustomer = getArguments().getParcelable(Constants.EXTRA_CUSTOMER);
        }

        if (mSelectedCustomer != null) {
            mCallback.onCustomerSelected(mSelectedCustomer);
        }

        // Get a reference to the customers table
        mCustomersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_CUSTOMERS);

        return rootView;

    }

    /**
     * Save the current state of this fragment
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the selected driver in the instance state
        outState.putParcelable(Constants.STATE_INFO_CUSTOMER, mSelectedCustomer);
    }

    @OnClick(R.id.fab_add_customer)
    public void onFabAddCustomerClicked() {
        mCallback.onAddCustomerClicked();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        LinearLayoutManager layoutManager =
//                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        //mCustomerRecyclerView.setLayoutManager(layoutManager);

        HPLinearLayoutManager hpLinearLayoutManager = new HPLinearLayoutManager(getContext());
        rv_customer_list.setLayoutManager(hpLinearLayoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        rv_customer_list.setHasFixedSize(false);

    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_CustomerList.CustomerClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement CustomerClickListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Log.i(Constants.LOG_TAG, "onConnected called");
        loadFirebaseAdapter();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();

        // Notify the activity this fragment was loaded
        mCallback.onFragmentLoaded(Constants.TAG_FRAGMENT_CUSTOMER_LIST);

        if (mClient == null) {
            buildApiClient();
            mClient.connect();
        } else {
            if (!mClient.isConnected()) {
                mClient.connect();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mClient != null) {
            mClient.disconnect();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClient = null;
    }

    private void buildApiClient() {
        if (mClient == null) {
            // Build up the LocationServices API client
            // Uses the addApi method to request the LocationServices API
            // Also uses enableAutoManage to automatically know when to connect/suspend the client
            mClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }

    private void loadFirebaseAdapter() {

        // Create FireBaseAdapter
        mFireAdapter = new FirebaseRecyclerAdapter<Customer, ViewHolder_Customers>(
                Customer.class,
                R.layout.item_admin_customer,
                ViewHolder_Customers.class,
                mCustomersRef) {

            @Override
            public void populateViewHolder(final ViewHolder_Customers holder, final Customer customer, int position) {

                // Highlight the selected record in the list
                if (mSelectedCustomer != null) {
                    if (Objects.equals(customer.getId(), mSelectedCustomer.getId())) {
                        holder.setSelectedColor(getContext(), R.color.colorPrimaryLight);
                    } else {
                        holder.setSelectedColor(getContext(), R.color.colorWhite);
                    }
                }

                // Set the name field
                holder.setName(customer.getCompany());

                // Get the Place based on the stored placeId
                PendingResult<PlaceBuffer> placeResult;
                placeResult = Places.GeoDataApi.getPlaceById(mClient, customer.getPlaceId());

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        Place myPlace = places.get(0);

                        // Set the address field
                        holder.setAddress(myPlace.getAddress().toString());
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Notify the activity a customer has been selected
                        mCallback.onCustomerSelected(customer);
                        customerSelected(customer);
                    }
                });
            }

        };

        rv_customer_list.setAdapter(mFireAdapter);

        // Hide or show the list depending on if there are records
        mCustomersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //remove loading indicator

                // Perform initial setup, this will only be called once
                if (dataSnapshot.hasChildren()) {
                    showList(true);
                } else {
                    showList(false);
                }

                // Create an observer to check if the list changes
                mObserver = new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {

                        int count = mFireAdapter.getItemCount();
                        if (count == 0) {
                            showList(false);
                        } else {
                            showList(true);
                        }
                    }

                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        int count = mFireAdapter.getItemCount();
                        if (count == 0) {
                            showList(false);
                        } else {
                            showList(true);
                        }
                    }
                };
                mFireAdapter.registerAdapterDataObserver(mObserver);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showList(boolean bShowList) {

        if (bShowList) {
            rv_customer_list.setVisibility(View.VISIBLE);
            tv_empty_view.setVisibility(View.GONE);
        } else {
            rv_customer_list.setVisibility(View.GONE);
            tv_empty_view.setVisibility(View.VISIBLE);
        }
    }

    private void customerSelected(Customer customer) {
        Log.i(Constants.LOG_TAG, "customerSelected()");

        mSelectedCustomer = customer;

        // redraw the list to show the selected customer
        mFireAdapter.notifyDataSetChanged();

    }
}
